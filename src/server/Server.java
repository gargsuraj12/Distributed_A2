package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Constants;
import common.FileDetails;
import common.Messages;

class ClientHandler extends Thread {
	final ObjectInputStream ois;
	final ObjectOutputStream oos;
	final Socket socket;
	// private volatile boolean flag = true;

	public ClientHandler(Socket s, ObjectInputStream ois, ObjectOutputStream oos) {
		this.socket = s;
		this.ois = ois;
		this.oos = oos;
	}

	// This method will set flag as false
//	public void stopRunning() {
//		flag = false;
//	}

	@Override
	public void run() {

		String requestMsg = null;
		String replyMsg = null;
		String username = null;
		ServerUtilities su = new ServerUtilities();
		Object ob = null;

		while (true) {

			try {
				oos.writeObject("Enter the command: ");
				ob = ois.readObject();
				if (!(ob instanceof String)) {
					oos.writeObject(Messages.INVALID_FORMAT);
					return;
				}
				requestMsg = (String) ob;
				System.out.println("Request msg is: " + requestMsg);
				String[] tokens = requestMsg.split(" ");
				String command = tokens[0];

				if (command.equalsIgnoreCase(Constants.CREATE_USER) && tokens.length == 2) {
					username = tokens[1];
					if (ServerStructures.userMap.containsKey(username)
							&& ServerStructures.activeUserSet.contains(username)) {
						replyMsg = Messages.USER_ALREADY_CONNECTED;

					} else if (ServerStructures.userMap.containsKey(username)
							&& ServerStructures.activeUserSet.contains(username) == false) {
						ServerStructures.activeUserSet.add(username);
						replyMsg = Messages.USER_LOGIN_SUCCESS;

					} else {
						replyMsg = su.createUser(tokens[1]);
						ServerStructures.activeUserSet.add(username);
					}
					System.out.println(replyMsg);
					oos.writeObject(replyMsg);

				} else if (command.equalsIgnoreCase(Constants.UPLOAD_FILE) && tokens.length == 2) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
					} else {
						replyMsg = su.receiveFile(username, ois);
					}
					System.out.println(replyMsg);
					oos.writeObject(replyMsg);

				} else if (command.equalsIgnoreCase(Constants.UPLOAD_FILE_UDP) && tokens.length == 2) {

				} else if (command.equalsIgnoreCase(Constants.CREATE_FOLDER) && tokens.length == 2) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
					} else {
						replyMsg = su.createFolderByUser(username, tokens[1]);
					}
					oos.writeObject(replyMsg);

				} else if (command.equalsIgnoreCase(Constants.MOVE_FILE) && tokens.length == 3) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
					} else {
						replyMsg = su.moveFileByUser(username, tokens[1], tokens[2]);
					}
					oos.writeObject(replyMsg);

				} else if (command.equalsIgnoreCase(Constants.CREATE_GROUP) && tokens.length == 2) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
					} else {
						replyMsg = su.createGroup(username, tokens[1]);
					}
					oos.writeObject(replyMsg);

				} else if (command.equalsIgnoreCase(Constants.LIST_GROUPS) && tokens.length == 1) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
						oos.writeObject(replyMsg);
					} else {
						List<String> groupList = su.listGroups();
						oos.writeObject(groupList);
						// Serialize and send list object
					}

				} else if (command.equalsIgnoreCase(Constants.LIST_GROUP_DETAILS) && tokens.length == 2) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
						oos.writeObject(replyMsg);
					} else {
						Map<String, List<FileDetails>> groupDetails = su.listGroupDetails(tokens[1]);
						oos.writeObject(groupDetails);
						// Serialize and send map object
					}

				} else if (command.equalsIgnoreCase(Constants.JOIN_GROUP) && tokens.length == 2) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
					} else {
						replyMsg = su.joinGroup(username, tokens[1]);
					}
					oos.writeObject(replyMsg);

				} else if (command.equalsIgnoreCase(Constants.LEAVE_GROUP) && tokens.length == 2) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
					} else {
						replyMsg = su.leaveGroup(username, tokens[1]);
					}
					oos.writeObject(replyMsg);

				} else if (command.equalsIgnoreCase(Constants.SHARE_MSG) && tokens.length == 2) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
					} else {

					}
					oos.writeObject(replyMsg);

				} else if (command.equalsIgnoreCase(Constants.GET_FILE) && tokens.length == 2) {
					if (username == null) {
						replyMsg = Messages.USER_NOT_CONNECTED;
					} else {
						replyMsg = su.validateGetFileCommand(tokens[1]);
						boolean flag = true;
						if (replyMsg.equals(Messages.GROUP_NOT_EXIST) || replyMsg.equals(Messages.USER_NOT_IN_GROUP)
								|| replyMsg.equals(Messages.FILEPATH_NOT_EXIST)) {
							flag = false;
						}
						if (flag) {
							oos.writeObject(Messages.COMMAND_VALIDATION_SUCCESS);
							replyMsg = su.sendFile(replyMsg, oos);
						}
					}
					oos.writeObject(replyMsg);

				} else if (command.equalsIgnoreCase(Constants.EXIT) && tokens.length == 1) {
					if (ServerStructures.activeUserSet.contains(username)) {
						ServerStructures.activeUserSet.remove(username);
					}
					replyMsg = Messages.CONNECTION_CLOSE_SUCCESS;
					oos.writeObject(replyMsg);
					break;
				} else {
					replyMsg = Messages.INVALID_COMMAND;
					oos.writeObject(replyMsg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ServerStructures.serialize();
		System.out.println("Exiting thread for connection: " + socket.getRemoteSocketAddress());
		try {
			oos.close();
			ois.close();
			socket.close();
			// Thread.sleep(100);
			// stopRunning();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// this.stopRunning();
	}
}

public class Server {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		int localPort = Integer.parseInt(args[0]);
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(localPort);
		System.out.println("Server socket created and server listening at: " + serverSocket.getLocalSocketAddress());

		ServerStructures.deserialize();

		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				System.out.println("Server accepted the client: " + socket.getRemoteSocketAddress());

				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				System.out.println("Assigning new thread for this client..");
				new ClientHandler(socket, ois, oos).start();
			} catch (Exception e) {
				socket.close();
				e.printStackTrace();
			}
		}
	}
}
