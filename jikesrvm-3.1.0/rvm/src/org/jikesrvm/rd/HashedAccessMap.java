package org.jikesrvm.rd;

import org.jikesrvm.mm.mminterface.MemoryManager;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

/** RaceDet: map from integers to objects */
@Uninterruptible
final class HashedAccessMap extends IntMap<HashedAccess> {
  
  @UninterruptibleNoWarn
  final static HashedAccessMap newHashedAccessMap() {
    MemoryManager.setAllocatingInBarrier(true);
    HashedAccessMap map = new HashedAccessMap();
    MemoryManager.setAllocatingInBarrier(false);
    return map;
  }
  
  @UninterruptibleNoWarn
  final HashedAccess[] newArray(int length) {
    MemoryManager.setAllocatingInBarrier(true);
    HashedAccess[] array = new HashedAccess[length];
    MemoryManager.setAllocatingInBarrier(false);
    return array;
  }
  
  final int getKey(HashedAccess value) {
    return value.fieldNumber;
  }
  
  final HashedAccess getNext(HashedAccess value) {
    return value.next;
  }

  final void setNext(HashedAccess value, HashedAccess next) {
    value.next = next;
  }

}
