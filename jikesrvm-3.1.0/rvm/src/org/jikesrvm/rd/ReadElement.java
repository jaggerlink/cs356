package org.jikesrvm.rd;

import org.jikesrvm.mm.mminterface.MemoryManager;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

/** RaceDet: an element of a read vector */
@Uninterruptible
final class ReadElement {
  final int tid;
  int clock;
  int siteID;
  ReadElement next;

  private ReadElement(int tid, int clock, int siteID) {
    this.tid = tid;
    this.clock = clock;
    this.siteID = siteID;
  }
  
  @UninterruptibleNoWarn
  static ReadElement newReadElement(int tid, int clock, int siteID) {
    MemoryManager.setAllocatingInBarrier(true);
    ReadElement elem = new ReadElement(tid, clock, siteID);
    MemoryManager.setAllocatingInBarrier(false);
    return elem;
  }
}
