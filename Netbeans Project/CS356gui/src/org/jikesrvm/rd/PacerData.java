package org.jikesrvm.rd;

import java.io.Serializable;
import java.util.ArrayList;

public class PacerData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3556846890663854204L;

	class RaceData implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8091142242419345729L;
		String descriptor;
		String methodName;
		public int lineNumber;
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
		
		public String toString() {
			return descriptor + " " + methodName + " " + lineNumber + " " + bci;
		}
	}
	
	//static PacerData theData;
	ArrayList<RaceData> priorRaces;
	ArrayList<RaceData> currentRaces;
	int totalRaces;
	int totalDynamic;
	long samplingIncrements;
	long totalIncrements;
	ArrayList<String> raceDet;
	
	public PacerData() {
		//theData = new PacerData();
		priorRaces = new ArrayList<RaceData>();
		currentRaces = new ArrayList<RaceData>();
		raceDet = new ArrayList<String>();
	}
	
	
	public int getRaceSize() {
		return priorRaces.size();
	}
	public int getStatSize() {
		return raceDet.size();
	}
	public String getSR() {
		return "Sampling Increment:  " + samplingIncrements + "; Total Increments:  " + totalIncrements;
	}
	public String getNR() {
		return "RACES:  " + totalRaces + "distinct; " + totalDynamic + " dynamic";
	}
	public String getRace(int x) {
		return priorRaces.get(x) + ", " + currentRaces(x);
	}
	public String getDRS(int x) {
		return raceDet.get(x);
	}
	
	public void addPriorRace(String descriptor, String methodName, int lineNumber, int bci, boolean isRead, boolean isStatic, String type) {
		priorRaces.add(new RaceData(descriptor, methodName, lineNumber, bci, isRead, isStatic, type));
	}
	
	public void addCurrentRace(String descriptor, String methodName, int lineNumber, int bci, boolean isRead, boolean isStatic, String type) {
		currentRaces.add(new RaceData(descriptor, methodName, lineNumber, bci, isRead, isStatic, type));
	}
	
	public void setTotalDynamicRaces(int totalRaces, int totalDynamic) {
		this.totalRaces = totalRaces;
		this.totalDynamic = totalDynamic;
	}
	
	public void setIncrements(long samplingIncrements, long totalIncrements) {
		this.samplingIncrements = samplingIncrements;
		this.totalIncrements = totalIncrements;
	}
	
	public void addRaceDet(String raceDetVal) {
		raceDet.add(raceDetVal);
	}
	
	public String getPriorRaceDescriptor(int index) {
		return priorRaces.get(index).descriptor;
	}
	
	public int getPriorRaceLine(int index) {
		return priorRaces.get(index).lineNumber;
	}
	
	
	
}
