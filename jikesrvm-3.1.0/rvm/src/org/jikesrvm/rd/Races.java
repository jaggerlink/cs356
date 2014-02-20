package org.jikesrvm.rd;

import java.io.PrintStream;
import java.util.HashSet;

import org.jikesrvm.Callbacks;
import org.jikesrvm.VM;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.scheduler.SpinLock;
import org.jikesrvm.util.HashMapRVM;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.NoInline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;
import org.vmmagic.unboxed.ObjectReference;

@Uninterruptible
public final class Races {
  
  private static HashMapRVM<Race,Race> races;
  private static final SpinLock racesLock = new SpinLock();
  public static PacerData socketData;
  private static SocketClient socketOut;
  
  @Interruptible
  static final void boot() {
    races = new HashMapRVM<Race,Race>();
    socketData = new PacerData();
    socketOut = new SocketClient();
  }
  
  @UninterruptibleNoWarn
  static final Race getRace(Race race) {
    racesLock.lock();
    Race existingRace = races.get(race);
    if (existingRace == null) {
      races.put(race, race);
      racesLock.unlock();
      return race;
    }
    racesLock.unlock();
    return existingRace;
  }
  
  @NoInline
  static final void race(int mySiteID, HashSet<Site> prevSites) {
    if (RaceDet.recordRaces()) {
      Site currSite = Site.getSite(mySiteID);
      recordRace(prevSites, currSite);
    }
  }
  
  // RaceDet: LATER: this doesn't count the number of distinct occurrences of a prev site, so Pacer might find
  // slightly higher *dynamic, pairwise* races than expected -- still seems like a fair measurement, however
  
  @UninterruptibleNoWarn
  @NoInline
  static final HashSet<Site> getPrevSites(int mySiteID, boolean prevIsRead, String raceType, Access prevAccess, VC currVC, HashSet<Site> prevSites) {
    if (RaceDet.recordRaces()) {
      MemoryManager.setAllocatingInBarrier(true);
      if (prevSites == null) {
        prevSites = new HashSet<Site>();
      }
      if (!prevIsRead || prevAccess.hasReadEpoch()) {
        int prevSiteID = (prevIsRead ? prevAccess.readSiteID : prevAccess.writeSiteID);
        Site prevSite = Site.getSite(prevSiteID);
        prevSites.add(prevSite);
      } else {
        ReadElementMap readVector = prevAccess.readVector;
        for (ReadElement prev : readVector.getData()) {
          for (; prev != null; prev = prev.next) {
            int tid = prev.tid;
            boolean guilty = (tid >= currVC.clocks.length || prev.clock > currVC.clocks[tid]);
            if (guilty) {
              Site prevSite = Site.getSite(prev.siteID);
              prevSites.add(prevSite);
            }
          }
        }
      }
      MemoryManager.setAllocatingInBarrier(false);
    }
    if (RaceDet.reportRaces()) {
      reportRace(mySiteID, prevIsRead, raceType, prevAccess, currVC, false/*isNew*/, null);
    }
    return prevSites;
  }
  
  @NoInline
  private static final boolean recordRace(HashSet<Site> prevSites, Site currSite) {
    if (prevSites != null) {
      Race race = Race.getRace(prevSites, currSite);
      boolean isNew = (race.getCount() == 0);
      race.incCount();
      return isNew;
    } else {
      return false;
    }
  }
  
  @NoInline
  private static final void reportRace(int mySiteID, boolean prevIsRead, String raceType, Access prevAccess, VC currVC, boolean isNew, ObjectReference object) {
    Site mySite = Site.getSite(mySiteID);

    int myTid = RVMThread.getCurrentThread().getRaceDetID();

    RaceDet.outputLock.lock();
    
    VM.write(raceType);
    VM.write(" race detected:");
    VM.writeln();

    VM.write("  Curr: T");
    VM.writeInt(myTid);
    VM.write(" at ");
    mySite.vmWrite(true);
    if (RaceDet.trackObjects()) {
      VM.writeln();
      VM.write("  Object = ");
      VM.write(object);
      if (!mySite.isStatic) {
        VM.write("; objectCount = ");
        VM.writeInt(TrackAllocSites.getObjectCount(object));
      }
    }
    if (RaceDet.trackAllocSites()) {
      if (!mySite.isStatic) {
        int site = TrackAllocSites.getAllocSite(object);
        int objectCount = TrackAllocSites.getAllocSiteCount(object);
        int latestCount = TrackAllocSites.getAllocSiteCount(site);
        VM.writeln();
        VM.write("  allocSite = ");
        VM.writeHex(site);
        VM.write("; objectCount = ");
        VM.writeInt(objectCount);
        VM.write("; latestCount = ");
        VM.writeInt(latestCount);
      }
    }
    VM.writeln();

    if (!prevIsRead || prevAccess.hasReadEpoch()) {
      int prevTid = (prevIsRead ? prevAccess.readTid : prevAccess.writeTid);
      int prevSiteID = (prevIsRead ? prevAccess.readSiteID : prevAccess.writeSiteID);
      writePrevAccess(prevTid, prevSiteID, true);
    } else {
      ReadElementMap readVector = prevAccess.readVector;
      for (ReadElement prev : readVector.getData()) {
        for (; prev != null; prev = prev.next) {
          int tid = prev.tid;
          boolean guilty = (tid >= currVC.clocks.length || prev.clock > currVC.clocks[tid]);
          if (guilty) {
            writePrevAccess(tid, prev.siteID, guilty);
          }
        }
      }
    }

    VM.write("  ");
    if (!prevIsRead) {
      write(prevAccess.writeClock, prevAccess.writeTid);
    } else if (prevAccess.hasReadEpoch()) {
      write(prevAccess.readClock, prevAccess.readTid);
    } else {
      write(prevAccess.readVector);
    }
    VM.write(" doesn't HB ");
    currVC.write();
    VM.writeln();

    if (RaceDet.stats() && isNew) {
      RVMThread.dumpStack();
    }
    
    RaceDet.outputLock.unlock();
  }
  
  static final void writePrevAccess(int prevTid, int prevSiteID, boolean guilty) {
    if (guilty) {
      VM.write(" *Prev: T");
    } else {
      VM.write("  Prev: T");
    }
    VM.writeInt(prevTid);
    VM.write(" at ");
    if (RaceDet.trackTimes()) {
      // first commented-out block tracks number of GCs
      // second commented-out block tracks time elapsed
      /*
      int majorCount = SiteWord.getMajorGCs(prevSiteID);
      int minorCount = SiteWord.getMinorGCs(prevSiteID);
      VM.write(majorCount);
      VM.write(" major, ");
      VM.write(minorCount);
      VM.write(" minor GCs ago");
      */
      /*
      double seconds = ThreadSite.getSeconds(prevSiteID);
      VM.write(seconds, 8);
      VM.write(" seconds ago");
      */
    } else {
      if (prevSiteID == 0) {
        VM.write("UNKNOWN SITE");
      } else {
        Site prevSite = Site.getSite(prevSiteID);
        prevSite.vmWrite(false);
      }
    }
    VM.writeln();
  }
  
  static void write(int clock, int tid) {
    VM.writeInt(clock);
    VM.write("@");
    VM.writeInt(tid);
  }
  
  static void write(ReadElementMap readVector) {
    int maxTid = 0;
    for (ReadElement prev : readVector.getData()) {
      for (; prev != null; prev = prev.next) {
        int tid = prev.tid;
        if (tid > maxTid) {
          maxTid = tid;
        }
      }
    }
    String delim = "";
    VM.write("< ");
    for (int tid = 0; tid <= maxTid; tid++) {
      VM.write(delim);
      ReadElement elem = readVector.get(tid);
      VM.writeInt(elem == null ? 0 : elem.clock);
      delim = ", ";
    }
    VM.write(" >");
  }
  
  @Interruptible
  static void postReport(PrintStream ps) {
    int totalDynamic = 0;
    for (Race race : races.keys()) {
      int count = race.getCount();
      totalDynamic += count;
      //TODO
      //ps.println("RACE: " + race + " (count = " + count + ")");
      //ps.println();
      race.storeData();
    }
    //TODO
    //ps.println("Races: " + races.size() + " distinct, " + totalDynamic + " dynamic");
    //ps.println();
    socketData.setTotalDynamicRaces(races.size(), totalDynamic);
  }
  
  static {
    if (RaceDet.recordRaces()) {
      Callbacks.addExitMonitor(new Callbacks.ExitMonitor() {
        public void notifyExit(int value) {
          postReport(System.out);
          new Thread(socketOut).start();
        }
      });
    }
  }
}
