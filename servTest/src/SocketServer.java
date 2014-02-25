import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.jikesrvm.rd.PacerData;;

public class SocketServer implements Runnable {

	public static void main(String[] args) {
		SocketServer server = new SocketServer();
		new Thread(server).start();
	}

	@Override
	public void run() {
		try {
			ServerSocket listener = new ServerSocket(8080);
			Socket socket = listener.accept();
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			try {
				PacerData test = (PacerData)input.readObject();
				System.out.println(test.getPriorRaceDescriptor(0) + " " + test.getPriorRaceLine(0));
				String testFix = test.getPriorRaceDescriptor(0).substring(1, test.getPriorRaceDescriptor(0).length() - 1) + ".java";
				ProcessBuilder pb = new ProcessBuilder("gedit", testFix, "+" + test.getPriorRaceLine(0));
				pb.directory(new File("/home/joshua/Documents"));
				try {
					pb.start();
					//Runtime.getRuntime().exec("xterm -e vi ./src/PBTest.java +5");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				System.out.println("Invalid Input.");
				e.printStackTrace();
			}
			
			listener.close();
		}
		catch (IOException e) {
			System.out.println("Something went wrong.");
			e.printStackTrace();
		}
	}

}
