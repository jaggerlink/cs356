package org.jikesrvm.rd;

import org.jikesrvm.mm.mminterface.MemoryManager;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

/** RaceDet: map from integers to objects */
@Uninterruptible
final class ReadElementMap extends IntMap<ReadElement> {
  
  @UninterruptibleNoWarn
  final static ReadElementMap newReadElementMap() {
    MemoryManager.setAllocatingInBarrier(true);
    ReadElementMap map = new ReadElementMap();
    MemoryManager.setAllocatingInBarrier(false);
    return map;
  }
  
  @UninterruptibleNoWarn
  final ReadElement[] newArray(int length) {
    MemoryManager.setAllocatingInBarrier(true);
    ReadElement[] array = new ReadElement[length];
    MemoryManager.setAllocatingInBarrier(false);
    return array;
  }
  
  final int getKey(ReadElement value) {
    return value.tid;
  }
  
  final ReadElement getNext(ReadElement value) {
    return value.next;
  }

  final void setNext(ReadElement value, ReadElement next) {
    value.next = next;
  }

  final ReadElement[] getData() {
    return data;
  }
}
