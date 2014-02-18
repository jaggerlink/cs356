package org.jikesrvm.rd;

import org.jikesrvm.mm.mminterface.MemoryManager;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

/** RaceDet: represent the race detection access info for a field/slot */
@Uninterruptible
class Access {
  int writeTid;
  int writeClock;
  int writeSiteID = -1; // signifies that there's no write info recorded

  int readTid;
  int readClock;
  int readSiteID = -1; // not necessary but may help debugging

  ReadElementMap readVector;

  @UninterruptibleNoWarn
  @Inline
  static final Access newAccess() {
    MemoryManager.setAllocatingInBarrier(true);
    Access access = new Access();
    MemoryManager.setAllocatingInBarrier(false);
    return access;
  }

  @Inline
  final boolean hasWriteInfo() {
    return writeSiteID != -1;
  }
  
  @Inline
  final boolean hasReadEpoch() {
    return readVector == null;
  }
  
}
