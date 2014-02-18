package org.jikesrvm.compilers.opt.hir2lir;

import static org.jikesrvm.compilers.opt.driver.OptConstants.RUNTIME_SERVICES_BCI;
import static org.jikesrvm.compilers.opt.ir.Operators.CALL;
import static org.jikesrvm.compilers.opt.ir.Operators.GETSTATIC;
import static org.jikesrvm.compilers.opt.ir.Operators.YIELDPOINT_PROLOGUE_opcode;
import static org.jikesrvm.compilers.opt.ir.Operators.YIELDPOINT_BACKEDGE_opcode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.NormalMethod;
import org.jikesrvm.classloader.RVMField;
import org.jikesrvm.classloader.FieldReference;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.compilers.opt.DefUse;
import org.jikesrvm.compilers.opt.escape.EscapeTransformations;
import org.jikesrvm.compilers.opt.escape.FI_EscapeSummary;
import org.jikesrvm.compilers.opt.inlining.InlineDecision;
import org.jikesrvm.compilers.opt.inlining.Inliner;
import org.jikesrvm.compilers.opt.ir.ALoad;
import org.jikesrvm.compilers.opt.ir.AStore;
import org.jikesrvm.compilers.opt.ir.BasicBlock;
import org.jikesrvm.compilers.opt.ir.BasicBlockEnumeration;
import org.jikesrvm.compilers.opt.ir.Call;
import org.jikesrvm.compilers.opt.ir.GetField;
import org.jikesrvm.compilers.opt.ir.GetStatic;
import org.jikesrvm.compilers.opt.ir.IR;
import org.jikesrvm.compilers.opt.ir.IRTools;
import org.jikesrvm.compilers.opt.ir.Instruction;
import org.jikesrvm.compilers.opt.ir.Move;
import org.jikesrvm.compilers.opt.ir.OperandEnumeration;
import org.jikesrvm.compilers.opt.ir.Prologue;
import org.jikesrvm.compilers.opt.ir.PutField;
import org.jikesrvm.compilers.opt.ir.PutStatic;
import org.jikesrvm.compilers.opt.ir.Register;
import org.jikesrvm.compilers.opt.ir.RegisterOperandEnumeration;
import org.jikesrvm.compilers.opt.ir.operand.LocationOperand;
import org.jikesrvm.compilers.opt.ir.operand.MethodOperand;
import org.jikesrvm.compilers.opt.ir.operand.Operand;
import org.jikesrvm.compilers.opt.ir.operand.RegisterOperand;
import org.jikesrvm.rd.RaceDet;
import org.jikesrvm.rd.Site;
import org.jikesrvm.rd.Stats;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.util.HashMapRVM;
import org.jikesrvm.util.HashSetRVM;

/** RaceDet: read/write barrier instrumentation */ 
public final class RaceDetInstr {

  private final IR ir;
  private final HashSetRVM<Instruction> barriersToInline;
  private final HashMapRVM<Register,RegisterOperand> indirectMetadataRegisterMap;
  private FI_EscapeSummary escapeSummary;
  private HashSetRVM<Instruction> fullyRedInsts;
  private Operand samplingActivatedOperand;

  private static final boolean verbose = false;
  
  RaceDetInstr(IR ir) {
    this.ir = ir;
    this.indirectMetadataRegisterMap = new HashMapRVM<Register,RegisterOperand>();
    this.barriersToInline = new HashSetRVM<Instruction>();
  }
  
  void perform() {
    if (!RaceDet.addSomeBarriers(ir.getMethod())) {
      return;
    }
    
    doEscapeAnalysis();

    if (verbose) {
      System.out.println("Method before: " + ir.getMethod());
      ir.printInstructions();
    }
    
    fullyRedInsts = new HashSetRVM<Instruction>(); 
    if (RaceDet.eliminateRedundantBarriers()) {
      computeRedundantReadBarriers();
    }
    
    // RaceDet: if doing LiteRace with method granularity, do instrumentation here
    if (RaceDet.liteRace() && RaceDet.liteRaceMethod()) {
      NormalMethod target = Entrypoints.rdLiteRaceMethodSampleMethod;
      Instruction callInst = Call.create1(CALL,
                                          null,
                                          IRTools.AC(target.getOffset()),
                                          MethodOperand.STATIC(target),
                                          IRTools.IC(ir.getMethod().getId()));
      ir.cfg.entry().prependInstructionRespectingPrologue(callInst);
    }
    
    processInsts();

    if (verbose) {
      System.out.println("Method after: " + ir.getMethod());
      ir.printInstructions();
    }
    
    for (Instruction barrier : barriersToInline) {
      inline(barrier, ir, true);
    }
  }

  private void doEscapeAnalysis() {
    // use escape analysis to find definitely unshared objects
    if (RaceDet.escapeAnalysis()) {
      escapeSummary = EscapeTransformations.getEscapeSummary(ir);
    } else {
      DefUse.computeDU(ir);
      DefUse.recomputeSSA(ir);
    }
  }
  
  private void processInsts() {
    Instruction next;
    if (RaceDet.useSampling()) {
      samplingActivatedOperand = ir.regpool.makeTempInt();
    } else {
      samplingActivatedOperand = IRTools.IC(1);
    }

    boolean instrumentedPrologue = false;
    for (Instruction inst = ir.firstInstructionInCodeOrder(); inst != null; inst = next) {
      next = inst.nextInstructionInCodeOrder();
      if (fullyRedInsts.contains(inst)) {
        if (verbose) { System.out.println("Skipping redundant instruction: " + inst); }
      } else if (ALoad.conforms(inst) || AStore.conforms(inst)) {
        instrumentArrayInst(inst);
      } else if (GetField.conforms(inst)  || PutField.conforms(inst) ||
                 GetStatic.conforms(inst) || PutStatic.conforms(inst)) {
        instrumentScalarInst(inst);
      // I think the important thing here is to NOT instrument YIELDPOINT_OSR_opcode instructions!
      // That caused a lot of weird crashes.
      // Also, we don't instrument epilogue yieldpoints since no code occurs after them
      } else if (RaceDet.useSampling() &&
                 RaceDet.checkHoisting() &&
                 (inst.getOpcode() == YIELDPOINT_PROLOGUE_opcode ||
                  inst.getOpcode() == YIELDPOINT_BACKEDGE_opcode)) {
        // RaceDet: LATER: technically samplingActivated could change at any GC-safe point,
        // making this optimization perhaps not quite correct
        insertGetSamplingActivated(inst, false);
        instrumentedPrologue |=  (inst.getOpcode() == YIELDPOINT_PROLOGUE_opcode);
      }
    }
    if (VM.VerifyAssertions) {
      if (RaceDet.useSampling() && RaceDet.checkHoisting()) {
        VM._assert(instrumentedPrologue);
      }
    }
  }
  
  private void insertGetSamplingActivated(Instruction inst, boolean before) {
    Instruction samplingActivatedInst =
      GetStatic.create(GETSTATIC,
                       samplingActivatedOperand.asRegister().copyRO(),
                       IRTools.AC(Entrypoints.rdSamplingActivatedField.getOffset()),
                       new LocationOperand(Entrypoints.rdSamplingActivatedField));
    samplingActivatedInst.bcIndex = RUNTIME_SERVICES_BCI;
    samplingActivatedInst.position = inst.position;
    if (before) {
      inst.insertBefore(samplingActivatedInst);
    } else {
      inst.insertAfter(samplingActivatedInst);
    }
  }
  
  private void instrumentArrayInst(Instruction inst) {
    boolean isRead = ALoad.conforms(inst);
    NormalMethod barrierMethod = RaceDet.getAlgoBarrier(inst, isRead, false, true, false, false);

    if (barrierMethod != null) {
      LocationOperand loc = isRead ? ALoad.getLocation(inst) : AStore.getLocation(inst);
      Operand ref = isRead ? ALoad.getArray(inst) : AStore.getArray(inst);

      boolean mightEscape = true;
      if (RaceDet.escapeAnalysis()) {
        if (ref.isRegister()) {
          if (escapeSummary.isThreadLocal(ref.asRegister().getRegister())) {
            if (RaceDet.stats()) { Stats.compileTimeDefinitelyLocalArrays.inc(); }
            mightEscape = false;
          } else {
            if (RaceDet.stats()) { Stats.compileTimeMayEscapeArrays.inc(); }
          }
        }
      }

      if (mightEscape) {
        Operand index = isRead ? ALoad.getIndex(inst) : AStore.getIndex(inst);
        int siteID = Site.getSite(inst, isRead, false, loc.getElementType().getArrayTypeForElementType());

        // just pass the sharing info if we have it
        RegisterOperand indirectMetadata = null;
        if (RaceDet.passIndirectMetadata()) {
          NormalMethod useIndirectMetadataMethod = RaceDet.getBarrierUsePassedMetadata(barrierMethod);
          if (useIndirectMetadataMethod != null) {
            indirectMetadata = getIndirectMetadata(inst, ref, ir);
            if (indirectMetadata != null) {
              barrierMethod = useIndirectMetadataMethod;
              ref = indirectMetadata;
            }
          }
        }
        
        //if (RaceDet.stats()) { Stats.getStatForObject(barrierMethod).inc(); }

        Instruction barrier;
        if (indirectMetadata == null) {
          if (!RaceDet.checkHoisting()) {
            insertGetSamplingActivated(inst, true);
          }
          barrier = Call.create4(CALL,
                                 null,
                                 IRTools.AC(barrierMethod.getOffset()),
                                 MethodOperand.STATIC(barrierMethod),
                                 ref.copy(),
                                 index.copy(),
                                 IRTools.IC(siteID),
                                 samplingActivatedOperand.copy());
        } else {
          barrier = Call.create3(CALL,
                                 null,
                                 IRTools.AC(barrierMethod.getOffset()),
                                 MethodOperand.STATIC(barrierMethod),
                                 ref.copy(),
                                 index.copy(),
                                 IRTools.IC(siteID));
        }
        insert(barrier, true, inst, ir, true);
      }
    }
  }
  
  private void instrumentScalarInst(Instruction inst) {
    boolean isRead = GetField.conforms(inst) || GetStatic.conforms(inst);
    boolean isStatic = GetStatic.conforms(inst) || PutStatic.conforms(inst);
    LocationOperand loc = isStatic ? (isRead ? GetStatic.getLocation(inst) : PutStatic.getLocation(inst))
        : (isRead ? GetField.getLocation(inst)  : PutField.getLocation(inst));
    FieldReference fieldRef = loc.getFieldRef();
    RVMField field = fieldRef.peekResolvedField();
    boolean isResolved = (field != null);
    boolean isVolatile = (field != null && field.isVolatile());
    boolean hasMetadata;
    if (isResolved) {
      hasMetadata = RaceDet.addDirectFieldMetadata(field) ||
      RaceDet.addIndirectFieldMetadata(field);
    } else {
      hasMetadata = RaceDet.mightHaveFieldMetadata(fieldRef, isStatic);
    }
    if (hasMetadata) {
      NormalMethod barrierMethod = RaceDet.getAlgoBarrier(inst, isRead, true, isResolved, isVolatile, isStatic);
      if (barrierMethod != null) {
        Operand ref = null;
        if (!isStatic) {
          ref = isRead ? GetField.getRef(inst) : PutField.getRef(inst);
        }

        boolean mightEscape = true;
        if (RaceDet.escapeAnalysis()) {
          if (ref != null && ref.isRegister()) {
            if (escapeSummary.isThreadLocal(ref.asRegister().getRegister())) {
              if (RaceDet.stats()) { Stats.compileTimeDefinitelyLocalScalars.inc(); }
              mightEscape = false;
            } else {
              if (RaceDet.stats()) { Stats.compileTimeMayEscapeScalars.inc(); }
            }
          }
        }

        if (verbose && !mightEscape) {
          System.out.println("-----------------------------------");
          System.out.println("Escape inst: " + inst);
        }

        if (mightEscape) {
          int siteID = Site.getSite(inst, isRead, isStatic, fieldRef.getFieldContentsType());

          // just pass the sharing info if we have it
          RegisterOperand indirectMetadata = null;
          if (RaceDet.passIndirectMetadata()) {
            NormalMethod useIndirectMetadataMethod = RaceDet.getBarrierUsePassedMetadata(barrierMethod);
            if (useIndirectMetadataMethod != null) {
              indirectMetadata = getIndirectMetadata(inst, ref, ir);
              if (indirectMetadata != null) {
                barrierMethod = useIndirectMetadataMethod;
                ref = indirectMetadata;
              }
            }
          }

          //if (RaceDet.stats()) { Stats.getStatForObject(barrierMethod).inc(); }

          Instruction barrier;
          if (isResolved) {
            if (isVolatile) {
              if (isStatic) {
                barrier = Call.create1(CALL,
                    null,
                    IRTools.AC(barrierMethod.getOffset()),
                    MethodOperand.STATIC(barrierMethod),
                    IRTools.AC(field.getMetadataOffset()));
              } else {
                barrier = Call.create2(CALL,
                    null,
                    IRTools.AC(barrierMethod.getOffset()),
                    MethodOperand.STATIC(barrierMethod),
                    ref.copy(),
                    IRTools.AC(field.getMetadataOffset()));
              }
            } else {
              if (isStatic) {
                if (indirectMetadata == null) {
                  if (!RaceDet.checkHoisting()) {
                    insertGetSamplingActivated(inst, true);
                  }
                  barrier = Call.create3(CALL,
                      null,
                      IRTools.AC(barrierMethod.getOffset()),
                      MethodOperand.STATIC(barrierMethod),
                      IRTools.AC(field.getMetadataOffset()),
                      IRTools.IC(siteID)
                      /*IRTools.AC(ObjectReference.fromObject(field.getDeclaringClass()).toAddress())*/,
                      samplingActivatedOperand.copy());
                } else {
                  barrier = Call.create2(CALL,
                      null,
                      IRTools.AC(barrierMethod.getOffset()),
                      MethodOperand.STATIC(barrierMethod),
                      IRTools.AC(field.getMetadataOffset()),
                      IRTools.IC(siteID));
                      //IRTools.AC(ObjectReference.fromObject(field.getDeclaringClass()).toAddress()));
                }
              } else {
                if (indirectMetadata == null) {
                  if (!RaceDet.checkHoisting()) {
                    insertGetSamplingActivated(inst, true);
                  }
                  barrier = Call.create4(CALL,
                      null,
                      IRTools.AC(barrierMethod.getOffset()),
                      MethodOperand.STATIC(barrierMethod),
                      ref.copy(),
                      IRTools.IC(field.getFieldNumber()),
                      IRTools.IC(siteID),
                      samplingActivatedOperand.copy());
                } else {
                  barrier = Call.create3(CALL,
                      null,
                      IRTools.AC(barrierMethod.getOffset()),
                      MethodOperand.STATIC(barrierMethod),
                      ref.copy(),
                      IRTools.IC(field.getFieldNumber()),
                      IRTools.IC(siteID));
                }
              }
            }
          } else {
            if (isStatic) {
              barrier = Call.create2(CALL,
                  null,
                  IRTools.AC(barrierMethod.getOffset()),
                  MethodOperand.STATIC(barrierMethod),
                  IRTools.IC(fieldRef.getId()),
                  IRTools.IC(siteID));
            } else {
              barrier = Call.create3(CALL,
                  null,
                  IRTools.AC(barrierMethod.getOffset()),
                  MethodOperand.STATIC(barrierMethod),
                  ref.copy(),
                  IRTools.IC(fieldRef.getId()),
                  IRTools.IC(siteID));
            }
          }
          insert(barrier, isResolved, inst, ir, true);
        }
      }
    }
  }
  
  private RegisterOperand getIndirectMetadata(Instruction inst, Operand ref, IR ir) {
    if (verbose) {
      System.out.println("----------------------------------------------");
      System.out.println("Method: " + ir.getMethod());
      System.out.println("Inst: " + inst);
    }
    if (!inst.getBasicBlock().getInfrequent() && ref != null && ref.isRegister()) {
      Register reg = ref.asRegister().getRegister();
      if (verbose) { System.out.println("  Reg: " + reg); }
      if (reg.isSSA()) {
        //if (VM.VerifyAssertions) { VM._assert(!escapeSummary.isThreadLocal(result.getRegister())); }
        RegisterOperand def;
        Instruction defInst;
        String indent = "  ";
        do {
          RegisterOperandEnumeration defs = DefUse.defs(reg);
          def = defs.next();
          if (VM.VerifyAssertions) { VM._assert(!defs.hasMoreElements()); }
          defInst = def.instruction;
          if (Prologue.conforms(defInst)) {
            return null;
          }
          if (verbose) { System.out.println(indent + "Def instruction: " + defInst); }
          if (Move.conforms(defInst)) {
            OperandEnumeration uses = defInst.getUses();
            Operand use = uses.next();
            if (VM.VerifyAssertions) { VM._assert(!uses.hasMoreElements()); }
            if (use.isRegister() && use.asRegister().getRegister().isSSA()) {
              reg = use.asRegister().getRegister();
              indent += "  ";
              continue;
            }
          }
        } while (false);
        RegisterOperand indirectMetadata = indirectMetadataRegisterMap.get(def.getRegister());
        if (indirectMetadata == null) {
          RVMMethod target = Entrypoints.rdIndirectMetadataBarrierMethod;
          indirectMetadata = ir.regpool.makeTemp(TypeReference.JavaLangObject);
          Instruction barrier = Call.create2(CALL,
                                             indirectMetadata,
                                             IRTools.AC(target.getOffset()),
                                             MethodOperand.STATIC(target),
                                             def.copyRO(),
                                             samplingActivatedOperand.copy());
          barrier.bcIndex = RUNTIME_SERVICES_BCI;
          barrier.position = defInst.position;
          defInst.insertAfter(barrier);
          if (!RaceDet.checkHoisting()) {
            insertGetSamplingActivated(defInst, false);
          }
          if (!defInst.getBasicBlock().getInfrequent() &&
              RaceDet.inline()) {
            barriersToInline.add(barrier);
          }
          if (verbose) {
            System.out.println("Added barrier: " + barrier);
            ir.printInstructions();
          }
          indirectMetadataRegisterMap.put(def.getRegister(), indirectMetadata);
        }
        return indirectMetadata;
      }
    }
    return null;
  }
  
  /** insert and possibly inline a barrier method */
  private void insert(Instruction barrier, boolean isResolved, Instruction inst, IR ir, boolean before) {
    if (VM.VerifyAssertions) {
      RVMMethod target = Call.getMethod(barrier).getTarget();
      int numParams = target.getParameterTypes().length;
      if (Call.getNumberOfParams(barrier) != numParams) {
        System.out.println(barrier);
        System.out.println(inst);
        VM.sysFail("Bad match");
      }
    }
    
    barrier.bcIndex = RUNTIME_SERVICES_BCI;
    barrier.position = inst.position;
    if (before) {
      inst.insertBefore(barrier);
    } else {
      inst.insertAfter(barrier);
      // don't need to change next
    }
    // only inline into hot basic blocks
    if (!inst.getBasicBlock().getInfrequent() &&
        RaceDet.inline() &&
        isResolved) {
      // not sure why inst.position might be null
      if (inst.position != null) {
        barriersToInline.add(barrier);
      }
    }
  }
  
  /**
   * Inline a call instruction
   */
  private void inline(Instruction inst, IR ir, boolean noCalleeExceptions) {
    // Save and restore inlining control state.
    // Some options have told us to inline this runtime service,
    // so we have to be sure to inline it "all the way" not
    // just 1 level.
    boolean savedInliningOption = ir.options.INLINE;
    boolean savedExceptionOption = ir.options.H2L_NO_CALLEE_EXCEPTIONS;
    ir.options.INLINE = true;
    ir.options.H2L_NO_CALLEE_EXCEPTIONS = noCalleeExceptions;
    boolean savedOsrGI = ir.options.OSR_GUARDED_INLINING;
    ir.options.OSR_GUARDED_INLINING = false;
    try {
      InlineDecision inlDec =
          InlineDecision.YES(Call.getMethod(inst).getTarget(), "Expansion of runtime service");
      Inliner.execute(inlDec, ir, inst);
    } finally {
      ir.options.INLINE = savedInliningOption;
      ir.options.H2L_NO_CALLEE_EXCEPTIONS = savedExceptionOption;
      ir.options.OSR_GUARDED_INLINING = savedOsrGI;
    }
  }
  
  static final class FieldAccess {
    final FieldReference field;
    final boolean isRead;
    
    public FieldAccess(FieldReference field, boolean isRead) {
      this.field = field;
      this.isRead = isRead;
    }
    
    @Override
    public boolean equals(Object o) {
      FieldAccess other = (FieldAccess)o;
      return field == other.field && isRead == other.isRead;
    }
    
    @Override
    public int hashCode() {
      return field.hashCode() + (isRead ? 1 : 0);
    }
    
    @Override
    public String toString() {
      return field + (isRead ? " (read)" : " (write)");
    }
  }
  
  // RaceDet: LATER: synchronization operations should clear data-flow -- maybe not a problem since samplingActivated recomputed at yieldpoints?
  
  /** Compute redundant field reads and writes */
  @SuppressWarnings("unchecked")
  final void computeRedundantReadBarriers() {
    // first set all the scratch objects to empty sets
    for (BasicBlock bb = ir.lastBasicBlockInCodeOrder(); bb != null; bb = bb.prevBasicBlockInCodeOrder()) {
      bb.scratchObject = new HashMap<Register,HashSet<FieldAccess>>();
    }
    ir.cfg.exit().scratchObject = new HashMap<Register,HashSet<FieldAccess>>();

    // do data-flow
    HashMap<Register,HashSet<FieldAccess>> thisFullyRedSet = new HashMap<Register,HashSet<FieldAccess>>();
    boolean changed;
    int iteration = 0;
    do {
      if (verbose) { System.out.println("Beginning iteration " + iteration + " fullyRedInsts.size() = " + fullyRedInsts.size()); }
      iteration++;
      changed = false;
      for (BasicBlock bb = ir.firstBasicBlockInCodeOrder(); bb != null; bb = bb.nextBasicBlockInCodeOrder()) {
        // compute redundant variables for the bottom of the block
        // and merge with redundant variables
        thisFullyRedSet.clear();
        boolean first = true;
        for (BasicBlockEnumeration e = bb.getIn(); e.hasMoreElements(); ) {
          BasicBlock predBB = e.next();
          HashMap<Register,HashSet<FieldAccess>> predFullyRedSet = (HashMap<Register,HashSet<FieldAccess>>)predBB.scratchObject;
          if (first) {
            for (Register reg : predFullyRedSet.keySet()) {
              // gotta copy the set, not reused the same object
              HashSet<FieldAccess> fieldAccesses = new HashSet<FieldAccess>(predFullyRedSet.get(reg));
              thisFullyRedSet.put(reg, fieldAccesses);
            }
            first = false;
          } else {
            // intersection of field access sets
            for (Iterator<Register> iter = thisFullyRedSet.keySet().iterator(); iter.hasNext(); ) {
              Register reg = iter.next();
              if (predFullyRedSet.containsKey(reg)) {
                HashSet<FieldAccess> fieldAccesses = thisFullyRedSet.get(reg);
                fieldAccesses.retainAll(predFullyRedSet.get(reg));
              } else {
                iter.remove();
              }
            }
          }
        }

        // propagate info from top to bottom of block
        for (Instruction i = bb.firstInstruction(); !i.isBbLast(); i = i.nextInstructionInCodeOrder()) {
          // first look at RHS (since we're going forward)
          Operand useOperand = null;
          boolean isRead = false;
          FieldReference fieldRef = null;
          /*} else if (NewArray.conforms(i)) {
            useOperand = NewArray.getResult(i);*/
          if (GetField.conforms(i)) {
            useOperand = GetField.getRef(i);
            fieldRef = GetField.getLocation(i).asFieldAccess().getFieldRef();
            isRead = true;
          /*} else if (ALoad.conforms(i)) {
            useOperand = ALoad.getArray(i);*/
          } else if (PutField.conforms(i)) {
            useOperand = PutField.getRef(i);
            fieldRef = PutField.getLocation(i).asFieldAccess().getFieldRef();
            isRead = false;
          /*} else if (AStore.conforms(i)) {
            useOperand = AStore.getArray(i);*/
          }
          if (useOperand != null) {
            //if (VM.VerifyAssertions) { VM._assert(useOperand.isRegister() || useOperand.isConstant()); }
            if (useOperand.isRegister()) {
              Register useReg = useOperand.asRegister().register;
              FieldAccess fieldAccess = new FieldAccess(fieldRef, isRead);
              HashSet<FieldAccess> fieldAccesses = thisFullyRedSet.get(useReg);
              if (fieldAccesses == null) {
                fieldAccesses = new HashSet<FieldAccess>();
                thisFullyRedSet.put(useReg, fieldAccesses);
              }
              if (fieldAccesses.contains(fieldAccess)) {
                fullyRedInsts.add(i);
              } else {
                fieldAccesses.add(fieldAccess);
              }
            } else if (useOperand.isConstant()) {
              fullyRedInsts.add(i);
            } else {
              VM.sysFail("Weird operand: " + useOperand);
            }
          }
          // now look at LHS
          if (Move.conforms(i)) {
            Operand srcOperand = Move.getVal(i);
            if (srcOperand.isRegister()) {
              Register useReg = srcOperand.asRegister().register;
              Register defReg = Move.getResult(i).register;
              if (thisFullyRedSet.containsKey(useReg)) {
                HashSet<FieldAccess> useFieldAccesses = thisFullyRedSet.get(useReg);
                HashSet<FieldAccess> defFieldAccesses = thisFullyRedSet.get(defReg);
                if (defFieldAccesses == null) {
                  defFieldAccesses = new HashSet<FieldAccess>();
                  thisFullyRedSet.put(defReg, defFieldAccesses);
                }
                defFieldAccesses.addAll(useFieldAccesses);
              }
            }
          } else {
            // look at other defs
            for (OperandEnumeration e = i.getDefs(); e.hasMoreElements(); ) {
              Operand defOperand = e.next();
              if (defOperand.isRegister()) {
                Register defReg = defOperand.asRegister().register;
                thisFullyRedSet.remove(defReg);
              }
            }
          }
        }

        // compare what we've computed with what was already there
        HashMap<Register,HashSet<FieldAccess>> oldFullyRedSet = (HashMap<Register,HashSet<FieldAccess>>)bb.scratchObject;
        if (!oldFullyRedSet.equals(thisFullyRedSet)) {
          if (verbose) {
            System.out.println(bb);
            System.out.println("  Old: ");
            printRegAccessMap(oldFullyRedSet);
            System.out.println("  New: ");
            printRegAccessMap(thisFullyRedSet);
          }
          //if (VM.VerifyAssertions) { VM._assert(thisFullyRedSet.containsAll(oldFullyRedSet)); }
          oldFullyRedSet.clear();
          oldFullyRedSet.putAll(thisFullyRedSet);
          changed = true;
        }
      }
    } while (changed);

    // print graph
    //genGraph(ir, "redComp", partRedInsts, fullyRedInsts, needsBarrierMap, true);

    // clear the scratch objects
    for (BasicBlock bb = ir.lastBasicBlockInCodeOrder(); bb != null; bb = bb.prevBasicBlockInCodeOrder()) {
      bb.scratchObject = null;
    }
    ir.cfg.exit().scratchObject = null;
  }
  
  private void printRegAccessMap(HashMap<Register,HashSet<FieldAccess>> map) {
    for (Register reg : map.keySet()) {
      System.out.println("    Reg: " + reg);
      HashSet<FieldAccess> fieldAccesses = map.get(reg);
      for (FieldAccess fieldAccess : fieldAccesses) {
        System.out.println("      Field access: " + fieldAccess);
      }
    }
  }
}
