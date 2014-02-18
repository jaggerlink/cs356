package org.jikesrvm.rd;

import org.jikesrvm.mm.mminterface.MemoryManager;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

/** RaceDet: represent the race detection access info for a field/slot */
@Uninterruptible
final class HashedAccess extends Access {

  final int fieldNumber;
  HashedAccess next;
  
  private HashedAccess(int fieldNumber) {
    this.fieldNumber = fieldNumber;
  }
  
  @UninterruptibleNoWarn
  @Inline
  static final HashedAccess newHashedAccess(int fieldNumber) {
    MemoryManager.setAllocatingInBarrier(true);
    HashedAccess access = new HashedAccess(fieldNumber);
    MemoryManager.setAllocatingInBarrier(false);
    return access;
  }
}
