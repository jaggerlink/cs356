package socketServer;

import java.io.Serializable;
import java.util.ArrayList;

public class PacerData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3556846890663854204L;

	public class RaceData {
		String descriptor;
		String methodName;
		int lineNumber;
		int bci;
		boolean isRead;
		boolean isStatic;
		String type;
		
		public RaceData(String descriptor, String methodName, int lineNumber, int bci, boolean isRead, boolean isStatic, String type) {
			this.descriptor = descriptor;
			this.methodName = methodName;
			this.lineNumber = lineNumber;
			this.bci = bci;
			this.isRead = isRead;
			this.isStatic = isStatic;
		}
		
	}
	
	static PacerData theData;
	ArrayList<RaceData> priorRaces;
	ArrayList<RaceData> currentRaces;
	int totalRaces;
	int totalDynamic;
	long samplingIncrements;
	long totalIncrements;
	ArrayList<String> raceDet;
	
	private static void init() {
		theData = new PacerData();
		theData.priorRaces = new ArrayList<RaceData>();
		theData.currentRaces = new ArrayList<RaceData>();
		theData.raceDet = new ArrayList<String>();
	}
	
	
	public void addPriorRace(String descriptor, String methodName, int lineNumber, int bci, boolean isRead, boolean isStatic, String type) {
		if(theData == null) {
			init();
		}
		theData.priorRaces.add(new RaceData(descriptor, methodName, lineNumber, bci, isRead, isStatic, type));
	}
	
	public void addCurrentRace(String descriptor, String methodName, int lineNumber, int bci, boolean isRead, boolean isStatic, String type) {
		if(theData == null) {
			init();
		}
		theData.currentRaces.add(new RaceData(descriptor, methodName, lineNumber, bci, isRead, isStatic, type));
	}
	
	public void setTotalDynamicRaces(int totalRaces, int totalDynamic) {
		if(theData == null) {
			init();
		}
		theData.totalRaces = totalRaces;
		theData.totalDynamic = totalDynamic;
	}
	
	public void setIncrements(long samplingIncrements, long totalIncrements) {
		if(theData == null) {
			init();
		}
		theData.samplingIncrements = samplingIncrements;
		theData.totalIncrements = totalIncrements;
	}
	
	public void addRaceDet(String raceDet) {
		if(theData == null) {
			init();
		}
		theData.raceDet.add(raceDet);
	}
	
	
	
}
