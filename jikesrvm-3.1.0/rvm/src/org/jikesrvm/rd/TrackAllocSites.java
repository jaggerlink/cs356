package org.jikesrvm.rd;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

import org.jikesrvm.Callbacks;
import org.jikesrvm.VM;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.objectmodel.MiscHeader;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.scheduler.SpinLock;
import org.jikesrvm.scheduler.Synchronization;
import org.jikesrvm.util.HashMapRVM;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.NoInline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;
import org.vmmagic.unboxed.ObjectReference;

/** RaceDet: track how often objects are accessed based on allocation site */
@Uninterruptible
public final class TrackAllocSites {
  @Inline
  public static final int getObjectCount(ObjectReference object) {
    if (VM.VerifyAssertions) { VM._assert(RaceDet.trackObjects()); }
    if (VM.VerifyAssertions) { VM._assert(!isJTOC(object)); }
    return object.toAddress().loadInt(MiscHeader.OBJECT_COUNT_OFFSET);
  }
  
  public static final void incObjectCount(ObjectReference object) {
    if (VM.VerifyAssertions) { VM._assert(RaceDet.trackObjects()); }
    if (VM.VerifyAssertions) { VM._assert(!isJTOC(object)); }
    Synchronization.fetchAndAdd(object.toObject(), MiscHeader.OBJECT_COUNT_OFFSET, 1);
  }
  
  public static final int getAllocSite(ObjectReference object) {
    if (VM.VerifyAssertions) { VM._assert(RaceDet.trackAllocSites()); }
    if (VM.VerifyAssertions) { VM._assert(!isJTOC(object)); }
    return object.toAddress().loadInt(MiscHeader.ALLOC_SITE_OFFSET);
  }

  public static final int getAllocSiteCount(ObjectReference object) {
    if (VM.VerifyAssertions) { VM._assert(RaceDet.trackAllocSites()); }
    if (VM.VerifyAssertions) { VM._assert(!isJTOC(object)); }
    return object.toAddress().loadInt(MiscHeader.ALLOC_SITE_COUNT_OFFSET);
  }

  public static final void setAllocSite(ObjectReference object, int site, int count) {
    if (VM.VerifyAssertions) { VM._assert(RaceDet.trackAllocSites()); }
    if (VM.VerifyAssertions) { VM._assert(!isJTOC(object)); }
    object.toAddress().store(site, MiscHeader.ALLOC_SITE_OFFSET);
    object.toAddress().store(count, MiscHeader.ALLOC_SITE_COUNT_OFFSET);
  }
  
  static HashMapRVM<Integer,Integer> allocSites = new HashMapRVM<Integer,Integer>();
  static final SpinLock allocSitesLock = new SpinLock();
  
  @UninterruptibleNoWarn
  public static final boolean updateAllocSites(int site) {
    MemoryManager.setAllocatingInBarrier(true);
    allocSitesLock.lock();
    if (allocSites != null) {
      Integer intObject = allocSites.get(site);
      int count = 0;
      if (intObject != null) {
        count = intObject.intValue();
      }
      allocSites.put(site, count + 1);
    }
    allocSitesLock.unlock();
    MemoryManager.setAllocatingInBarrier(false);
    return true;
  }
  
  @UninterruptibleNoWarn
  public static final int getAllocSiteCount(int site) {
    MemoryManager.setAllocatingInBarrier(true);
    Integer intObject = null;
    allocSitesLock.lock();
    if (allocSites != null) {
      intObject = allocSites.get(site);
    }
    allocSitesLock.unlock();
    int count = 0;
    if (intObject != null) {
      count = intObject.intValue();
    }
    MemoryManager.setAllocatingInBarrier(false);
    return count;
  }
  
  @Interruptible
  static void postReport(PrintStream ps) {
    allocSitesLock.lock();
    final HashMapRVM<Integer,Integer> allocSites = TrackAllocSites.allocSites;
    TrackAllocSites.allocSites = null;
    allocSitesLock.unlock();
    
    Integer[] sites = new Integer[allocSites.size()];
    int i = 0;
    int total = 0;
    for (int site : allocSites.keys()) {
      sites[i++] = site;
      total += allocSites.get(site);
    }
    Arrays.sort(sites, new Comparator<Integer>() {
      public int compare(Integer o1, Integer o2) {
        return allocSites.get(o1).compareTo(allocSites.get(o2));
      }
    });
    int cumulative = 0;
    for (int site : sites) {
      int count = allocSites.get(site);
      cumulative += count;
      ps.println("Alloc site " + site + ": " + count + " (" + ((double)cumulative / total) + ")");
    }
  }
  
  @Inline
  static final boolean isJTOC(ObjectReference object) {
    return Magic.getJTOC().EQ(object.toAddress());
  }
  
  static {
    if (RaceDet.trackAllocSites()) {
      Callbacks.addExitMonitor(new Callbacks.ExitMonitor() {
        public void notifyExit(int value) {
          postReport(System.out);
        }
      });
    }
  }

}
