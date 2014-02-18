package org.jikesrvm.rd;

import java.io.PrintStream;

import org.jikesrvm.Callbacks;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.scheduler.SpinLock;
import org.jikesrvm.util.HashMapRVM;
import org.jikesrvm.util.LinkedListRVM;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;

/** RaceDet: keeps track of statistics (for non-performance runs) */
@Uninterruptible
public final class Stats {

  static LinkedListRVM<Stat> stats = new LinkedListRVM<Stat>();
  static HashMapRVM<Object,Stat> objectStatMap;

  @Interruptible
  static final void boot() {
    objectStatMap = new HashMapRVM<Object,Stat>();
  }
  
  public static class Stat {
    final String name;
    protected long value;

    Stat(String name) {
      this.name = name;
      stats.add(this);
    }
    
    private Stat() {
      this.name = null;
    }
    
    @Uninterruptible
    public void inc() {
      long oldValue;
      do {
        oldValue = Magic.prepareLong(this, Entrypoints.rdStatValueField.getOffset());
      } while (!Magic.attemptLong(this, Entrypoints.rdStatValueField.getOffset(), oldValue, oldValue + 1));
    }
    
    final void report(PrintStream ps) {
      ps.println("RaceDet stat: " + name + " = " + this);
    }
    
    public String toString() {
      return commas(value);
    }
    
    public static final String commas(long num) {
      if (num < 0) {
        return "-" + commas(-num);
      } else if (num >= 1000) {
        return commas(num / 1000) + "," + String.valueOf((num % 1000) + 1000).substring(1);
      } else {
        return String.valueOf(num);
      }
    }
  }
  
  public static final class SamplingStat extends Stat {
    private final Stat duringSampling = new Stat();
    private final Stat duringNonSampling = new Stat();
    SamplingStat(String name) {
      super(name);
    }
    @Uninterruptible
    @Override
    public final void inc() {
      if (Sampling.duringSampling()) {
        duringSampling.inc();
      } else {
        duringNonSampling.inc();
      }
    }
    public String toString() {
      return duringSampling + " sampling, " + duringNonSampling + " non-sampling";
    }
  }

  public static final class MaxDiffStat extends Stat {
    private final Stat incStat = new Stat();
    private final Stat decStat = new Stat();
    private final SpinLock spinLock = new SpinLock();
    
    MaxDiffStat(String name) {
      super(name);
    }
    
    @Uninterruptible
    public final void inc() {
      spinLock.lock();
      incStat.inc();
      long diff = incStat.value - decStat.value;
      if (diff > value) {
        value = diff;
      }
      spinLock.unlock();
    }
    
    @Uninterruptible
    public final void dec() {
      spinLock.lock();
      decStat.inc();
      spinLock.unlock();
    }
  }

  static final SamplingStat readSameEpoch = new SamplingStat("readSameEpoch");
  static final SamplingStat readShared = new SamplingStat("readShared");
  static final SamplingStat readExclusive = new SamplingStat("readExclusive");
  static final SamplingStat readShare = new SamplingStat("readShare");
  
  static final Stat readFastPath = new Stat("readFastPath");

  static final SamplingStat writeSameEpoch = new SamplingStat("writeSameEpoch");
  static final SamplingStat writeExclusive = new SamplingStat("writeExclusive");
  static final SamplingStat writeShared = new SamplingStat("writeShared");
  
  static final Stat writeFastPath = new Stat("writeFastPath");
  
  static final SamplingStat readVolatile = new SamplingStat("readVolatile");
  static final SamplingStat writeVolatile = new SamplingStat("writeVolatile");
  
  static final SamplingStat acquire = new SamplingStat("acquire");
  static final SamplingStat release = new SamplingStat("release");
  static final SamplingStat fork = new SamplingStat("fork");
  //static Stat join = new Stat("join"); // these are accomplished with other primitives in Jikes
  
  public static final MaxDiffStat maxLiveThreads = new MaxDiffStat("liveThreads");

  static final SamplingStat copyShallow = new SamplingStat("copyShallow");
  static final SamplingStat copyDeep = new SamplingStat("copyDeep");
  static final SamplingStat joinShallow = new SamplingStat("joinShallow");
  static final SamplingStat joinDeep = new SamplingStat("joinDeep");

  public static final Stat compileTimeDefinitelyLocalArrays = new Stat("compileTimeDefinitelyLocalArrays");
  public static final Stat compileTimeMayEscapeArrays = new Stat("compileTimeMayEscapeArrays");
  public static final Stat compileTimeDefinitelyLocalScalars = new Stat("compileTimeDefinitelyLocalScalars");
  public static final Stat compileTimeMayEscapeScalars = new Stat("compileTimeMayEscapeScalars");
  
  @Interruptible
  static final void report(PrintStream ps) {
    for (Stat stat : stats) {
      stat.report(ps);
    }
  }
  
  @Interruptible
  public static final synchronized Stat getStatForObject(Object o) {
    Stat stat = objectStatMap.get(o);
    if (stat == null) {
      stat = new Stat(o.toString());
      objectStatMap.put(o, stat);
    }
    return stat;
  }
  
  static {
    if (RaceDet.stats()) {
      Callbacks.addExitMonitor(new Callbacks.ExitMonitor() {
        public void notifyExit(int value) {
          report(System.out);
        }
      });
    }
  }
}
