package org.jikesrvm.rd;

import java.util.HashSet;

import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.scheduler.Synchronization;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;

@Uninterruptible
public final class Race {
  
  // order doesn't matter!
  final HashSet<Site> priorSites; 
  final Site currSite;
  
  @Entrypoint
  private int count;
  
  private Race(HashSet<Site> priorSites, Site currSite) {
    this.priorSites = priorSites;
    this.currSite = currSite;
  }
  
  @UninterruptibleNoWarn
  static final Race getRace(HashSet<Site> priorSites, Site currSite) {
    MemoryManager.setAllocatingInBarrier(true);
    Race race = Races.getRace(new Race(priorSites, currSite));
    MemoryManager.setAllocatingInBarrier(false);
    return race;
  }

  final void incCount() {
    Synchronization.fetchAndAdd(this, Entrypoints.rdRaceCountField.getOffset(), 1);
  }
  
  final int getCount() {
    return count;
  }
  
  @Override
  @UninterruptibleNoWarn
  public final boolean equals(Object o) {
    Race other = (Race)o;
    return priorSites.equals(other.priorSites) &&
           currSite == other.currSite;
  }

  @Override
  @UninterruptibleNoWarn
  public final int hashCode() {
    return priorSites.hashCode() + currSite.hashCode();
  }
  
  @Interruptible
  @Override
  public final String toString() {
    String delim = "";
    StringBuffer buf = new StringBuffer();
    for (Site priorSite : priorSites) {
      buf.append(delim);
      buf.append(priorSite);
      delim = ", ";
    }
    buf.append(" -> ");
    buf.append(currSite);
    return buf.toString();
  }
  
  public final void storeData() {	//TODO
	  for (Site priorSite : priorSites) {
		  priorSite.storeData(Races.socketData, false);
		  currSite.storeData(Races.socketData, true);
	  }
  }
}
