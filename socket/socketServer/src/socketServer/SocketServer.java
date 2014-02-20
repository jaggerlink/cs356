package socketServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer implements Runnable {

	public static void main(String[] args) {
		SocketServer server = new SocketServer();
		new Thread(server).start();
	}

	@Override
	public void run() {
		try {
			ServerSocket listener = new ServerSocket(823);
			Socket socket = listener.accept();
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			try {
				PacerData test = (PacerData)input.readObject();
				System.out.println(test.totalRaces);
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
