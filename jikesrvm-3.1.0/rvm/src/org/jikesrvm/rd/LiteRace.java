package org.jikesrvm.rd;

import java.io.PrintStream;

import org.jikesrvm.Callbacks;
import org.jikesrvm.VM;
import org.jikesrvm.classloader.MemberReference;
import org.jikesrvm.classloader.MethodReference;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.NoCheckStore;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

/** RaceDet: an inefficient implementation of LiteRace */
@Uninterruptible
public final class LiteRace {

  private static final int MAX_THREADS = 450; // need a lot (for hsqldb)
  private static long[] threadSamplingAccesses;
  private static long[] threadTotalAccesses;
  static {
    if (RaceDet.liteRace()) {
      threadSamplingAccesses = new long[MAX_THREADS];
      threadTotalAccesses = new long[MAX_THREADS];
      seeds = new long[MAX_THREADS];
    }
  }
  
  @Uninterruptible
  public static final class LiteRaceInfo {
    private int nCheck;
    private short nInstr;
    private short n; // used only for adaptive rate
    private static final short MAX_N = 4; // used only for adaptive rate
    private static final int Decr = 10; // used only for adaptive rate
    private static final double FixedRate = 0.05; // used only for fixed rate
    
    @Interruptible
    @Override
    public String toString() {
      return "nCheck = " + nCheck + ", nInstr = " + nInstr + ", n = " + n; 
    }
    
    @Interruptible
    public static LiteRaceInfo[] newArray() {
      return new LiteRaceInfo[MAX_THREADS];
    }
    
    @UninterruptibleNoWarn
    static LiteRaceInfo newInfo() {
      MemoryManager.setAllocatingInBarrier(true);
      LiteRaceInfo info = new LiteRaceInfo();
      MemoryManager.setAllocatingInBarrier(false);
      return info;
    }
    
    @Inline
    boolean sample() {
      if (nInstr > 0) {
        nInstr--;
        if (nInstr == 0) {
          nCheck = nCheck0();
          if (VM.raceDetLiteRaceRandom) {
            nCheck = (nextInt() % (nCheck + 1)) + (nCheck / 2); // [0.5*nCheck0, 1.5*nCheck0] 
          }
        }
        return true;
      } else {
        if (VM.VerifyAssertions) { VM._assert(nCheck > 0); }
        nCheck--;
        if (nCheck == 0) {
          nInstr = nInstr0();
        }
        return false;
      }
    }
    
    private int nCheck0() {
      if (RaceDet.liteRaceFixed()) {
        return (short)((1 - FixedRate) * VM.raceDetLiteRaceBurstLength / FixedRate); // 190 (if FixedRate == 0.05 and BurstLength == 10)
      } else {
        if (n < MAX_N) {
          n++;
        }
        return (pow(Decr, n - 1) - 1) * nInstr0();
      }
    }
    
    private short nInstr0() {
      return (short)VM.raceDetLiteRaceBurstLength;
    }
    
    static private int pow(int base, int exp) {
      int product = base;
      for (; exp > 1; exp--) {
        product *= base;
      }
      return product;
    }
    
    private LiteRaceInfo() {
      n = 1;
      nCheck = 0;
      nInstr = nInstr0();
    }
  }
  
  @Entrypoint
  @NoCheckStore
  static final void methodSample(int mid) {
    if (!RaceDet.isApplicableThread()) {
      return;
    }
    RVMMethod method = MemberReference.getMemberRef(mid).asMethodReference().getResolvedMember();
    int tid = RVMThread.getCurrentThread().getRaceDetID();
    LiteRaceInfo info = method.liteRaceInfos[tid];
    if (info == null) {
      info = LiteRaceInfo.newInfo();
      method.liteRaceInfos[tid] = info;
    }
    info.sample();
  }
  
  @NoCheckStore
  static final boolean sample(int siteID) {
    if (!RaceDet.isApplicableThread()) {
      return false;
    }
    Site site = Site.getSite(siteID);
    int tid = RVMThread.getCurrentThread().getRaceDetID();
    boolean sample;
    if (RaceDet.liteRaceMethod()) {
      LiteRaceInfo info = site.method.liteRaceInfos[tid];
      if (info == null) {
        info = LiteRaceInfo.newInfo();
        site.method.liteRaceInfos[tid] = info;
      }
      sample = (info.nCheck == 0);
    } else {
      LiteRaceInfo info = site.liteRaceInfos[tid];
      if (info == null) {
        info = LiteRaceInfo.newInfo();
        site.liteRaceInfos[tid] = info;
      }
      sample = info.sample();
    }
    if (sample) {
      threadSamplingAccesses[tid]++;
    }
    threadTotalAccesses[tid]++;
    return sample;
  }
  
  @Interruptible
  static void postReport(PrintStream ps) {
    long samplingAccesses =  0;
    long totalAccesses = 0;
    for (int i = 0; i < MAX_THREADS; i++) {
      samplingAccesses += threadSamplingAccesses[i];
      totalAccesses += threadTotalAccesses[i];
    }
    double effectiveRate = (double)samplingAccesses / totalAccesses;
    System.out.println("Effective sampling rate = " + effectiveRate + " (" + samplingAccesses + " / " + totalAccesses + ")");
  }
  
  static {
    if (RaceDet.liteRace()) {
      Callbacks.addExitMonitor(new Callbacks.ExitMonitor() {
        public void notifyExit(int value) {
          postReport(System.out);
        }
      });
    }
  }

  // Stolen from classpath's Random.java so we don't have to deal with calling interruptible code:
  
  static private long seeds[];
  
  private static final int nextInt() {
    return next(31, RVMThread.getCurrentThread().getRaceDetID());
  }
  
  private static final int next(int bits, int tid) {
    seeds[tid] = (seeds[tid] * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
    return (int) (seeds[tid] >>> (48 - bits));
  }

}
