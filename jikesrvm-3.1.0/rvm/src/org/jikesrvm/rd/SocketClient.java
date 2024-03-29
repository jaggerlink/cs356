package org.jikesrvm.rd;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class SocketClient {
	Socket socket;
	ObjectOutputStream output;

	//public static void main(String[] args) {
	//	SocketClient client = new SocketClient();
	//	new Thread(client).start();
	//}

	public void run(PacerData theData) {
		try {
			socket = new Socket("localhost", 8080);
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(theData);
			//output.flush();
			output.close();
		}
		catch (IOException e) {
			System.out.println("Failed to open or write socket.");
		}
	}

}
