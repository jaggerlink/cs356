import java.io.File;
import java.io.IOException;


public class PBTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String rvmPath = "/home/joshua/pacer/jikesrvm-3.1.0/dist/FastAdaptiveGenImmix_rdSamplingStats_ia32-linux/";
		//ProcessBuilder pb = new ProcessBuilder("gedit", "./src/PBTest.java", "+10");
		//ProcessBuilder pb = new ProcessBuilder("xterm", "-e", "vi", "./src/PBTest.java", "+5");
		ProcessBuilder pb = new ProcessBuilder("xterm", "-hold", "-e", rvmPath + "rvm", "simpleRace");
		pb.directory(new File("/home/joshua/Documents"));
		try {
			pb.start();
			//Runtime.getRuntime().exec("xterm -e vi ./src/PBTest.java +5");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
