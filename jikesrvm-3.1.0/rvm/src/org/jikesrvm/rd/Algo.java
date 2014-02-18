package org.jikesrvm.rd;

import java.util.HashSet;

import org.jikesrvm.Constants;
import org.jikesrvm.VM;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.NoInline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/** RaceDet: logic for race detection algorithms */
@Uninterruptible
public final class Algo implements Constants {

  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  static final void updateObjectMetadata(Object o, int fieldNumber, boolean isRead, int siteID) {
    ObjectMetadata metadata = GetMetadata.getMetadataNoSync(o);
    if (Sampling.recordAccesses() || (metadata != null && metadata.hasAccess(fieldNumber))) {
      updateObjectMetadataSlowPath(o, fieldNumber, isRead, siteID);
    }
    // stats    
    else if (RaceDet.stats()) {
      if (isRead) {
        Stats.readFastPath.inc();
      } else {
        Stats.writeFastPath.inc();
      }
    }
  }

  @NoInline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  static final void updateObjectMetadataNoInlineRead(Object o, int fieldNumber, int siteID) {
    updateObjectMetadata(o, fieldNumber, true, siteID);
  }
  
  @NoInline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  static final void updateObjectMetadataNoInlineWrite(Object o, int fieldNumber, int siteID) {
    updateObjectMetadata(o, fieldNumber, false, siteID);
  }
  
  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  static final void updateObjectMetadataSlowPath(Object o, int fieldNumber, boolean isRead, int siteID) {
    ObjectMetadata metadata = GetMetadata.lockMetadata(o, Sampling.recordAccesses());
    if (metadata != null) {
      Access access = metadata.getAccess(fieldNumber, Sampling.recordAccesses());
      if (access != null) {
        if (isRead) {
          access = read(access, siteID);
        } else {
          access = write(access, siteID);
        }
        if (VM.VerifyAssertions) { VM._assert(access != null || Sampling.deleteAccesses()); }
        if (access == null) {
          metadata = metadata.removeAccess(fieldNumber);
        }
        if (VM.VerifyAssertions) { VM._assert(metadata != null || Sampling.deleteAccesses()); }
      }
    }
    GetMetadata.unlockMetadata(o, metadata);
  }
  
  @NoInline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  static final void updateStaticAccessSlowPathRead(Offset metadataOffset, int siteID) {
    updateStaticAccessSlowPath(metadataOffset, true, siteID);
    
  }
  
  @NoInline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  static final void updateStaticAccessSlowPathWrite(Offset metadataOffset, int siteID) {
    updateStaticAccessSlowPath(metadataOffset, false, siteID);
  }

  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  static final void updateStaticAccess(Offset metadataOffset, boolean isRead, int siteID) {
    Access access = GetMetadata.getAccessNoSync(metadataOffset);
    if (Sampling.recordAccesses() || access != null) {
      updateStaticAccessSlowPath(metadataOffset, isRead, siteID);
    } else if (RaceDet.stats()) {
      if (isRead) {
        Stats.readFastPath.inc();
      } else {
        Stats.writeFastPath.inc();
      }
    }
  }

  // RaceDet: LATER: deal with this -- maybe just ignore certain races instead?
  // create a happens-before edge between static initializers and class variable uses
  /*
  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  static final void updateStaticAccess(Offset metadataOffset, boolean isRead, int siteID, Address rvmClass) {
    if (VM.rdStaticInitHappensBefore) {
      Algo.acquire(rvmClass.toObjectReference().toObject());
    }
  }
  */
  
  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  private static final void updateStaticAccessSlowPath(Offset metadataOffset, boolean isRead, int siteID) {
    Access access = GetMetadata.lockAccess(metadataOffset, Sampling.recordAccesses());
    if (access != null) {
      if (isRead) {
        access = read(access, siteID);
      } else {
        access = write(access, siteID);
      }
      if (VM.VerifyAssertions) { VM._assert(access != null || Sampling.deleteAccesses()); }
    }
    GetMetadata.unlockAccess(metadataOffset, access);
  }

  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  private static final Access read(Access access, int siteID) {
    
    // check if LiteRace-based sampling would sample this read
    if (RaceDet.liteRace()) {
      boolean result = LiteRace.sample(siteID);
      if (!result) {
        return access;
      }
    }
    
    //[FT READ SAME EPOCH]
    if (access.hasReadEpoch() &&
        access.readTid == RVMThread.getCurrentThread().getRaceDetID() &&
        access.readClock == RVMThread.getCurrentThread().myClock) {
      // don't need to check for any races or update access info, but we can clear some/all info?
      if (RaceDet.stats()) { Stats.readSameEpoch.inc(); }
      // if there's no write info, then we have no read or write info
      if (Sampling.deleteAccesses() && !access.hasWriteInfo()) {
        return null;
      } else {
        return access;
      }
    } else {
      return readSlowPath(access, siteID);
    }
  }
  
  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  private static final Access readSlowPath(Access access, int siteID) {
    // finalizer and compiler threads may get here; just exit if so
    if (!RaceDet.isApplicableThread()) {
      return access;
    }

    VC threadVC = RVMThread.getCurrentThread().vectorClock;
    int myClock = RVMThread.getCurrentThread().myClock;
    int myTid = RVMThread.getCurrentThread().getRaceDetID();
    
    // check for W-R race
    if (!VC.lessThan(access.writeTid, access.writeClock, threadVC)) {
      HashSet<Site> prevSites = Races.getPrevSites(siteID, false, "W-R", access, threadVC, null);
      Races.race(siteID, prevSites);
    }
    // [FT READ SHARED]: read set is a vector clock
    if (!access.hasReadEpoch()) {
      if (RaceDet.stats()) { Stats.readShared.inc(); }
      ReadElementMap readVector = access.readVector;
      if (Sampling.deleteAccesses()) {
        // just remove the element
        if (VM.VerifyAssertions) { VM._assert(readVector.size() >= 2); }
        readVector.removeIfPresent(myTid);
        
        // don't do this because it's an O(n)-time operation:
        // remove elements that happen before given clock
        /*
        ReadElement[] oldData = readVector.getData();
        readVector = ReadElementMap.newReadElementMap();
        access.readVector = readVector;
        for (ReadElement elem : oldData) {
          ReadElement next;
          for (; elem != null; elem = next) {
            next = elem.next;
            if (elem.tid >= threadVC.clocks.length ||
                elem.clock > threadVC.clocks[elem.tid]) {
              readVector.put(elem);
            }
          }
        }
        */
        
        if (VM.VerifyAssertions) { VM._assert(readVector.size() >= 1); }
        if (readVector.size() == 1) {
          ReadElement onlyElem = readVector.getFirst();
          access.readTid = onlyElem.tid;
          access.readClock = onlyElem.clock;
          access.readSiteID = onlyElem.siteID;
          access.readVector = null;
        }
      } else {
        ReadElement readElem = readVector.get(myTid);
        if (readElem == null) {
          readElem = ReadElement.newReadElement(myTid, myClock, siteID);
          readVector.put(readElem);
        } else {
          readElem.clock = myClock;
          readElem.siteID = siteID;
        }
      }
    // [FT READ EXCLUSIVE]: keep using read epoch
    } else if (VC.lessThan(access.readTid, access.readClock, threadVC)) {
      if (RaceDet.stats()) { Stats.readExclusive.inc(); }
      if (Sampling.deleteAccesses()) {
        if (!access.hasWriteInfo()) {
          return null;
        }
        // let's clear the read info, since FastTrack would overwrite it
        access.readTid = 0;
        access.readClock = 0;
        access.readSiteID = -1;
      } else {
        access.readTid = myTid;
        access.readClock = myClock;
        access.readSiteID = siteID;
      }
    // [FT READ SHARE]: create read vector clock
    } else {
      readVerySlowPath(access, siteID);
    }
    return access;
  }

  @NoInline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  private static final void readVerySlowPath(Access access, int siteID) {
    int myClock = RVMThread.getCurrentThread().myClock;
    int myTid = RVMThread.getCurrentThread().getRaceDetID();
    if (RaceDet.stats()) { Stats.readShare.inc(); }
    if (Sampling.deleteAccesses()) {
      // we can't delete the existing access (it's concurrent with current access),
      // but we don't record the current access
    } else {
      ReadElementMap readVector = ReadElementMap.newReadElementMap();
      readVector.put(ReadElement.newReadElement(access.readTid, access.readClock, access.readSiteID));
      readVector.put(ReadElement.newReadElement(myTid, myClock, siteID));
      access.readVector = readVector;
    }
  }
  
  @NoInline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  private static final Access write(Access access, int siteID) {
    
    // check if LiteRace-based sampling would sample this read
    if (RaceDet.liteRace()) {
      boolean result = LiteRace.sample(siteID);
      if (!result) {
        return access;
      }
    }
    
    // [FT WRITE SAME EPOCH]
    if (access.writeTid == RVMThread.getCurrentThread().getRaceDetID() &&
        access.writeClock == RVMThread.getCurrentThread().myClock) {
      if (RaceDet.stats()) { Stats.writeSameEpoch.inc(); }
    } else {
      writeSlowPath(access, siteID);
    }
    
    // we always want to throw away all access info after a write
    if (Sampling.deleteAccesses()) {
      return null;
    } else {
      return access;
    }
  }

  // RaceDet: LATER: handle cases where compiler calls app (here and in readSlowPath) and vectorClock == null / racedet ID is invalid / whatever
  // For example, this leads to issues with VC.lessThan(int, int, VC) crashing when running antlr
  
  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  private static final void writeSlowPath(Access access, int siteID) {
    // finalizer and compiler threads may get here; just exit if so
    if (!RaceDet.isApplicableThread()) {
      return;
    }

    VC threadVC = RVMThread.getCurrentThread().vectorClock;
    int myClock = RVMThread.getCurrentThread().myClock;
    int myTid = RVMThread.getCurrentThread().getRaceDetID();
    // check for W-W race
    HashSet<Site> prevSites = null;
    if (!VC.lessThan(access.writeTid, access.writeClock, threadVC)) {
      prevSites = Races.getPrevSites(siteID, false, "W-W", access, threadVC, prevSites);
    }
    // [FT WRITE EXCLUSIVE]: read set is an epoch
    if (access.hasReadEpoch()) {
      if (RaceDet.stats()) { Stats.writeExclusive.inc(); }
      // check for R-W race
      if (!VC.lessThan(access.readTid, access.readClock, threadVC)) {
        prevSites = Races.getPrevSites(siteID, true, "R-W", access, threadVC, prevSites);
      }
      // an extension to FastTrack: discard the read info even though it's an epoch
      if (!Sampling.deleteAccesses()) {
        access.readClock = 0;
        access.readTid = 0;
        access.readSiteID = -1; // not necessary but may help debugging
      }
    // [FT WRITE SHARED]: read set is a vector clock
    } else {
      prevSites = writeVerySlowPath(access, siteID, prevSites);
    }
    if (prevSites != null) {
      Races.race(siteID, prevSites);
    }
    if (!Sampling.deleteAccesses()) {
      access.writeTid = myTid;
      access.writeClock = myClock;
      access.writeSiteID = siteID;
    }
  }
  
  @NoInline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  private static final HashSet<Site> writeVerySlowPath(Access access, int siteID, HashSet<Site> prevSites) {
    ReadElementMap readVector = access.readVector;
    VC threadVC = RVMThread.getCurrentThread().vectorClock;
    if (RaceDet.stats()) { Stats.writeShared.inc(); }
    if (!VC.lessThan(readVector, threadVC)) {
      prevSites = Races.getPrevSites(siteID, true, "R-W", access, threadVC, prevSites);
    }
    if (Sampling.deleteAccesses()) {
      // don't need to do anything; read/write metadata will be deleted shortly
    } else {
      access.readVector = null;
      access.readClock = 0;
      access.readTid = 0;
      access.readSiteID = -1; // not necessary but may help debugging
    }
    return prevSites;
  }
  
  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void readVolatile(ObjectReference object, Offset metadataOffset) {
    Address addr = object.toAddress().plus(metadataOffset);
    readVolatile(addr);
  }

  @NoInline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void readVolatile(Address addr) {
    // let's lock the clock for a little while
    VC volatileVC = GetMetadata.lockVolatileVC(addr);
    VC newVector = VC.join(RVMThread.getCurrentThread().vectorClock, volatileVC);
    if (newVector != RVMThread.getCurrentThread().vectorClock) {
      RVMThread.getCurrentThread().vectorClock = newVector;
    }
    if (RaceDet.stats()) { Stats.readVolatile.inc(); }
    // write and unlock the clock
    GetMetadata.unlockVolatileVC(addr, volatileVC);
  }

  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void writeVolatile(ObjectReference object, Offset metadataOffset) {
    Address addr = object.toAddress().plus(metadataOffset);
    writeVolatile(addr);
  }

  @NoInline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void writeVolatile(Address addr) {
    // let's lock the clock for a little while
    VC volatileVC = GetMetadata.lockVolatileVC(addr);
    // convert to a vector clock and make sure it's there and long enough
    volatileVC = VC.joinTargetIsVolatile(volatileVC, RVMThread.getCurrentThread().vectorClock);
    RVMThread.getCurrentThread().incrementClock();
    // write and unlock the clock
    GetMetadata.unlockVolatileVC(addr, volatileVC);
    if (RaceDet.stats()) { Stats.writeVolatile.inc(); }
  }

  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void acquire(Object o) {
    VC threadVC = RVMThread.getCurrentThread().vectorClock;
    VC newVector = VC.join(threadVC, GetMetadata.getLockVC(o));
    if (newVector != threadVC) {
      RVMThread.getCurrentThread().vectorClock = newVector;
    }
    if (RaceDet.stats()) { Stats.acquire.inc(); }
  }

  @Inline //@NoBoundsCheck @NoCheckStore @NoNullCheck
  public static final void release(Object o) {
    VC lockVC = GetMetadata.getLockVC(o);
    VC newLockVC = VC.copy(lockVC, RVMThread.getCurrentThread().vectorClock);
    if (newLockVC != lockVC) {
      GetMetadata.setLockVC(o, newLockVC);
    }
    RVMThread.getCurrentThread().incrementClock();
    if (RaceDet.stats()) { Stats.release.inc(); }
  }

  public static final void fork(RVMThread oldThread, RVMThread newThread) {
    if (oldThread.vectorClock == null) {
      // oldThread won't have a vector clock if it's a VM thread
    } else {
      newThread.vectorClock = VC.copyThreadVC(newThread.vectorClock, oldThread.vectorClock, newThread.getRaceDetID());
      oldThread.incrementClock();
    }
    if (RaceDet.stats()) {
      Stats.fork.inc();
      Stats.maxLiveThreads.inc();
    }
  }
  
  // i don't think we need join, since it's implemented using wait-notify
  
}
