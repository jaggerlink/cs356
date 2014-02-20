import java.util.ArrayList;
import java.io.*;

public class DataInfo {
	private ArrayList<String> raceDetect = new ArrayList<>();
	private ArrayList<String> raceDetStat = new ArrayList<>();
	private String eSampling = null;	
	private String numRace = null;
	
	//write effective sampling text file
	public void writeES() {
		try {
			File file = new File("EfficientSampling.txt");
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(eSampling);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//write race detection text file
	public void writeRD() {
		try {
			File file = new File("RaceDetected.txt");
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			for(int i = 0; i < raceDetect.size(); i++) {
				output.write(raceDetect.get(i));
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//write number of races text file
	public void writeNR() {
		try {
			File file = new File("NumberOfRaces.txt");
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(numRace);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//write RaceDet stats text file
	public void writeRDS() {
		try {
			File file = new File("RaceDetStat.txt");
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			for(int i = 0; i < raceDetStat.size(); i++) {
				output.write(raceDetStat.get(i));
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	public void writeFile(int i) {
		switch(i) {
			case 1:	// Effective Sampling
					writeES();
					break;
			case 2: // Race Detection
					writeRD();
					break;
			case 3: // Number of Races
					writeNR();
					break;
			case 4: // RaceDet Stat
					writeRDS();
					break;
		}
	}
}
