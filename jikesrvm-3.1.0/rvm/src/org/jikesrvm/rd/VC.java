package org.jikesrvm.rd;

import org.jikesrvm.VM;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.NoInline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

/** RaceDet: represent a vector clock */
@Uninterruptible
public final class VC {
  int[] clocks;
  private int lastTid = -1; // RaceDet: LATER: make short?
  private int lastVersion = -1;
  private int[] versions; // the versions of various threads that have joined to make this VC
  private boolean shared; // is this VC object shared between multiple objects?

  // RaceDet: LATER: doesn't seem like we need to worry about synchronization on shared since
  // it'll get set to true only during non-sampling periods, and only sampling periods will be
  // checking whether they can modify it in place or need to clone it first

  public VC(int tid) {
    clocks = newIntArray(tid + 1);
    lastTid = tid;
    lastVersion = 0;
    versions = newIntArray(tid + 1);
  }
  
  private VC(VC vc, int newLength) {
    clocks = cloneIntArray(vc.clocks, newLength);
    lastTid = vc.lastTid;
    lastVersion = vc.lastVersion;
    versions = cloneIntArray(vc.versions, newLength);
    shared = false;
  }
  
  @Inline
  static final VC joinTargetIsVolatile(VC dest, VC src) {
    // if sampling is off, try to do a shallow copy
    if (Sampling.shallowVectorClockCopies()) {
      // check if dest HB src
      if (dest == null ||
          (src.versions.length > dest.lastTid &&
           dest.lastTid >= 0 &&
           src.versions[dest.lastTid] >= dest.lastVersion)) {
        // do shallow copy
        if (RaceDet.stats()) { Stats.joinShallow.inc(); } // count it as a shallow join
        src.shared = true;
        return src;
      }
    }
    // do deep join
    return joinSlowPath(dest, src, false);
  }
  
  @Inline
  static final VC join(VC dest, VC src) {
    
    // if the destination isn't a thread VC, it might be null
    // if the incoming VC is null or we have the latest version, we can skip the join
    if (src == null || (dest.versions.length > src.lastTid && src.lastTid >= 0 && dest.versions[src.lastTid] >= src.lastVersion)) {
      if (RaceDet.stats()) { Stats.joinShallow.inc(); }
      return dest;
    }
    return joinSlowPath(dest, src, true);
  }
  
  @NoInline
  private static final VC joinSlowPath(VC dest, VC src, boolean destIsThreadVC) {
    if (RaceDet.stats()) { Stats.joinDeep.inc(); }
    if (!destIsThreadVC && dest == null) {
      // RaceDet: LATER: doing this since possibly activating sharing in sampling periods leads to dangerous races
      // -- but need to think about it
      return src.cloneDeep();
      /*
      if (Sampling.shallowVectorClockCopies()) {
        src.shared = true;
        return src;
      } else {
        return src.cloneDeep();
      }
      */
    }
    // if we're copying to a volatile, can we do a shallow copy
    if (!destIsThreadVC && Sampling.shallowVectorClockCopies()) {
      boolean destHappensBeforeSrc = true;
      if (src.clocks.length < dest.clocks.length) {
        destHappensBeforeSrc = false;
      } else {
        for (int i = 0; i < dest.clocks.length; i++) {
          if (dest.clocks[i] > src.clocks[i]) {
            destHappensBeforeSrc = false;
            break;
          }
        }
      }
      // we can do a shallow copy instead of a deep join
      if (destHappensBeforeSrc) {
        src.shared = true;
        return src;
      }
    }
    // first check if we're going to update the clock
    boolean needsUpdate = false;
    if (dest.clocks.length < src.clocks.length) {
      needsUpdate = true;
    } else {
      for (int i = 0; i < src.clocks.length; i++) {
        if (src.clocks[i] > dest.clocks[i]) {
          needsUpdate = true;
          break;
        }
      }
    }
    if (needsUpdate) {
      if (dest.shared || Sampling.shallowVectorClockCopies()) {
        dest = dest.cloneDeep();
      }
      int minLength = src.clocks.length;
      if (dest.clocks.length < minLength) {
        dest.versions = cloneIntArray(dest.versions, minLength);
        dest.clocks = cloneIntArray(dest.clocks, minLength);
      }
      int[] srcClocks = src.clocks;
      int[] destClocks = dest.clocks;
      for (int i = 0; i < srcClocks.length; i++) {
        if (srcClocks[i] > destClocks[i]) {
          destClocks[i] = srcClocks[i];
        }
      }
      if (destIsThreadVC) {
        dest.lastVersion++;
      }
    }
    // if it's a thread
    if (destIsThreadVC) {
      if (src.lastTid >= 0) {
        // RaceDet: LATER: do a join on versions array?
        dest.versions[src.lastTid] = src.lastVersion;
      }
    // otherwise it's a volatile
    } else {
      // vector clock got updated, so lastTid isn't valid anymore
      if (needsUpdate) {
        dest.lastTid = -1;
        dest.lastVersion = -1;
      }
    }
    return dest;
  }
  
  @Inline
  static final VC copy(VC dest, VC src) {
    if (Sampling.shallowVectorClockCopies()) {
      if (RaceDet.stats()) { Stats.copyShallow.inc(); }
      src.shared = true;
      return src;
    } else {
      return copySlowPath(dest, src);
    }
  }

  // RaceDet: LATER: try to do shallow copy here?
  @Inline
  static final VC copyThreadVC(VC dest, VC src, int tid) {
    dest = copySlowPath(dest, src);
    dest.lastTid = tid;
    dest.lastVersion = 0;
    return dest;
  }

  @NoInline
  private static final VC copySlowPath(VC dest, VC src) {
    if (RaceDet.stats()) { Stats.copyDeep.inc(); }
    if (dest == null || dest.shared) {
      return src.cloneDeep();
    } else {
      int srcLength = src.clocks.length;
      if (srcLength > dest.clocks.length) {
        dest.clocks = cloneIntArray(src.clocks, srcLength);
        dest.versions = cloneIntArray(src.versions, srcLength);
      } else {
        copyIntArray(dest.clocks, src.clocks);
        copyIntArray(dest.versions, src.versions);
      }
      dest.lastTid = src.lastTid;
      dest.lastVersion = src.lastVersion;
      return dest;
    }
  }
  
  @Inline
  private final VC cloneDeep() {
    return cloneDeep(this.clocks.length);
  }
  
  @UninterruptibleNoWarn
  private final VC cloneDeep(int newLength) {
    MemoryManager.setAllocatingInBarrier(true);
    VC newVC = new VC(this, newLength);
    MemoryManager.setAllocatingInBarrier(false);
    return newVC;
  }
  
  @Inline
  static final boolean lessThan(int tid, int clock, VC vc) {
    if (vc.clocks.length > tid) {
      return clock <= vc.clocks[tid];
    } else {
      return clock == 0;
    }
  }
  
  static final boolean lessThan(ReadElementMap vc1, VC vc2) {
    for (ReadElement elem : vc1.getData()) {
      for (; elem != null; elem = elem.next) {
        if (elem.tid >= vc2.clocks.length || elem.clock > vc2.clocks[elem.tid]) {
          return false;
        }
      }
    }
    return true;
  }

  @Inline
  public final VC incrementClock(int tid) {
    if (shared) {
      VC newVC = cloneDeep();
      newVC.incrementClock(tid);
      return newVC;
    }
    clocks[tid]++;
    lastVersion++;
    if (VM.VerifyAssertions) { VM._assert(lastTid == tid); }
    return this;
  }

  @Inline
  public final int getClock(int tid) {
    return clocks[tid];
  }

  private static final void copyIntArray(int[] dest, int[] src) {
    if (VM.VerifyAssertions) { VM._assert(dest.length >= src.length); }
    for (int i = 0; i < src.length; i++) {
      dest[i] = src[i];
    }
  }

  private static final int[] cloneIntArray(int[] array, int newSize) {
    int[] newArray = newIntArray(newSize);
    for (int i = 0; i < array.length; i++) {
      newArray[i] = array[i];
    }
    return newArray;
  }

  @UninterruptibleNoWarn
  private static final int[] newIntArray(int size) {
    MemoryManager.setAllocatingInBarrier(true);
    int[] newVC = new int[size];
    MemoryManager.setAllocatingInBarrier(false);
    return newVC;
  }
  
  final void write()  {
    String delim = "";
    VM.write("< ");
    for (int i = 0; i < clocks.length; i++) {
      VM.write(delim);
      VM.writeInt(clocks[i]);
      delim = ", ";
    }
    VM.write(" >");
  }
}
