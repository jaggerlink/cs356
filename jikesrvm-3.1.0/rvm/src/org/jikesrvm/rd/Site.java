package org.jikesrvm.rd;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.NormalMethod;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.compilers.opt.ir.Instruction;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.rd.LiteRace.LiteRaceInfo;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.util.ImmutableEntryHashMapRVM;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

/** RaceDet: represents a source code location */
@Uninterruptible
public final class Site {

  // these uniquely describe the site
  public final NormalMethod method;
  public final int bci;
  
  // not part of uniqueness, but included in persistent version
  public final int lineNumber;
  public final boolean isRead;
  public final boolean isStatic;
  public final Atom type;
  final LiteRaceInfo[] liteRaceInfos;
  
  public int count;
  public final ImmutableEntryHashMapRVM<Integer,Integer> contextCount = null; // = new HashMapRVM<Integer,Integer>();
  
  private static int currentID = 1; // don't use 0
  private static Site[] siteArray = new Site[1024];
  private static ImmutableEntryHashMapRVM<Site,Site> siteMap;

  @Interruptible
  static final void boot() {
    siteMap = new ImmutableEntryHashMapRVM<Site,Site>();
  }
  
  @UninterruptibleNoWarn
  public static int getSite(Instruction inst, boolean isRead, boolean isStatic, TypeReference type) {
    return getSite(inst.position.method, inst.bcIndex, isRead, isStatic, type);
  }
  
  /** Create a site that will be used with instrumentation in this run */
  @UninterruptibleNoWarn
  public static int getSite(NormalMethod method, int bci, boolean isRead, boolean isStatic, TypeReference type) {
    Site site = new Site(method, bci, isRead, isStatic, type);
    synchronized (siteMap) {
      //sitesLock.lock();
      Site existingSite = siteMap.get(site);
      if (existingSite == null) {
        siteMap.put(site, site);
      } else {
        site = existingSite;
      }
      // unique ID for this site
      int id = currentID++;
      // grow the array if needed
      if (id == siteArray.length) {
        Site[] newArray = new Site[siteArray.length * 2];
        System.arraycopy(siteArray, 0, newArray, 0, siteArray.length);
        siteArray = newArray;
      }
      siteArray[id] = site;
      //sitesLock.unlock();
      return id;
    }
  }
  
  /** Create a site that will be used with instrumentation in this run */
  private Site(NormalMethod method, int bci, boolean isRead, boolean isStatic, TypeReference type) {
    this.method = method;
    this.bci = bci;
    this.lineNumber = method.getLineNumberForBCIndex(bci);
    this.isRead = isRead;
    this.isStatic = isStatic;
    this.type = type.getName();
    if (RaceDet.liteRace() && !RaceDet.liteRaceMethod()) {
      liteRaceInfos = LiteRaceInfo.newArray();
    } else {
      liteRaceInfos = null;
    }
  }

  static final Site getSite(int id) {
    return siteArray[id];
  }

  @UninterruptibleNoWarn
  static final void incCount(int id) {
    Site site = getSite(id);
    site.count++;
  }
  
  @Override
  public final boolean equals(Object o) {
    Site site = (Site)o;
    return site.method == this.method &&
           site.bci == this.bci;
  }
  
  @Override
  public final int hashCode() {
    return method.getId() + bci;
  }

  @Interruptible
  @Override
  public final String toString() {
    return method.getDeclaringClass().getDescriptor() + "." + method.getName() + ":" + lineNumber + " (" + bci + ") (" + (isRead ? "R" : "W") + (isStatic ? " static" : "") + " " + type + ")";
  }
  
  public void vmWrite(boolean isCurrSite) {
    method.getDeclaringClass().getDescriptor().sysWrite();
    VM.write(".");
    method.getName().sysWrite();
    VM.write(":");
    VM.write(lineNumber);
    VM.write(" (");
    VM.write(isRead ? "R" : "W");
    if (isStatic) {
      VM.write(" static");
    }
    VM.write(" ");
    type.sysWrite();
    VM.write(") count = ");
    VM.writeInt(count);
  }

}
