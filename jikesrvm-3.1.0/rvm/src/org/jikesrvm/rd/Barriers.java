package org.jikesrvm.rd;

import org.jikesrvm.Constants;
import org.jikesrvm.VM;
import org.jikesrvm.classloader.FieldReference;
import org.jikesrvm.classloader.RVMField;
import org.jikesrvm.objectmodel.MiscHeader;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/** RaceDet: knows about accessing per-field metadata */
@Uninterruptible
public final class Barriers implements Constants {

  // Need read set and write set per field
  static final int LOG_NUM_FIELDS = 0;
  static final int LOG_NUM_FIELDS_VOLATILE = 0;
  static final int NUM_FIELDS = 1 << LOG_NUM_FIELDS;
  static final int NUM_FIELDS_VOLATILE = 1 << LOG_NUM_FIELDS_VOLATILE;
  public static final int LOG_FIELD_SIZE = LOG_BYTES_IN_WORD;
  public static final int FIELD_SIZE = 1 << LOG_FIELD_SIZE;
  
  @Inline
  public static final int getNumFields(RVMField field) {
    if (VM.VerifyAssertions) { VM._assert(RaceDet.shouldAddSomeFieldMetadata(field)); }
    if (field.isVolatile()) {
      return NUM_FIELDS_VOLATILE;
    }
    return NUM_FIELDS;
  }

  @Inline
  public static boolean isReference(RVMField field, int whichOffset) {
    if (VM.VerifyAssertions) { VM._assert(RaceDet.shouldAddSomeFieldMetadata(field)); }
    return true;
  }

  // member fields:
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldReadBarrierResolved(Object o, int fieldNumber, int siteID) {
    fieldReadBarrierResolvedSampling(o, fieldNumber, siteID, Sampling.samplingActivated);
  }
  
  // RaceDet: LATER: look at the actual code generated here to see how much better we could do
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldReadBarrierResolvedSampling(Object o, int fieldNumber, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(o).toAddress().loadInt(MiscHeader.FIELD_METADATA_OFFSET)) != 0) {
      Algo.updateObjectMetadataNoInlineRead(o, fieldNumber, siteID);
    }
  }
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldReadBarrierResolvedSamplingStats(Object o, int fieldNumber, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(o).toAddress().loadInt(MiscHeader.FIELD_METADATA_OFFSET)) != 0) {
      Algo.updateObjectMetadataNoInlineRead(o, fieldNumber, siteID);
    } else if (RaceDet.stats()) {
      Stats.readFastPath.inc();
    }
  }
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldReadBarrierResolvedUsePassedMetadata(Object o, int fieldNumber, int siteID) {
    if (o != null) {
      Algo.updateObjectMetadataNoInlineRead(o, fieldNumber, siteID);
    }
  }
  
  // RaceDet: LATER: can we remove these null checks?
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldReadBarrierResolvedVolatile(Object o, Offset metadataOffset) {
    if (o != null) {
      Algo.readVolatile(ObjectReference.fromObject(o), metadataOffset);
    }
  }
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldReadBarrierUnresolved(Object o, int fieldID, int siteID) {
    if (o != null) {
      RVMField field = FieldReference.getMemberRef(fieldID).asFieldReference().getResolvedField();
      if (field.hasMetadataOffset()) {
        if (field.isVolatile()) {
          Algo.readVolatile(ObjectReference.fromObject(o), field.getMetadataOffset());
        } else if (RaceDet.algoBarriers()) {
          Algo.updateObjectMetadata(o, field.getFieldNumber(), true, siteID);
        }
      }
    }
  }
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldWriteBarrierResolved(Object o, int fieldNumber, int siteID) {
    fieldWriteBarrierResolvedSampling(o, fieldNumber, siteID, Sampling.samplingActivated);
  }
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldWriteBarrierResolvedSampling(Object o, int fieldNumber, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(o).toAddress().loadInt(MiscHeader.FIELD_METADATA_OFFSET)) != 0) {
      Algo.updateObjectMetadataNoInlineWrite(o, fieldNumber, siteID);
    }
  }
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldWriteBarrierResolvedSamplingStats(Object o, int fieldNumber, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(o).toAddress().loadInt(MiscHeader.FIELD_METADATA_OFFSET)) != 0) {
      Algo.updateObjectMetadataNoInlineWrite(o, fieldNumber, siteID);
    } else if (RaceDet.stats()) {
      Stats.writeFastPath.inc();
    }
  }
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldWriteBarrierResolvedUsePassedMetadata(Object o, int fieldNumber, int siteID) {
    if (o != null) {
      Algo.updateObjectMetadataNoInlineWrite(o, fieldNumber, siteID);
    }
  }

  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldWriteBarrierResolvedVolatile(Object o, Offset metadataOffset) {
    if (o != null) {
      Algo.writeVolatile(ObjectReference.fromObject(o), metadataOffset);
    }
  }
  
  @Entrypoint
  @Inline
  //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void fieldWriteBarrierUnresolved(Object o, int fieldID, int siteID) {
    if (o != null) {
      RVMField field = FieldReference.getMemberRef(fieldID).asFieldReference().getResolvedField();
      if (field.hasMetadataOffset()) {
        if (field.isVolatile()) {
          Algo.writeVolatile(ObjectReference.fromObject(o), field.getMetadataOffset());
        } else if (RaceDet.algoBarriers()) {
          Algo.updateObjectMetadata(o, field.getFieldNumber(), false, siteID);
        }
      }
    }
  }

  // static fields:
  
  @Entrypoint
  @Inline
  public static final void fieldReadBarrierStaticResolved(Offset metadataOffset, int siteID) {
    fieldReadBarrierStaticResolvedSampling(metadataOffset, siteID, Sampling.samplingActivated);
  }
  
  @Entrypoint
  @Inline
  public static final void fieldReadBarrierStaticResolvedSampling(Offset metadataOffset, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(Magic.getJTOC()).toAddress().loadInt(metadataOffset)) != 0) {
      Algo.updateStaticAccessSlowPathRead(metadataOffset, siteID);
    }
  }

  @Entrypoint
  @Inline
  public static final void fieldReadBarrierStaticResolvedSamplingStats(Offset metadataOffset, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(Magic.getJTOC()).toAddress().loadInt(metadataOffset)) != 0) {
      Algo.updateStaticAccessSlowPathRead(metadataOffset, siteID);
    } else if (RaceDet.stats()) {
      Stats.readFastPath.inc();
    }
  }
  

  @Entrypoint
  @Inline
  public static final void fieldReadBarrierStaticResolvedVolatile(Offset metadataOffset) {
    Algo.readVolatile(Magic.getJTOC().toObjectReference(), metadataOffset);
  }
  
  @Entrypoint
  @Inline
  public static final void fieldReadBarrierStaticUnresolved(int fieldID, int siteID) {
    RVMField field = FieldReference.getMemberRef(fieldID).asFieldReference().getResolvedField();
    if (field.hasMetadataOffset()) {
      if (field.isVolatile()) {
        Algo.readVolatile(Magic.getJTOC().toObjectReference(), field.getMetadataOffset());
      } else if (RaceDet.algoBarriers()) {
        Algo.updateStaticAccess(field.getMetadataOffset(), true, siteID/*, ObjectReference.fromObject(field.getDeclaringClass()).toAddress()*/);
      }
    }
  }
  
  @Entrypoint
  @Inline
  public static final void fieldWriteBarrierStaticResolved(Offset metadataOffset, int siteID) {
    fieldWriteBarrierStaticResolvedSampling(metadataOffset, siteID, Sampling.samplingActivated);
  }
  
  @Entrypoint
  @Inline
  public static final void fieldWriteBarrierStaticResolvedSampling(Offset metadataOffset, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(Magic.getJTOC()).toAddress().loadInt(metadataOffset)) != 0) {
      Algo.updateStaticAccessSlowPathWrite(metadataOffset, siteID);
    }
  }
  
  @Entrypoint
  @Inline
  public static final void fieldWriteBarrierStaticResolvedSamplingStats(Offset metadataOffset, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(Magic.getJTOC()).toAddress().loadInt(metadataOffset)) != 0) {
      Algo.updateStaticAccessSlowPathWrite(metadataOffset, siteID);
    } else if (RaceDet.stats()) {
      Stats.writeFastPath.inc();
    }
  }
  
  @Entrypoint
  @Inline
  public static final void fieldWriteBarrierStaticResolvedVolatile(Offset metadataOffset) {
    Algo.writeVolatile(Magic.getJTOC().toObjectReference(), metadataOffset);
  }
  
  @Entrypoint
  @Inline
  public static final void fieldWriteBarrierStaticUnresolved(int fieldID, int siteID) {
    RVMField field = FieldReference.getMemberRef(fieldID).asFieldReference().getResolvedField();
    if (field.hasMetadataOffset()) {
      if (field.isVolatile()) {
        Algo.writeVolatile(Magic.getJTOC().toObjectReference(), field.getMetadataOffset());
      } else if (RaceDet.algoBarriers()) {
        Algo.updateStaticAccess(field.getMetadataOffset(), false, siteID/*, ObjectReference.fromObject(field.getDeclaringClass()).toAddress()*/);
      }
    }
  }
  
  // arrays:
  
  @Entrypoint
  @Inline
  public static final void arrayReadBarrier(Object o, int arrayIndex, int siteID) {
    arrayReadBarrierSampling(o, arrayIndex, siteID, Sampling.samplingActivated);
  }
  
  @Entrypoint
  @Inline
  public static final void arrayReadBarrierSampling(Object o, int arrayIndex, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(o).toAddress().loadInt(MiscHeader.FIELD_METADATA_OFFSET)) != 0) {
      Algo.updateObjectMetadataNoInlineRead(o, arrayIndex, siteID);
    }
  }
  
  @Entrypoint
  @Inline
  public static final void arrayReadBarrierSamplingStats(Object o, int arrayIndex, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(o).toAddress().loadInt(MiscHeader.FIELD_METADATA_OFFSET)) != 0) {
      Algo.updateObjectMetadataNoInlineRead(o, arrayIndex, siteID);
    } else if (RaceDet.stats()) {
      Stats.readFastPath.inc();
    }
  }
  
  @Entrypoint
  @Inline
  public static final void arrayReadBarrierUsePassedMetadata(Object o, int arrayIndex, int siteID) {
    if (o != null) {
      Algo.updateObjectMetadataNoInlineRead(o, arrayIndex, siteID);
    }
  }

  @Entrypoint
  @Inline
  public static final void arrayWriteBarrier(Object o, int arrayIndex, int siteID) {
    arrayWriteBarrierSampling(o, arrayIndex, siteID, Sampling.samplingActivated);
  }
  
  @Entrypoint
  @Inline
  public static final void arrayWriteBarrierSampling(Object o, int arrayIndex, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(o).toAddress().loadInt(MiscHeader.FIELD_METADATA_OFFSET)) != 0) {
      Algo.updateObjectMetadataNoInlineWrite(o, arrayIndex, siteID);
    }
  }
  
  @Entrypoint
  @Inline
  public static final void arrayWriteBarrierSamplingStats(Object o, int arrayIndex, int siteID, int samplingActivated) {
    if ((samplingActivated | ObjectReference.fromObject(o).toAddress().loadInt(MiscHeader.FIELD_METADATA_OFFSET)) != 0) {
      Algo.updateObjectMetadataNoInlineWrite(o, arrayIndex, siteID);
    } else if (RaceDet.stats()) {
      Stats.writeFastPath.inc();
    }
  }
  
  @Entrypoint
  @Inline
  public static final void arrayWriteBarrierUsePassedMetadata(Object o, int arrayIndex, int siteID) {
    if (o != null) {
      Algo.updateObjectMetadataNoInlineWrite(o, arrayIndex, siteID);
    }
  }
  
  /*
  @Inline
  static final Word loadMetadata(Address addr, Offset metadataOffset) {
    return addr.loadWord(metadataOffset);
  }

  @Inline
  static final void storeMetadata(Address addr, Word metadata, Offset metadataOffset, boolean writeBarrier) {
    if (writeBarrier) {
      storeMetadataWriteBarrier(addr, metadata, metadataOffset);
    } else {
      addr.store(metadata, metadataOffset);
    }
  }

  @NoInline
  private static final void storeMetadataWriteBarrier(Address addr, Word metadata, Offset metadataOffset) {
    MemoryManager.putfieldWriteBarrier(addr.toObjectReference().toObject(), metadata.toAddress().toObjectReference().toObject(), metadataOffset, 0);
  }
  */
  
  /*
  @Inline
  static final boolean storeMetadataAtomic(Address addr, Word oldMetadata, Word metadata, Offset metadataOffset, boolean writeBarrier) {
    if (writeBarrier) {
      return storeMetadataAtomicWriteBarrier(addr, oldMetadata, metadata, metadataOffset);
    } else {
      if (RaceDet.useNeededCAS()) {
        return addr.attempt(oldMetadata, metadata, metadataOffset);
      } else {
        storeMetadata(addr, metadata, metadataOffset, writeBarrier);
        return true;
      }
    }
  }

  @NoInline
  static final boolean storeMetadataAtomicWriteBarrier(Address addr, Word oldMetadata, Word metadata, Offset metadataOffset) {
    if (RaceDet.useNeededCAS()) {
      return MemoryManager.tryCompareAndSwapWriteBarrier(addr.toObjectReference().toObject(), metadataOffset, oldMetadata.toAddress().toObjectReference().toObject(), metadata.toAddress().toObjectReference().toObject());
    } else {
      storeMetadataWriteBarrier(addr, metadata, metadataOffset);
      return true;
    }
  }
  */
  
}
