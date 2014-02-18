import java.util.ArrayList;

public class DataInfo {
	private static ArrayList<String> effSampling = new ArrayList<>();
	private static ArrayList<String> raceDetect = new ArrayList<>();
	private static ArrayList<String> numRace = new ArrayList<>();
	private static ArrayList<String> raceDetStat = new ArrayList<>();
	
	public ArrayList<String> getEffSampling(){
		return effSampling;
	}
	public ArrayList<String> getRaceDetect() {
		return raceDetect;
	}
	public ArrayList<String> getNumRace() {
		return numRace;
	}
	public ArrayList<String> getRaceDetStat() {
		return raceDetStat;
	}
	
	public void addEffSampling(String s) {
		effSampling.add(s);
		System.out.println(s);
	}
	public void addRaceDetect(String s) {
		raceDetect.add(s);
		System.out.println(s);
	}
	public void addNumRace(String s) {
		numRace.add(s);
		System.out.println(s);
	}
	public void addRaceDetStat(String s) {
		raceDetStat.add(s);
		System.out.println(s);
	}
}