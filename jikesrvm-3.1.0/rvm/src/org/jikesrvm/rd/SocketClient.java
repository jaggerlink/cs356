package org.jikesrvm.rd;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient implements Runnable {
	Socket socket;
	ObjectOutputStream output;
	
	public SocketClient() {
		try {
			socket = new Socket("localhost", 823);
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException e) {
			System.out.println("Failed to open socket.");
		}
	}

	//public static void main(String[] args) {
	//	SocketClient client = new SocketClient();
	//	new Thread(client).start();
	//}

	@Override
	public void run() {
		try {
			output.writeObject(new String("Hi!"));
		}
		catch (IOException e) {
			System.out.println("Failed to write to socket.");
			e.printStackTrace();
		}
	}

}
