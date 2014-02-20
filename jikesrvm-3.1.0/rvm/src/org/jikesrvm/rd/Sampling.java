package org.jikesrvm.rd;

import java.io.PrintStream;

import org.jikesrvm.Callbacks;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Time;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

/** RaceDet: the sampling aspects of sampling-based race detection */
@Uninterruptible
public final class Sampling {

  /** Is sampling currently on? */
  @Entrypoint
  static int samplingActivated = (RaceDet.useSampling() ? 0 : 1);

  @Interruptible
  static final void boot() {
    if (RaceDet.useSampling()) {
      seed = Time.currentTimeMillis();
      samplingActivated = (nextFloat() < VM.raceDetSamplingRate ? 1 : 0);
    }
  }
  
  // Various sampling-based decisions
  
  /** The most general sampling question: is it currently a sampling period? */
  @Inline
  static final boolean duringSampling() {
    if (RaceDet.useSampling()) {
      return samplingActivated != 0;
    } else {
      return true;
    }
  }
  
  /** Should we record new accesses? */
  @Inline
  public static final boolean recordAccesses() {
    if (RaceDet.useSampling()) {
      return samplingActivated != 0;
    } else {
      return true;
    }
  }

  /** Should we throw away accesses? */
  @Inline
  public static final boolean deleteAccesses() {
    if (RaceDet.useSampling()) {
      return samplingActivated == 0;
    } else {
      return false;
    }
  }

  /** Should we increment vector clocks after relevant operations? */
  @Inline
  public static final boolean incClocks() {
    if (RaceDet.useSampling()) {
      return samplingActivated != 0;
    } else {
      return true;
    }
  }

  /** Should we do shallow vector clock copies where possible? */
  @Inline
  public static final boolean shallowVectorClockCopies() {
    if (RaceDet.useSampling()) {
      return samplingActivated == 0;
    } else {
      return false;
    }
  }

  private static int totalPeriods;
  private static int samplingPeriods;
  private static long totalIncrements;
  private static long samplingIncrements;
  public static long deadThreadIncrements;
  private static float biasRatio = 1;
  
  /** Turn sampling on or off */
  @UninterruptibleNoWarn
  public static final void samplingDecision(boolean verbose) {
    if (VM.VerifyAssertions) { VM._assert(RaceDet.useSampling()); }
    // compute work done during last sampling or non-sampling period
    int delta = 0;
    for (int i = 0; i < RVMThread.numThreads; i++) {
      RVMThread thread = RVMThread.threads[i];
      if (RaceDet.isApplicableThread(thread) && thread.isAlive()) {
        delta += thread.numIncrements;
        thread.numIncrements = 0;
      }
    }
    // RaceDet: LATER: doesn't correlate well, at least for hsqldb (not sure if this is true anymore)
    delta += deadThreadIncrements;
    deadThreadIncrements = 0;
    //if (VM.VerifyAssertions) { VM._assert(delta >= 0); }
    totalIncrements += delta;
    if (samplingActivated != 0) {
      samplingIncrements += delta;
    }

    // compute the effective rate so far, and scale the target rate based on that
    if (totalIncrements > 0) {
      // compute how many sampling and total periods
      if (samplingActivated != 0) {
        samplingPeriods++;
      }
      totalPeriods++;
      
      float samplingPeriodRate = (float)samplingPeriods / totalPeriods;
      if (verbose) {
        reportEffectiveRate("Effective rate = ");
        VM.sysWrite("  Sampling period rate: ", samplingPeriodRate); VM.sysWriteln(" ( ", samplingPeriods, " / ", totalPeriods, ")");
      }

      // RaceDet: LATER: only compute ratio each time sampling ends?
      //if (samplingActivated) {
        float nonSamplingIncrements = totalIncrements - samplingIncrements;
        float nonSamplingPeriods = totalPeriods - samplingPeriods;
        if (samplingIncrements != 0 && nonSamplingPeriods != 0) {
          biasRatio = (float)nonSamplingIncrements * samplingPeriods / (samplingIncrements * nonSamplingPeriods);
        }
        // let's adjust the desired rate based on what rate we've gotten so far
        float desiredRate;
        float effectiveRate = (float)samplingIncrements / totalIncrements;
        if (effectiveRate == 0) {
          desiredRate = VM.raceDetSamplingRate;
        } else {
          desiredRate = VM.raceDetSamplingRate * VM.raceDetSamplingRate / effectiveRate;
        }
        float scaledSamplingRate = desiredRate * biasRatio;
        float targetProb = scaledSamplingRate / (scaledSamplingRate + (1 - desiredRate));
        float aggrProb;
        if (samplingPeriodRate == 0) {
          aggrProb = targetProb;
        } else {
          aggrProb = targetProb * targetProb / samplingPeriodRate;
        }
        
        if (verbose) {
          VM.sysWriteln("  Bias ratio = ", biasRatio);
          VM.sysWriteln("  scaledSamplingRate = ", scaledSamplingRate);
          VM.sysWriteln("  targetProb = ", targetProb);
          VM.sysWriteln("  aggrProb = ", aggrProb);
        }
      //}
      
      // decide whether to do sampling next
      samplingActivated = (nextBoolean(aggrProb) ? 1 : 0);
      if (verbose) {
        VM.sysWriteln("  Sampling activated = ", samplingActivated);
      }
    }
  }
  
  // Stolen from classpath's Random.java so we don't have to deal with calling synchronized code:
  
  static private long seed;
  
  private static final boolean nextBoolean(float p) {
    return nextFloat() < p;
  }
  
  private static final float nextFloat() {
    return next(24) / (float) (1 << 24);
  }
  
  private static final int next(int bits) {
    seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
    return (int) (seed >>> (48 - bits));
  }

  private static void reportEffectiveRate(String prefix) {
    //float effectiveRate = (float)samplingIncrements / totalIncrements;
    //TODO
    //VM.sysWrite(prefix, effectiveRate);
    //VM.sysWriteln(" (", samplingIncrements, " / ", totalIncrements, ")");
    Races.socketData.setIncrements(samplingIncrements, totalIncrements);
  }

  @Interruptible
  static void postReport(PrintStream ps) {
    reportEffectiveRate("Effective sampling rate = ");
  }
  
  static {
    if (RaceDet.useSampling()) {
      Callbacks.addExitMonitor(new Callbacks.ExitMonitor() {
        public void notifyExit(int value) {
          postReport(System.out);
        }
      });
    }
  }

}
