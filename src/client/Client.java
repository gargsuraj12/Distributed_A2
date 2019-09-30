package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import common.Connection;
import common.Constants;
import common.Messages;

public class Client {
	static InetAddress remoteIP;
	static int remotePort;
	static InetAddress localIP;
	static int localPort;

	private void executeTCPClient() {
		Scanner scanner = new Scanner(System.in);
		String command = null, response = null, input = null;
		String[] tokens = null;
		Connection conn = new Connection();
		Socket socket = conn.getClientTCPConnection(Client.remoteIP, Client.remotePort, Client.localIP,
				Client.localPort);
		try {
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

			ClientUtilities cu = new ClientUtilities(ois, oos);
			while (true) {
				cu.checkAndPrintMessages();
				//System.out.print(ois.readObject());
				System.out.print(Constants.ENTER_COMMAND);
				input = scanner.nextLine();
				
				// Sending command to server
				oos.writeObject(input);
				
				tokens = input.split(" ");
				command = tokens[0];
				
				if (command.equalsIgnoreCase(Constants.CREATE_USER) && tokens.length == 2) {
					response = cu.getResponseFromServer();
					System.out.println("From Server: " + response);
					
				} else if (command.equalsIgnoreCase(Constants.UPLOAD_FILE) && tokens.length == 2) {
					response = cu.uploadFile(tokens[1]);
					System.out.println("From Server: " + response);
					
				} else if (command.equalsIgnoreCase(Constants.UPLOAD_FILE_UDP) && tokens.length == 2) {
					response = cu.getResponseFromServer();
					System.out.println("From Server: " + response);
					if(response.equals(Messages.SERVER_UDP_SOCKET_CONNECTED)) {
						cu.uploadUDP(tokens[1]);
						response = cu.getResponseFromServer();
						System.out.println(response);
					}
					
				} else if (command.equalsIgnoreCase(Constants.CREATE_FOLDER) && tokens.length == 2) {
					response = cu.getResponseFromServer();
					System.out.println("From Server: "+response);
					
				} else if (command.equalsIgnoreCase(Constants.MOVE_FILE) && tokens.length == 3) {
					response = cu.getResponseFromServer();
					System.out.println("From Server: "+response);
					
				} else if (command.equalsIgnoreCase(Constants.CREATE_GROUP) && tokens.length == 2) {
					response = cu.getResponseFromServer();
					System.out.println("From Server: "+response);
					
				} else if (command.equalsIgnoreCase(Constants.LIST_GROUPS) && tokens.length == 1) {
					cu.receiveGroupListFromServer();
					
				} else if (command.equalsIgnoreCase(Constants.LIST_GROUP_DETAILS) && tokens.length == 2) {
					cu.receiveGroupDetailsFromServer(tokens[1]);
					
				} else if (command.equalsIgnoreCase(Constants.JOIN_GROUP) && tokens.length == 2) {
					response = cu.getResponseFromServer();
					System.out.println("From Server: "+response);
					
				} else if (command.equalsIgnoreCase(Constants.LEAVE_GROUP) && tokens.length == 2) {
					response = cu.getResponseFromServer();
					System.out.println("From Server: "+response);
					
				} else if (command.equalsIgnoreCase(Constants.SHARE_MSG) && tokens.length == 2) {

				} else if (command.equalsIgnoreCase(Constants.GET_FILE) && tokens.length == 2) {
					response = cu.getResponseFromServer();
					if(response.equals(Messages.COMMAND_VALIDATION_SUCCESS)) {
						System.out.println("From Server: Starting file downloading..");
						response = cu.downloadFile();
						System.out.println("From Server: "+response);
					}
					else {
						System.out.println("From Server: "+response);
					}
				} else if (command.equalsIgnoreCase(Constants.EXIT) && tokens.length == 1) {
					response = cu.getResponseFromServer();
					System.out.println("From Server: "+response);
					System.out.println("Closing this connection..");
					socket.close();
					System.out.println("Connection closed successfully..");
					break;
				} else {
					oos.writeObject(command);
					response = (String) ois.readObject();
					System.out.println(response);
				}
			}
			scanner.close();
			ois.close();
			oos.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			remoteIP = InetAddress.getByName(args[0]);
			remotePort = Integer.parseInt(args[1]);
			localIP = InetAddress.getByName(args[2]);
			localPort = Integer.parseInt(args[3]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		new Client().executeTCPClient();
	}
}
