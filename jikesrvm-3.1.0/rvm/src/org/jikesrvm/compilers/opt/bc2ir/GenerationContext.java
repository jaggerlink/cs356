/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm.compilers.opt.bc2ir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.jikesrvm.ArchitectureSpecificOpt.RegisterPool;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.NormalMethod;
import org.jikesrvm.classloader.RVMType;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.compilers.baseline.BranchProfile;
import org.jikesrvm.compilers.baseline.BranchProfiles;
import org.jikesrvm.compilers.baseline.ConditionalBranchProfile;
import org.jikesrvm.compilers.baseline.EdgeCounts;
import org.jikesrvm.compilers.baseline.SwitchBranchProfile;
import org.jikesrvm.compilers.common.CompiledMethod;
import org.jikesrvm.compilers.opt.ClassLoaderProxy;
import org.jikesrvm.compilers.opt.OptOptions;
import org.jikesrvm.compilers.opt.OptimizingCompilerException;
import org.jikesrvm.compilers.opt.inlining.InlineOracle;
import org.jikesrvm.compilers.opt.inlining.InlineSequence;
import org.jikesrvm.compilers.opt.ir.BasicBlock;
import org.jikesrvm.compilers.opt.ir.BasicBlockEnumeration;
import org.jikesrvm.compilers.opt.ir.Call;
import org.jikesrvm.compilers.opt.ir.ControlFlowGraph;
import org.jikesrvm.compilers.opt.ir.Empty;
import org.jikesrvm.compilers.opt.ir.ExceptionHandlerBasicBlock;
import org.jikesrvm.compilers.opt.ir.ExceptionHandlerBasicBlockBag;
import org.jikesrvm.compilers.opt.ir.IRTools;
import org.jikesrvm.compilers.opt.ir.Instruction;
import org.jikesrvm.compilers.opt.ir.MonitorOp;
import org.jikesrvm.compilers.opt.ir.Move;
import org.jikesrvm.compilers.opt.ir.Nullary;
import org.jikesrvm.compilers.opt.ir.Operators;
import org.jikesrvm.compilers.opt.ir.Prologue;
import org.jikesrvm.compilers.opt.ir.Register;
import org.jikesrvm.compilers.opt.ir.Return;
import org.jikesrvm.compilers.opt.ir.operand.AddressConstantOperand;
import org.jikesrvm.compilers.opt.ir.operand.BranchProfileOperand;
import org.jikesrvm.compilers.opt.ir.operand.ClassConstantOperand;
import org.jikesrvm.compilers.opt.ir.operand.MethodOperand;
import org.jikesrvm.compilers.opt.ir.operand.Operand;
import org.jikesrvm.compilers.opt.ir.operand.RegisterOperand;
import org.jikesrvm.compilers.opt.ir.operand.TrueGuardOperand;
import org.jikesrvm.compilers.opt.ir.operand.TypeOperand;
import org.jikesrvm.rd.RaceDet;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.runtime.Statics;
import org.vmmagic.unboxed.Offset;

/**
 * Defines the context in which BC2IR will abstractly interpret
 * a method's bytecodes and populate targetIR with instructions.
 *
 **/
public final class GenerationContext implements org.jikesrvm.compilers.opt.driver.OptConstants, Operators {

  //////////
  // These fields are used to communicate information from its
  // caller to BC2IR
  //////////
  /**
   * The original method (root of the calling context tree)
   */
  NormalMethod original_method;

  /**
   * The compiled method assigned for this compilation of original_method
   */
  CompiledMethod original_cm;

  /**
   * The method to be generated
   */
  public NormalMethod method;

  /**
   * The BranchProfile data for method, if available
   */
  BranchProfiles branchProfiles;

  /**
   * The options to control the generation
   */
  public OptOptions options;

  /**
   * The CFG object into which instructions should be generated.
   */
  public ControlFlowGraph cfg;

  /**
   * The register pool to be used during generation
   */
  public RegisterPool temps;

  /**
   * The parameters which BC2IR should use to seed the local state
   * for the entry basic block.
   */
  Operand[] arguments;

  /**
   * The basic block into which BC2IR's caller will generate a "prologue."
   * BC2IR will add a CFG edge from prologue to the block containing the
   * instructions generated for bytecode 0, but it is its caller's
   * responsibility to populate the prologue with instructions.
   * All blocks generated by BC2IR will be injected by BC2IR.doFinalPass
   * immediately
   * after prologue in the code ordering
   * (ie prologue can assume it will fallthrough
   * to the first basic block in the ir generated for method.
   */
  public BasicBlock prologue;

  /**
   * The basic block into which BC2IR's caller will generate an epilogue.
   * BC2IR will add CFG edges to this node, but it is its caller's
   * responsibility to populate it with instructions.
   * NOTE: After IR is generated one of two conditions will hold:
   * <ul>
   * <li> epilogue == cfg.lastInCodeOrder():  (if it is to be inlined,
   *                                           then the generated cfg
   *                                           is expecting to "fallthrough"
   *                                           to the next bblock)
   * <li> epilogue == null:  implies that there is no "normal" exit from
   *                         the callee (all exits via throw)
   * </ul>
   * NOTE: BC2IR assumes that epilogue is a single basic block
   *       (ie it has no out edges)
   */
  public BasicBlock epilogue;

  /**
   * The exit node of the outermost CFG
   * (used by BC2IR for not-definitely caught athrows and by OSR_Yieldpoints)
   */
  public BasicBlock exit;

  /**
   * A catch, unlock, and rethrow exception handler used for
   * synchronized methods.
   */
  BasicBlock unlockAndRethrow;

  /**
   * The Register to which BC2IR should assign the return value(s)
   * of the method. It will be null when the method has a void return.
   */
  Register resultReg;

  /**
   * The enclosing exception handlers (null if there are none).
   */
  ExceptionHandlerBasicBlockBag enclosingHandlers;

  /**
   * Inlining context of the method to be generated
   */
  public InlineSequence inlineSequence;

  /**
   * The InlineOracle to be consulted for all inlining decisions during
   * the generation of this IR.
   */
  InlineOracle inlinePlan;

  //////////
  // These fields are used to communicate information from BC2IR to its caller
  //////////
  /**
   * Did BC2IR generate a reachable exception handler while generating
   * the IR for this method
   */
  public boolean generatedExceptionHandlers;

  /**
   * Did BC2IR encounter a magic that requires us to allocate a stack frame?
   */
  public boolean allocFrame;

  /**
   * Used to communicate the meet of the return values back to the caller
   * Mainly useful when BC2IR is doing inlining....allows the caller
   * BC2IR object
   * to exploit knowledge the callee BC2IR object had about the result.
   */
  public Operand result;
  /**
   * Do we do check stores?
   */
  boolean doesCheckStore;

  //////////
  // Main public methods
  /////////

  /**
   * Use this constructor to create an outermost (non-inlined)
   * GenerationContext.
   *
   * @param meth The NormalMethod whose IR will be generated
   * @param params The known types of the parameters to the method. For method specialization.
   * @param cm   The compiled method id to be used for this compilation
   * @param opts The Options to be used for the generation
   * @param ip   The InlineOracle to be used for the generation
   */
  GenerationContext(NormalMethod meth, TypeReference[] params, CompiledMethod cm, OptOptions opts, InlineOracle ip) {
    original_method = meth;
    original_cm = cm;
    method = meth;
    if (opts.frequencyCounters() || opts.inverseFrequencyCounters()) {
      branchProfiles = EdgeCounts.getBranchProfiles(meth);
    }
    options = opts;
    inlinePlan = ip;
    inlineSequence = new InlineSequence(meth);
    doesCheckStore = !meth.hasNoCheckStoreAnnotation();

    // Create the CFG. Initially contains prologue, epilogue, and exit.
    cfg = new ControlFlowGraph(0);
    prologue = new BasicBlock(PROLOGUE_BLOCK_BCI, inlineSequence, cfg);
    epilogue = new BasicBlock(EPILOGUE_BLOCK_BCI, inlineSequence, cfg);
    cfg.addLastInCodeOrder(prologue);
    cfg.addLastInCodeOrder(epilogue);
    exit = cfg.exit();
    epilogue.insertOut(exit);

    // Create register pool, initialize arguments, resultReg.
    temps = new RegisterPool(meth);
    _ncGuards = new HashMap<Register, RegisterOperand>();
    initLocalPool();
    TypeReference[] definedParams = meth.getParameterTypes();
    if (params == null) params = definedParams;
    int numParams = params.length;
    int argIdx = 0;
    int localNum = 0;
    arguments = new Operand[method.isStatic() ? numParams : numParams + 1];
    // Insert IR_PROLOGUE instruction.  Loop below will fill in its operands
    Instruction prologueInstr = Prologue.create(IR_PROLOGUE, arguments.length);
    appendInstruction(prologue, prologueInstr, PROLOGUE_BCI);

    if (!method.isStatic()) {
      TypeReference thisType = meth.getDeclaringClass().getTypeRef();
      RegisterOperand thisOp = makeLocal(localNum, thisType);
      // The this param of a virtual method is by definition non null
      RegisterOperand guard = makeNullCheckGuard(thisOp.getRegister());
      BC2IR.setGuard(thisOp, guard);
      appendInstruction(prologue, Move.create(GUARD_MOVE, guard.copyRO(), new TrueGuardOperand()), PROLOGUE_BCI);
      thisOp.setDeclaredType();
      thisOp.setExtant();
      if (method.getDeclaringClass().isFinal()) {
        thisOp.setPreciseType();
      }
      arguments[0] = thisOp;
      Prologue.setFormal(prologueInstr, 0, thisOp.copyU2D());
      argIdx++;
      localNum++;
    }
    for (int paramIdx = 0; paramIdx < numParams; paramIdx++) {
      TypeReference argType = params[paramIdx];
      RegisterOperand argOp = makeLocal(localNum, argType);
      argOp.setDeclaredType();
      if (argType.isClassType()) {
        argOp.setExtant();
      }
      arguments[argIdx] = argOp;
      Prologue.setFormal(prologueInstr, argIdx, argOp.copyU2D());
      argIdx++;
      localNum++;
      if (argType.isLongType() || argType.isDoubleType()) {
        localNum++; // longs & doubles take two words of local space
      }
    }
    TypeReference returnType = meth.getReturnType();
    if (returnType != TypeReference.Void) {
      resultReg = temps.makeTemp(returnType).getRegister();
    }

    enclosingHandlers = null;

    completePrologue(true);
    completeEpilogue(true);
    completeExceptionHandlers(true);
  }

  /**
   * Create a child generation context from parent & callerBB to
   * generate IR for callsite.
   * Make this 'static' to avoid confusing parent/child fields.
   *
   * @param parent the parent gc
   * @param ebag the enclosing exception handlers (null if none)
   * @param callee the callee method to be inlined
   *        (may _not_ be equal to Call.getMethod(callSite).method)
   * @param callSite the Call instruction to be inlined.
   * @return the child context
   */
  public static GenerationContext createChildContext(GenerationContext parent, ExceptionHandlerBasicBlockBag ebag,
                                                  NormalMethod callee, Instruction callSite) {
    GenerationContext child = new GenerationContext();
    child.method = callee;
    if (parent.options.frequencyCounters() || parent.options.inverseFrequencyCounters()) {
      child.branchProfiles = EdgeCounts.getBranchProfiles(callee);
    }
    child.original_method = parent.original_method;
    child.original_cm = parent.original_cm;

    // Some state gets directly copied to the child
    child.options = parent.options;
    child.temps = parent.temps;
    child._ncGuards = parent._ncGuards;
    child.exit = parent.exit;
    child.inlinePlan = parent.inlinePlan;

    // Now inherit state based on callSite
    child.inlineSequence = new InlineSequence(child.method, callSite.position, callSite);
    child.enclosingHandlers = ebag;
    child.arguments = new Operand[Call.getNumberOfParams(callSite)];
    for (int i = 0; i < child.arguments.length; i++) {
      child.arguments[i] = Call.getParam(callSite, i).copy(); // copy instead
      // of clearing in case inlining aborts.
    }
    if (Call.hasResult(callSite)) {
      child.resultReg = Call.getResult(callSite).copyD2D().getRegister();
      child.resultReg.setSpansBasicBlock(); // it will...
    }

    // Initialize the child CFG, prologue, and epilogue blocks
    child.cfg = new ControlFlowGraph(parent.cfg.numberOfNodes());
    child.prologue = new BasicBlock(PROLOGUE_BCI, child.inlineSequence, child.cfg);
    child.prologue.exceptionHandlers = ebag;
    child.epilogue = new BasicBlock(EPILOGUE_BCI, child.inlineSequence, child.cfg);
    child.epilogue.exceptionHandlers = ebag;
    child.cfg.addLastInCodeOrder(child.prologue);
    child.cfg.addLastInCodeOrder(child.epilogue);

    // Set up the local pool
    child.initLocalPool();

    // Insert moves from child.arguments to child's locals in prologue
    TypeReference[] params = child.method.getParameterTypes();
    int numParams = params.length;
    int argIdx = 0;
    int localNum = 0;
    if (!child.method.isStatic()) {
      Operand receiver = child.arguments[argIdx];
      argIdx++;
      RegisterOperand local = null;
      if (receiver.isRegister()) {
        RegisterOperand objPtr = receiver.asRegister();
        if (ClassLoaderProxy.includesType(child.method.getDeclaringClass().getTypeRef(), objPtr.getType()) != YES) {
          // narrow type of actual to match formal static type implied by method
          objPtr.clearPreciseType(); // Can be precise but not assignable if enough classes aren't loaded
          objPtr.setDeclaredType();
          objPtr.setType(child.method.getDeclaringClass().getTypeRef());
        }
        local = child.makeLocal(localNum, objPtr);
        localNum++;
        child.arguments[0] = local; // Avoid confusion in BC2IR of callee
        // when objPtr is a local in the caller.
      } else if (receiver.isConstant()) {
        local = child.makeLocal(localNum, receiver.getType());
        localNum++;
        local.setPreciseType();
        // Constants trivially non-null
        RegisterOperand guard = child.makeNullCheckGuard(local.getRegister());
        BC2IR.setGuard(local, guard);
        child.prologue.appendInstruction(Move.create(GUARD_MOVE, guard.copyRO(), new TrueGuardOperand()));
      } else {
        OptimizingCompilerException.UNREACHABLE("Unexpected receiver operand");
      }
      Instruction s = Move.create(REF_MOVE, local, receiver);
      s.bcIndex = PROLOGUE_BCI;
      s.position = callSite.position;
      child.prologue.appendInstruction(s);
    }
    for (int paramIdx = 0; paramIdx < numParams; paramIdx++, argIdx++) {
      TypeReference argType = params[paramIdx];
      RegisterOperand formal;
      Operand actual = child.arguments[argIdx];
      if (actual.isRegister()) {
        RegisterOperand rActual = actual.asRegister();
        if (ClassLoaderProxy.includesType(argType, rActual.getType()) != YES) {
          // narrow type of actual to match formal static type implied by method
          rActual.clearPreciseType(); // Can be precise but not
          // assignable if enough classes aren't loaded
          rActual.setDeclaredType();
          rActual.setType(argType);
        }
        formal = child.makeLocal(localNum, rActual);
        localNum++;
        child.arguments[argIdx] = formal;  // Avoid confusion in BC2IR of
        // callee when arg is a local in the caller.
      } else {
        formal = child.makeLocal(localNum, argType);
        localNum++;
      }
      Instruction s = Move.create(IRTools.getMoveOp(argType), formal, actual);
      s.bcIndex = PROLOGUE_BCI;
      s.position = callSite.position;
      child.prologue.appendInstruction(s);
      if (argType.isLongType() || argType.isDoubleType()) {
        localNum++; // longs and doubles take two local words
      }
    }

    child.completePrologue(false);
    child.completeEpilogue(false);
    child.completeExceptionHandlers(false);

    return child;
  }

  /**
   * Only for internal use by Inliner (when inlining multiple targets)
   * This is probably not the prettiest way to handle this, but it requires
   * no changes to BC2IR's & Inliner's high level control logic.
   *
   * @param parent the parent gc
   * @param ebag the enclosing exception handlers (null if none)
   * @return the synthetic context
   */
  public static GenerationContext createSynthetic(GenerationContext parent, ExceptionHandlerBasicBlockBag ebag) {
    // Create the CFG. Initially contains prologue and epilogue
    GenerationContext child = new GenerationContext();

    child.cfg = new ControlFlowGraph(-100000);

    // It may be wrong to use the parent inline sequence as the
    // position here, but it seems to work out.  This is a synthetic
    // context that is just used as a container for multiple inlined
    // targets, so in the cases that I've observed where the prologue
    // and epilogue don't disappear, it was correct to have the
    // parent's position. -- Matt
    child.prologue = new BasicBlock(PROLOGUE_BCI, parent.inlineSequence, parent.cfg);
    child.prologue.exceptionHandlers = ebag;
    child.epilogue = new BasicBlock(EPILOGUE_BCI, parent.inlineSequence, parent.cfg);
    child.epilogue.exceptionHandlers = ebag;
    child.cfg.addLastInCodeOrder(child.prologue);
    child.cfg.addLastInCodeOrder(child.epilogue);

    // All other fields are intentionally left null.
    // We are only really using this context to transfer a synthetic CFG
    // from the low-level Inliner.execute back to its caller.
    // TODO: Rewrite GenerationContext to be a subclass of a root
    // class that is just a CFG wrapper.  Then, have an instance of this
    // new parent
    // class be the return value for the main entrypoints in Inliner
    // and create an instance of the root class instead of GC when
    // inlining multiple targets.

    return child;
  }

  /**
   * Use this to transfer state back from a child context back to its parent.
   *
   * @param parent the parent context that will receive the state
   * @param child  the child context from which the state will be taken
   */
  public static void transferState(GenerationContext parent, GenerationContext child) {
    parent.cfg.setNumberOfNodes(child.cfg.numberOfNodes());
    if (child.generatedExceptionHandlers) {
      parent.generatedExceptionHandlers = true;
    }
    if (child.allocFrame) {
      parent.allocFrame = true;
    }
  }

  ///////////
  // Local variables
  ///////////

  // The registers to use for various types of locals.
  // Note that "int" really means 32-bit gpr.
  private Register[] intLocals;
  private Register[] addressLocals;
  private Register[] floatLocals;
  private Register[] longLocals;
  private Register[] doubleLocals;

  private void initLocalPool() {
    int numLocals = method.getLocalWords();
    intLocals = new Register[numLocals];
    addressLocals = new Register[numLocals];
    floatLocals = new Register[numLocals];
    longLocals = new Register[numLocals];
    doubleLocals = new Register[numLocals];
  }

  private Register[] getPool(TypeReference type) {
    if (type == TypeReference.Float) {
      return floatLocals;
    } else if (type == TypeReference.Long) {
      return longLocals;
    } else if (type == TypeReference.Double) {
      return doubleLocals;
    } else if (type.isReferenceType() || type.isWordType()) {
      return addressLocals;
    } else {
      return intLocals;
    }
  }

  /**
   * Return the Register used to for local i of TypeReference type
   */
  public Register localReg(int i, TypeReference type) {
    Register[] pool = getPool(type);
    if (pool[i] == null) {
      pool[i] = temps.getReg(type);
      pool[i].setLocal();
    }
    return pool[i];
  }

  /**
   * Should null checks be generated?
   */
  boolean noNullChecks() {
    return method.hasNoNullCheckAnnotation();
  }

  /**
   * Should bounds checks be generated?
   */
  boolean noBoundsChecks() {
    return method.hasNoBoundsCheckAnnotation();
  }

  /**
   * Make a register operand that refers to the given local variable number
   * and has the given type.
   *
   * @param i local variable number
   * @param type desired data type
   */
  public RegisterOperand makeLocal(int i, TypeReference type) {
    return new RegisterOperand(localReg(i, type), type);
  }

  /**
   * Make a register operand that refers to the given local variable number,
   * and inherits its properties (type, flags) from props
   *
   * @param i local variable number
   * @param props RegisterOperand to inherit flags from
   */
  RegisterOperand makeLocal(int i, RegisterOperand props) {
    RegisterOperand local = makeLocal(i, props.getType());
    local.setInheritableFlags(props);
    BC2IR.setGuard(local, BC2IR.getGuard(props));
    return local;
  }

  /**
   * Get the local number for a given register
   */
  public int getLocalNumberFor(Register reg, TypeReference type) {
    Register[] pool = getPool(type);
    for (int i = 0; i < pool.length; i++) {
      if (pool[i] == reg) return i;
    }
    return -1;
  }

  /**
   * Is the operand a particular bytecode local?
   */
  public boolean isLocal(Operand op, int i, TypeReference type) {
    if (op instanceof RegisterOperand) {
      if (getPool(type)[i] == ((RegisterOperand) op).getRegister()) return true;
    }
    return false;
  }

  ///////////
  // Validation operands (guards)
  ///////////

  // For each register, we always use the same register as a validation operand.
  // This helps us avoid needlessly losing information at CFG join points.
  private HashMap<Register, RegisterOperand> _ncGuards;

  /**
   * Make a register operand to use as a null check guard for the
   * given register.
   */
  RegisterOperand makeNullCheckGuard(Register ref) {
    RegisterOperand guard = _ncGuards.get(ref);
    if (guard == null) {
      guard = temps.makeTempValidation();
      _ncGuards.put(ref, guard.copyRO());
    } else {
      guard = guard.copyRO();
    }
    return guard;
  }

  ///////////
  // Profile data
  ///////////
  public BranchProfileOperand getConditionalBranchProfileOperand(int bcIndex, boolean backwards) {
    float prob;
    if (branchProfiles != null) {
      BranchProfile bp = branchProfiles.getEntry(bcIndex);
      prob = ((ConditionalBranchProfile) bp).getTakenProbability();
    } else if (backwards) {
      prob = 0.9f;
    } else {
      prob = 0.5f;
    }
    // experimental option: flip the probablity to see how bad things would be if
    // we were completely wrong.
    if (options.inverseFrequencyCounters()) {
      prob = 1f - prob;
    }
    return new BranchProfileOperand(prob);
  }

  public SwitchBranchProfile getSwitchProfile(int bcIndex) {
    if (branchProfiles != null) {
      BranchProfile bp = branchProfiles.getEntry(bcIndex);
      return (SwitchBranchProfile) bp;
    } else {
      return null;
    }
  }

  ///////////
  // Implementation
  ///////////

  /**
   * for internal use only (in createInlinedContext)
   */
  private GenerationContext() {}

  /**
   * Fill in the rest of the method prologue.
   * PRECONDITION: arguments & temps have been setup/initialized.
   */
  private void completePrologue(boolean isOutermost) {
    // Deal with Uninteruptible code.
    if (!isOutermost && requiresUnintMarker()) {
      Instruction s = Empty.create(UNINT_BEGIN);
      appendInstruction(prologue, s, PROLOGUE_BCI);
    }

    // Deal with implicit monitorenter for synchronized methods.
    // When working with the class writer do not expand static
    // synchronization headers as there is no easy way to get at
    // class object

    // OSR: if this is a specialized method, no monitor enter at the beginging
    // since it's the second time reenter
    if (method.isForOsrSpecialization()) {
      // do nothing
    } else if (method.isSynchronized() && !options.ESCAPE_INVOKEE_THREAD_LOCAL) {
      Operand lockObject = getLockObject();
      Instruction s = MonitorOp.create(MONITORENTER, lockObject, new TrueGuardOperand());
      appendInstruction(prologue, s, SYNCHRONIZED_MONITORENTER_BCI);
    }
  }

  /**
   * Fill in the rest of the method epilogue.
   * PRECONDITION: arguments & temps have been setup/initialized.
   */
  private void completeEpilogue(boolean isOutermost) {
    // Deal with implicit monitorexit for synchronized methods.
    if (method.isSynchronized() && !options.ESCAPE_INVOKEE_THREAD_LOCAL) {
      Operand lockObject = getLockObject();
      Instruction s = MonitorOp.create(MONITOREXIT, lockObject, new TrueGuardOperand());
      appendInstruction(epilogue, s, SYNCHRONIZED_MONITOREXIT_BCI);
    }

    // Deal with Uninteruptible code.
    if (!isOutermost && requiresUnintMarker()) {
      Instruction s = Empty.create(UNINT_END);
      appendInstruction(epilogue, s, EPILOGUE_BCI);
    }

    if (isOutermost) {
      TypeReference returnType = method.getReturnType();
      Operand retVal = returnType.isVoidType() ? null : new RegisterOperand(resultReg, returnType);
      Instruction s = Return.create(RETURN, retVal);
      appendInstruction(epilogue, s, EPILOGUE_BCI);
    }
  }

  /**
   * If the method is synchronized then we wrap it in a
   * synthetic exception handler that unlocks & rethrows
   * PRECONDITION: cfg, arguments & temps have been setup/initialized.
   */
  private void completeExceptionHandlers(boolean isOutermost) {
    if (method.isSynchronized() && !options.ESCAPE_INVOKEE_THREAD_LOCAL) {
      ExceptionHandlerBasicBlock rethrow =
          new ExceptionHandlerBasicBlock(SYNTH_CATCH_BCI,
                                             inlineSequence,
                                             new TypeOperand(RVMType.JavaLangThrowableType),
                                             cfg);
      rethrow.exceptionHandlers = enclosingHandlers;
      RegisterOperand ceo = temps.makeTemp(TypeReference.JavaLangThrowable);
      Instruction s = Nullary.create(GET_CAUGHT_EXCEPTION, ceo);
      appendInstruction(rethrow, s, SYNTH_CATCH_BCI);
      Operand lockObject = getLockObject();

      // RaceDet: decide whether to use instrumented unlock
      RVMMethod target;
      if (RaceDet.instrumentSyncOps(method)) {
        target = Entrypoints.unlockAndThrowMethod;
      } else {
        target = Entrypoints.unlockAndThrowWithoutRaceDetInstrMethod;
      }
      MethodOperand methodOp = MethodOperand.STATIC(target);
      methodOp.setIsNonReturningCall(true); // Used to keep cfg correct
      s =
          Call.create2(CALL,
                       null,
                       new AddressConstantOperand(target.getOffset()),
                       methodOp,
                       lockObject,
                       ceo.copyD2U());
      appendInstruction(rethrow, s, RUNTIME_SERVICES_BCI);

      cfg.insertBeforeInCodeOrder(epilogue, rethrow);

      // May be overly conservative
      // (if enclosed by another catch of Throwable...)
      if (enclosingHandlers != null) {
        for (BasicBlockEnumeration e = enclosingHandlers.enumerator(); e.hasMoreElements();) {
          BasicBlock eh = e.next();
          rethrow.insertOut(eh);
        }
      }
      rethrow.setCanThrowExceptions();
      rethrow.setMayThrowUncaughtException();
      rethrow.insertOut(exit);

      // save a reference to this block so we can discard it if unused.
      unlockAndRethrow = rethrow;

      ExceptionHandlerBasicBlock[] sh = new ExceptionHandlerBasicBlock[1];
      sh[0] = rethrow;
      enclosingHandlers = new ExceptionHandlerBasicBlockBag(sh, enclosingHandlers);
      generatedExceptionHandlers = true;
    }
  }

  /**
   * Get the object for locking for synchronized methods.
   * either the class object or the this ptr.
   */
  private Operand getLockObject() {
    if (method.isStatic()) {
      Class<?> klass = method.getDeclaringClass().getClassForType();
      Offset offs = Offset.fromIntSignExtend(Statics.findOrCreateObjectLiteral(klass));
      return new ClassConstantOperand(klass, offs);
    } else {
      return makeLocal(0, arguments[0].getType());
    }
  }

  private void appendInstruction(BasicBlock b, Instruction s, int bcIndex) {
    s.position = inlineSequence;
    s.bcIndex = bcIndex;
    b.appendInstruction(s);
  }

  private boolean requiresUnintMarker() {
    if (method.isInterruptible()) return false;

    // supress redundant markers by detecting when we're inlining
    // one Uninterruptible method into another one.
    for (InlineSequence p = inlineSequence.getCaller(); p != null; p = p.getCaller()) {
      if (!p.getMethod().isInterruptible()) return false;
    }

    return true;
  }

  /**
   * Make sure, the gc is still in sync with the IR, even if we applied some
   * optimizations. This method should be called before hir2lir conversions
   * which might trigger inlining.
   */
  public void resync() {
    //make sure the _ncGuards contain no dangling mappings
    resync_ncGuards();
  }

  /**
   * This method makes sure that _ncGuard only maps to registers that
   * are actually in the IRs register pool.
   */
  private void resync_ncGuards() {
    HashSet<Register> regPool = new HashSet<Register>();

    for (Register r = temps.getFirstSymbolicRegister(); r != null; r = r.getNext()) {
      regPool.add(r);
    }

    Iterator<Map.Entry<Register, RegisterOperand>> i = _ncGuards.entrySet().iterator();
    while (i.hasNext()) {
      Map.Entry<Register, RegisterOperand> entry = i.next();
      if (!(regPool.contains(entry.getValue()))) i.remove();
    }
  }

  /**
   * Kill ncGuards, so we do not use outdated mappings unintendedly later on
   */
  public void close() {
    _ncGuards = null;
  }

}

