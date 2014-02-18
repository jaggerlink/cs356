package org.jikesrvm.rd;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.FieldReference;
import org.jikesrvm.classloader.NormalMethod;
import org.jikesrvm.classloader.RVMField;
import org.jikesrvm.compilers.opt.ir.Instruction;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.scheduler.SpinLock;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;

/** RaceDet: primary class for race detection */
@Uninterruptible
public final class RaceDet {

  public static final SpinLock outputLock = new SpinLock();
  
  @Interruptible
  public static final void boot() {
    Site.boot();
    Sampling.boot();
    Races.boot();
    Stats.boot();
  }
  
  /** Be conservative since the field is unresolved */
  public static final boolean mightHaveFieldMetadata(FieldReference fieldRef, boolean isStatic) {
    //if (VM.VerifyAssertions) { VM._assert(!fieldRef.isResolved()); }
    if (fieldMetadata()) {
      return !fieldRef.getType().getName().isRVMDescriptor();
    } else {
      return false;
    }
  }

  public static final boolean shouldAddSomeFieldMetadata(RVMField field) {
    return addDirectFieldMetadata(field) || addIndirectFieldMetadata(field);
  }
  
  public static final boolean addDirectFieldMetadata(RVMField field) {
    if (field.isFinal()) {
      return false;
    }
    if ((field.isVolatile() && directFieldMetadataVolatiles()) ||
        (field.isStatic() && directFieldMetadataStatics()) ||
        directFieldMetadataRegulars()) {
      return !field.getMemberRef().asFieldReference().getType().getName().isRVMDescriptor();
    } else {
      return false;
    }
  }
  
  public static final boolean addIndirectFieldMetadata(RVMField field) {
    if (field.isFinal()) {
      return false;
    }
    if (indirectFieldMetadataRegulars() &&
        !field.isVolatile() &&
        !field.isStatic()) {
      return !field.getMemberRef().asFieldReference().getType().getName().isRVMDescriptor();
    } else {
      return false;
    }
  }
  
  @Inline
  public static final boolean addSomeBarriers(NormalMethod method) {
    return someBarriers() && barrierApplicable(method);
  }
  
  @Inline
  public static final boolean addPreChecks(NormalMethod method) {
    return passIndirectMetadata() && barrierApplicable(method);
  }
  
  @Inline
  public static final boolean instrumentSyncOps(NormalMethod method) {
    return syncOps() && syncOpsApplicable(method);
  }
  
  private static final boolean syncOpsApplicable(NormalMethod method) {
    Atom desc = method.getDeclaringClass().getDescriptor();
    // handle weird L$Proxy2; classes
    if (desc.getBytes()[1] != '$') {
      return !desc.isRVMDescriptor();
    }
    return false;
    //return barrierApplicable(method) || desc.isConcurrentDescriptor();
  }
  
  private static final boolean barrierApplicable(NormalMethod method) {
    // for now, just put instrumentation in the application (not the libraries)
    Atom desc = method.getDeclaringClass().getDescriptor();
    // handle weird L$Proxy2; classes
    //if (VM.VerifyAssertions) { VM._assert(desc.getBytes()[1] != '$'); }
    if (desc.getBytes()[1] != '$') {
      boolean app = !desc.isBootstrapClassDescriptor();
      if (app && VM.runningVM && VM.fullyBooted) {
        return true;
      }
    }
    return false;
  }
  
  /*
  @Interruptible
  public static final boolean addBarriers(Instruction inst) {
    return addBarriers(inst.position.getMethod());
  }
  */
  
  @Interruptible
  public static final NormalMethod getAlgoBarrier(Instruction inst, boolean isRead, boolean isField, boolean isResolved, boolean isVolatile, boolean isStatic) {
    NormalMethod method = inst.position.getMethod();

    if (VM.raceDetNoOpt) { return null; }
    
    // use special sampling barriers for opt compiler
    return getAlgoBarrierHelper(method, isRead, isField, isResolved, isVolatile, isStatic, true);
  }

  @Interruptible
  public static final NormalMethod getAlgoBarrier(NormalMethod method, boolean isRead, boolean isField, boolean isResolved, boolean isVolatile, boolean isStatic) {

    if (VM.raceDetNoBase) { return null; }

    // use non-sampling barriers for base compiler (i.e., just check sampling state inside barriers)
    return getAlgoBarrierHelper(method, isRead, isField, isResolved, isVolatile, isStatic, false);
  }

  @Interruptible
  private static final NormalMethod getAlgoBarrierHelper(NormalMethod method, boolean isRead, boolean isField, boolean isResolved, boolean isVolatile, boolean isStatic, boolean useSampling) {
    if (addSomeBarriers(method)) {
      // should we insert volatile barriers only?
      if (!algoBarriers()) {
        // the only possibility for volatiles is known volatiles and unresolved fields
        if (!(isVolatile || (isField && !isResolved))) {
          return null;
        }
      }
      // should we insert static barriers only
      if (staticsOnly()) {
        if (!isStatic) {
          return null;
        }
      }
      if (isField) {
        if (isRead) {
          if (isResolved) {
            if (isVolatile) {
              if (isStatic) {
                return Entrypoints.rdFieldReadBarrierStaticResolvedVolatileMethod;
              } else {
                return Entrypoints.rdFieldReadBarrierResolvedVolatileMethod;
              }
            } else {
              if (isStatic) {
                if (useSampling) {
                  if (stats()) {
                    return Entrypoints.rdFieldReadBarrierStaticResolvedSamplingStatsMethod;
                  } else {
                    return Entrypoints.rdFieldReadBarrierStaticResolvedSamplingMethod;
                  }
                } else {
                  return Entrypoints.rdFieldReadBarrierStaticResolvedMethod;
                }
              } else {
                if (useSampling) {
                  if (stats()) {
                    return Entrypoints.rdFieldReadBarrierResolvedSamplingStatsMethod;
                  } else {
                    return Entrypoints.rdFieldReadBarrierResolvedSamplingMethod;
                  }
                } else {
                  return Entrypoints.rdFieldReadBarrierResolvedMethod;
                }
              }
            }
          } else {
            if (isStatic) {
              return Entrypoints.rdFieldReadBarrierStaticUnresolvedMethod;
            } else {
              return Entrypoints.rdFieldReadBarrierUnresolvedMethod;
            }
          }
        } else {
          if (isResolved) {
            if (isVolatile) {
              if (isStatic) {
                return Entrypoints.rdFieldWriteBarrierStaticResolvedVolatileMethod;
              } else {
                return Entrypoints.rdFieldWriteBarrierResolvedVolatileMethod;
              }
            } else {
              if (isStatic) {
                if (useSampling) {
                  if (stats()) {
                    return Entrypoints.rdFieldWriteBarrierStaticResolvedSamplingStatsMethod;
                  } else {
                    return Entrypoints.rdFieldWriteBarrierStaticResolvedSamplingMethod;
                  }
                } else {
                  return Entrypoints.rdFieldWriteBarrierStaticResolvedMethod;
                }
              } else {
                if (useSampling) {
                  if (stats()) {
                    return Entrypoints.rdFieldWriteBarrierResolvedSamplingStatsMethod;
                  } else {
                    return Entrypoints.rdFieldWriteBarrierResolvedSamplingMethod;
                  }
                } else {
                  return Entrypoints.rdFieldWriteBarrierResolvedMethod;
                }
              }
            }
          } else {
            if (isStatic) {
              return Entrypoints.rdFieldWriteBarrierStaticUnresolvedMethod;
            } else {
              return Entrypoints.rdFieldWriteBarrierUnresolvedMethod;
            }
          }
        }
      } else {
        if (RaceDet.arrays()) {
          if (VM.VerifyAssertions) { VM._assert(isResolved && !isVolatile); }
          if (isRead) {
            if (useSampling) {
              if (stats()) {
                return Entrypoints.rdArrayReadBarrierSamplingStatsMethod;
              } else {
                return Entrypoints.rdArrayReadBarrierSamplingMethod;
              }
            } else {
              return Entrypoints.rdArrayReadBarrierMethod;
            }
          } else {
            if (useSampling) {
              if (stats()) {
                return Entrypoints.rdArrayWriteBarrierSamplingStatsMethod;
              } else {
                return Entrypoints.rdArrayWriteBarrierSamplingMethod;
              }
            } else {
              return Entrypoints.rdArrayWriteBarrierMethod;
            }
          }
        } else {
          return null;
        }
      }
    } else {
      return null;
    }
  }
  
  public static final NormalMethod getBarrierUsePassedMetadata(NormalMethod current) {
    if (current == Entrypoints.rdFieldReadBarrierResolvedSamplingMethod) {
      return Entrypoints.rdFieldReadBarrierResolvedUsePassedMetadataMethod;
    } else if (current == Entrypoints.rdFieldWriteBarrierResolvedSamplingMethod) {
      return Entrypoints.rdFieldWriteBarrierResolvedUsePassedMetadataMethod;
    } else if (current == Entrypoints.rdArrayReadBarrierSamplingMethod) {
      return Entrypoints.rdArrayReadBarrierUsePassedMetadataMethod;
    } else if (current == Entrypoints.rdArrayWriteBarrierSamplingMethod) {
      return Entrypoints.rdArrayWriteBarrierUsePassedMetadataMethod;
    } else {
      return null;
    }
  }
  
  /** Should this thread be tracked by race detection? */
  @Inline
  public static final boolean isApplicableThread(RVMThread t) {
    return t.getRaceDetID() >= 0;
  }

  /** Should this thread be tracked by race detection? */
  @Inline
  public static final boolean isApplicableThread() {
    return isApplicableThread(RVMThread.getCurrentThread());
  }

  /** Insert read and write barriers? */
  @Inline
  public static final boolean algoBarriers() {
    return VM.raceDetBarriers;
  }
  
  /** Insert barriers under some conditions (just for volatiles) or in the general case (algorithm read and write barriers)? */
  @Inline
  public static final boolean someBarriers() {
    return VM.raceDetSyncOps || VM.raceDetBarriers;
  }
  
  /** Insert read and write barriers for statics only? */
  @Inline
  public static final boolean staticsOnly() {
    return VM.raceDetStaticsOnly;
  }
  
  /** Read and write barriers should do nothing? */
  @Inline
  public static final boolean emptyBarriers() {
    return VM.raceDetEmptyBarriers;
  }
  
  /** Insert read and write barriers for arrays? */
  @Inline
  public static final boolean arrays() {
    return !VM.raceDetNoArrays;
  }
  
  /** Use escape analysis to find definitely unshared objects? */
  @Inline
  public static boolean escapeAnalysis() {
    return !VM.raceDetNoEscapeAnalysis;
  }

  /** Perform race detection steps at synchronizations operations? */
  @Inline
  public static final boolean syncOps() {
    return VM.raceDetSyncOps;
  }
  
  /** Use per-field metadata at all? */
  @Inline
  public static final boolean fieldMetadata() {
    return VM.raceDetFieldMetadata;
  }

  /** Use per-field metadata for volatile fields? */
  @Inline
  public static final boolean directFieldMetadataVolatiles() {
    return fieldMetadata(); //&& syncOps();
  }
  
  /** Use per-field metadata for static fields? */
  @Inline
  public static final boolean directFieldMetadataStatics() {
    return fieldMetadata(); //&& barriers();
  }
  
  /** Use direct per-field metadata for non-static, non-volatile fields? */
  @Inline
  public static final boolean directFieldMetadataRegulars() {
    return fieldMetadata() && VM.raceDetDirectRegularFieldMetadata;
  }

  /** Use indirect per-field metadata for non-static, non-volatile fields? */
  @Inline
  public static final boolean indirectFieldMetadataRegulars() {
    return fieldMetadata() && !VM.raceDetDirectRegularFieldMetadata;
  }

  /** Use a header word for a lock object's vector clock? */
  @Inline
  public static final boolean lockVCs() {
    return syncOps();
  }

  /** Use epochs [Flanagan and Freund, PLDI 2009]? */
  @Inline
  public static final boolean epochs() {
    return !VM.raceDetNoEpochs;
  }

  /** For efficiency, load and pass pointer to indirect metadata in opt compiler? */
  @Inline
  public static final boolean passIndirectMetadata() {
    return someBarriers() && VM.raceDetPassIndirectMetadata;
  }

  /** Keep track of last-access time so we can report the time between two racy accesses? */
  @Inline
  public static final boolean trackTimes() {
    return VM.raceDetTrackTimes;
  }

  /** Keep track of how often an object or field has been accessed? */
  @Inline
  public static final boolean trackObjects() {
    return VM.raceDetTrackObjects;
  }

  /** Keep track of object allocation sites? */
  @Inline
  public static final boolean trackAllocSites() {
    return VM.raceDetTrackAllocSites;
  }

  /** Use sampling? */
  @Inline
  public static final boolean useSampling() {
    return VM.raceDetSampling;
  }

  /** Perform LiteRace-style sampling (incompatible with our sampling)? */
  @Inline
  public static final boolean liteRace() {
    return VM.raceDetLiteRace;
  }

  /** Use the fixed-rate version of LiteRace? */
  @Inline
  public static final boolean liteRaceFixed() {
    if (VM.VerifyAssertions) { VM._assert(liteRace()); }
    return VM.raceDetLiteRaceFixed;
  }

  /** Use randomization for LiteRace? */
  @Inline
  public static final boolean liteRaceRandom() {
    if (VM.VerifyAssertions) { VM._assert(liteRace()); }
    return VM.raceDetLiteRaceRandom;
  }

  /** Use method granularity for LiteRace? */
  @Inline
  public static final boolean liteRaceMethod() {
    if (VM.VerifyAssertions) { VM._assert(liteRace()); }
    return VM.raceDetLiteRaceMethod;
  }

  /** Inline hot sharing and race detection barriers? */
  @Inline
  public static final boolean inline() {
    return !VM.raceDetNoInlining;
  }
  
  /** Do redundant barrier elimination? */
  @Inline
  public static final boolean eliminateRedundantBarriers() {
    return VM.raceDetRBE;
  }
  
  /** Hoist the check of whether we're in a sampling period? */
  @Inline
  public static final boolean checkHoisting() {
    return !VM.raceDetNoCheckHoisting;
  }
  
  /** Use needed CAS operations? */
  @Inline
  public static final boolean useNeededCAS() {
    return !VM.raceDetNoBarrierCAS;
  }
  
  /** Track and report statistics? */
  @Inline
  public static boolean stats() {
    return VM.raceDetStats;
  }
  
  /** Report races as they occur occur? */
  @Inline
  public static boolean reportRaces() {
    return (/*stats() ||*/ verbosity() >= 3 || trackTimes());
  }

  /** Record races that occur? */
  @Inline
  public static boolean recordRaces() {
    return !trackTimes();
  }

  /** How much to print? */
  @Inline
  public static final int verbosity() {
    return 2;
  }

}
