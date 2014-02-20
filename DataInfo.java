import java.util.ArrayList;
import java.io.*;

public class DataInfo {
	private ArrayList<String> raceDetect = new ArrayList<>();
	private ArrayList<String> raceDetStat = new ArrayList<>();
	private String eSampling = null;	
	private String numRace = null;
	
	//write effective sampling text file
	public void writeES() {
	}
	//write race detection text file
	public void writeRD() {
	}
	//write number of races text file
	public void writeNM() {
	}
	//write RaceDet stats text file
	public void writeRDS() {
	}
	
	//add the effective sampling rate to the string
	public void addES(String s) {
		eSampling = s;
	}
	//add the race detected lines to the arraylist
	public void addRD(String s) {
		raceDetect.add(s);
	}
	//add the number of races detected to the string
	public void addNM(String s) {
		numRace = s;
	}
	//add the RaceDet Stats to the arraylist
	public void addRDS(String s) {
		raceDetStat.add(s);
	}
}
