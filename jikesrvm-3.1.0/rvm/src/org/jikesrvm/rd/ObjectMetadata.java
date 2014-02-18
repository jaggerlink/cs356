package org.jikesrvm.rd;

import org.jikesrvm.Constants;
import org.jikesrvm.classloader.RVMType;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.objectmodel.ObjectModel;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.NoInline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;
import org.vmmagic.unboxed.Word;
import org.vmmagic.unboxed.WordArray;

/** RaceDet: represent the race detection metadata for an object*/
@Uninterruptible
abstract class ObjectMetadata {
  HashedAccessMap fieldAccesses;

  @UninterruptibleNoWarn
  @NoInline
  static final ObjectMetadata create(Object o) {
    RVMType type = ObjectModel.getObjectType(o);
    int numFields;
    if (type.isArrayType()) {
      numFields = ObjectModel.getArrayLength(o);
    } else {
      numFields = type.asClass().getIndirectMetadataFields();
    }
    
    ObjectMetadata metadata;
    MemoryManager.setAllocatingInBarrier(true);
    if (numFields <= Constants.BITS_IN_WORD) {
      metadata = new SmallObjectMetadata();
    } else {
      metadata = new LargeObjectMetadata(numFields);
    }
    MemoryManager.setAllocatingInBarrier(false);
    return metadata;
  }
  
  abstract boolean hasAccess(int fieldNumber);
  
  abstract void setAccess(int fieldNumber);
  
  abstract void clearAccess(int fieldNumber);
  
  @UninterruptibleNoWarn
  @Inline
  final Access getAccess(int fieldNumber, boolean create) {
    if (fieldAccesses == null) {
      if (create) {
        fieldAccesses = HashedAccessMap.newHashedAccessMap();
      } else {
        return null;
      }
    }
    HashedAccess access = fieldAccesses.get(fieldNumber);
    if (access == null && create) {
      access = HashedAccess.newHashedAccess(fieldNumber);
      fieldAccesses.put(access);
      setAccess(fieldNumber);
    }
    return access;
  }
  
  @UninterruptibleNoWarn
  @Inline
  final ObjectMetadata removeAccess(int fieldNumber) {
    fieldAccesses.remove(fieldNumber);
    clearAccess(fieldNumber);
    if (fieldAccesses.size() == 0) {
      fieldAccesses = null;
      // are we done with the access
      if (isEmpty()) {
        return null;
      }
    }
    return this;
  }
  
  final boolean isEmpty() {
    return fieldAccesses == null;
  }
}

@Uninterruptible
final class SmallObjectMetadata extends ObjectMetadata {
  Word bitVector;

  @Inline
  final Word getMask(int fieldNumber) {
    return Word.one().lsh(fieldNumber);
  }
  
  @Inline
  final boolean hasAccess(int fieldNumber) {
    return fieldAccesses != null && !bitVector.and(getMask(fieldNumber)).isZero();
  }

  @Inline
  final void clearAccess(int fieldNumber) {
    bitVector = bitVector.and(getMask(fieldNumber).not());
  }

  @Inline
  final void setAccess(int fieldNumber) {
    bitVector = bitVector.or(getMask(fieldNumber));
  }
}

@Uninterruptible
final class LargeObjectMetadata extends ObjectMetadata {
  public LargeObjectMetadata(int numFields) {
    int size = (numFields + Constants.BITS_IN_WORD - 1) >>> Constants.LOG_BITS_IN_WORD;
    bitVector = WordArray.create(size);
  }
  
  WordArray bitVector;

  @Inline
  final int getIndex(int fieldNumber) {
    return fieldNumber >>> Constants.LOG_BITS_IN_WORD;
  }
  
  @Inline
  final Word getMask(int fieldNumber) {
    int shift = fieldNumber & ((1 << Constants.LOG_BITS_IN_WORD) - 1);
    return Word.one().lsh(shift);
  }
  
  @Inline
  final boolean hasAccess(int fieldNumber) {
    return fieldAccesses != null && !bitVector.get(getIndex(fieldNumber)).and(getMask(fieldNumber)).isZero();
  }
  
  @Inline
  final void clearAccess(int fieldNumber) {
    int index = getIndex(fieldNumber);
    bitVector.set(index, bitVector.get(index).and(getMask(fieldNumber).not()));
  }

  @Inline
  final void setAccess(int fieldNumber) {
    int index = getIndex(fieldNumber);
    bitVector.set(index, bitVector.get(index).or(getMask(fieldNumber)));
  }
}
