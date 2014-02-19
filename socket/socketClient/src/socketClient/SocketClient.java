package socketClient;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient implements Runnable {

	public static void main(String[] args) {
		SocketClient client = new SocketClient();
		new Thread(client).start();
	}

	@Override
	public void run() {
		try {
			Socket socket = new Socket("localhost", 823);
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(new String("Hi!"));
			
			socket.close();
		}
		catch (IOException e) {
			System.out.println("Something went wrong.");
			e.printStackTrace();
		}
	}

}
