package org.jikesrvm.rd;

import org.jikesrvm.VM;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.objectmodel.MiscHeader;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/** RaceDet: for getting metadata for object fields, array elements, static fields, volatile fields */
@Uninterruptible
public final class GetMetadata {

  // Object metadata:
  
  @Inline //@NoBoundsCheck @NoNullCheck @NoCheckStore
  static final ObjectMetadata getMetadataNoSync(Object o) {
    return (ObjectMetadata)ObjectReference.fromObject(o).toAddress().loadWord(MiscHeader.FIELD_METADATA_OFFSET).and(LOCK_MASK.not()).toAddress().toObjectReference().toObject();
  }
  
  @Inline //@NoBoundsCheck @NoNullCheck @NoCheckStore
  static final ObjectMetadata lockMetadata(Object o, boolean create) {
    ObjectMetadata metadata = (ObjectMetadata)lock(ObjectReference.fromObject(o).toAddress(), MiscHeader.FIELD_METADATA_OFFSET);
    if (metadata == null && create) {
      metadata = ObjectMetadata.create(o);
    }
    return metadata;
  }
  
  @Inline //@NoBoundsCheck @NoNullCheck @NoCheckStore
  static final void unlockMetadata(Object o, ObjectMetadata metadata) {
    unlock(ObjectReference.fromObject(o).toAddress(), MiscHeader.FIELD_METADATA_OFFSET, metadata);
  }

  // Static access info:
  
  @Inline //@NoBoundsCheck @NoNullCheck @NoCheckStore
  static final Access getAccessNoSync(Offset metadataOffset) {
    return (Access)ObjectReference.fromObject(Magic.getJTOC()).toAddress().loadWord(metadataOffset).and(LOCK_MASK.not()).toAddress().toObjectReference().toObject();
  }
  
  @Inline //@NoBoundsCheck @NoNullCheck @NoCheckStore
  static final Access lockAccess(Offset metadataOffset, boolean create) {
    Access access = (Access)lock(Magic.getJTOC(), metadataOffset);
    if (access == null && create) {
      access = Access.newAccess();
    }
    return access;
  }
  
  @Inline //@NoBoundsCheck @NoNullCheck @NoCheckStore
  static final void unlockAccess(Offset metadataOffset, Access access) {
    unlock(Magic.getJTOC(), metadataOffset, access);
  }
  
  // Volatiles' vector clocks:
  
  @Inline //@NoBoundsCheck @NoNullCheck @NoCheckStore
  static final VC lockVolatileVC(Address addr) {
    VC vc = (VC)lock(addr, Offset.zero());
    return vc;
  }
  
  @Inline //@NoBoundsCheck @NoNullCheck @NoCheckStore
  static void unlockVolatileVC(Address addr, VC newVC) {
    unlock(addr, Offset.zero(), newVC);
  }
  
  // Vector clocks for lock objects:
  
  @Inline
  public static final VC getLockVC(Object o) {
    return (VC)ObjectReference.fromObject(o).toAddress().loadObjectReference(MiscHeader.LOCK_VC_OFFSET).toObject();
  }
  
  @Inline
  public static final void setLockVC(Object o, VC vc) {
    MemoryManager.putfieldWriteBarrier(o, vc, MiscHeader.LOCK_VC_OFFSET, 0);
  }
  
  // Locking:
  
  private static final Word LOCK_MASK = Word.one();
  
  @Inline
  private static final Object lock(Address addr, Offset offset) {
    if (RaceDet.useNeededCAS()) {
      Word oldValue;
      do {
        oldValue = addr.prepareWord(offset).and(LOCK_MASK.not());
      } while (!addr.attempt(oldValue, oldValue.or(LOCK_MASK), offset));
      return oldValue.toAddress().toObjectReference().toObject();
    } else {
      return addr.loadObjectReference(offset).toObject();
    }
  }

  @Inline
  private static final void unlock(Address addr, Offset offset, Object newObject) {
    if (RaceDet.useNeededCAS()) {
      Object oldObject = addr.loadObjectReference(offset).toObject();
      boolean success = MemoryManager.tryCompareAndSwapWriteBarrier(addr.toObjectReference().toObject(), offset, oldObject, newObject);
      if (VM.VerifyAssertions) { VM._assert(success); }
    } else {
      MemoryManager.putfieldWriteBarrier(addr.toObjectReference().toObject(), newObject, offset, 0);
    }
  }

  @Entrypoint
  @Inline
  public static final Object indirectMetadataBarrier(Object o, int samplingActivated) {
    if (o != null) {
      if ((samplingActivated | ObjectReference.fromObject(o).toAddress().loadInt(MiscHeader.FIELD_METADATA_OFFSET)) != 0) {
        return o;
      }
    }
    return null;
  }

}
