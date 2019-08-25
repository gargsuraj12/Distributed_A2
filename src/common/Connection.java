package common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Connection {
	public Socket getClientTCPConnection(InetAddress remoteIP, int remotePort, InetAddress localIP, int localPort) {
		Socket socket = null;
		try {
			socket = new Socket(remoteIP, remotePort, localIP, localPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return socket;
	}
	
	public boolean closeConnection(Socket socket) {
		try {
			if(socket.isConnected()) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}
