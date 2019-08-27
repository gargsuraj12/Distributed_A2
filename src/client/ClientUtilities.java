package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import common.Constants;
import common.FileDetails;
import common.Messages;

public class ClientUtilities {
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;

	public ClientUtilities(ObjectInputStream ois, ObjectOutputStream oos) {
		this.ois = ois;
		this.oos = oos;
	}

	String getUserHome() {
		return Paths.get("").toAbsolutePath().toString();
	}

	String getResponseFromServer() {
		String response = null;
		try {

			Object ob = ois.readObject();
			if (ob instanceof String) {
				response = (String) ob;
			} else {
				response = Messages.INVALID_FORMAT;
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return response;
	}

	String uploadFile(String filePath) {

		File file = new File(filePath);
		String response = null;
		FileInputStream fis = null;

		try {

			oos.writeObject(file.getName());

			fis = new FileInputStream(file);
			byte[] buffer = new byte[Constants.BUFFER_SIZE];
			Integer bytesRead = 0;
			int count = 1;
			while ((bytesRead = fis.read(buffer)) > 0) {
				oos.writeObject(bytesRead);
				oos.writeObject(Arrays.copyOf(buffer, buffer.length));
				System.out.println("Chunk: " + count + " sent.");
				count++;
			}
			fis.close();

			Object ob = ois.readObject();
			if (ob instanceof String) {
				response = (String) ob;
			} else {
				response = Messages.INVALID_FORMAT;
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	void receiveGroupListFromServer() {
		List<String> grpList = null;
		try {
			Object ob = ois.readObject();
			if (ob instanceof List<?>) {
				grpList = (List<String>) ob;

			} else if (ob == null) {
				System.out.println(Messages.NO_GROUP_EXIST);
				return;
			} else {
				System.out.println(Messages.INVALID_FORMAT);
				return;
			}
			System.out.println("Group at server are:");
			for (String grp : grpList) {
				System.out.println(grp);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	void receiveGroupDetailsFromServer(String grpName) {
		Map<String, List<FileDetails>> grpDetails = null;
		try {
			Object ob = ois.readObject();
			if (ob instanceof Map<?, ?>) {
				grpDetails = (Map<String, List<FileDetails>>) ob;

			} else if (ob instanceof String) {
				System.out.println(ob.toString());
				return;
			} else {
				System.out.println(Messages.INVALID_FORMAT);
				return;
			}
			System.out.println("Fro group: " + grpName + " details at server are:");
			for (String username : grpDetails.keySet()) {
				System.out.println("File details of user: " + username);
				for (FileDetails fd : grpDetails.get(username)) {
					System.out.println(fd.getFileName() + "\t\t\t" + fd.getFilePath());
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	String downloadFile() {
		FileOutputStream fos = null;
		byte[] buffer = new byte[Constants.BUFFER_SIZE];
		String retValue = null;
		String filePath = getUserHome();
//		System.out.println("User home path is: "+filePath);

		// 1. Read file name.
		Object o;
		try {
			o = ois.readObject();
			if (o instanceof String) {
				filePath += "/" + o.toString();
				fos = new FileOutputStream(filePath);
			} else {
				return Messages.FILENAME_READ_ERROR;
			}

			System.out.println("Location where the file to be written: " + filePath);

			// 2. Read file to the end.
			Integer bytesRead = 0;
			do {
				o = ois.readObject();
				if (!(o instanceof Integer)) {
					retValue = Messages.CURR_CHUNK_SIZE_READ_ERROR;
					break;
				}

				bytesRead = (Integer) o;
				o = ois.readObject();
				if (!(o instanceof byte[])) {
					retValue = Messages.CURR_CHUNK_DATA_READ_ERROR;
				}

				buffer = (byte[]) o;
				// 3. Write data to output file.
				fos.write(buffer, 0, bytesRead);

			} while (bytesRead == Constants.BUFFER_SIZE);

			fos.close();
			if (retValue != null) {
				return retValue;
			}

			Object ob = ois.readObject();
			if (ob instanceof String) {
				retValue = (String) ob;
			} else {
				retValue = Messages.INVALID_FORMAT;
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		return retValue;
	}

	boolean checkAndPrintMessages() {
		try {
			Object ob = ois.readObject();
			if (ob instanceof String && ob.toString().equals(Constants.ENTER_COMMAND)) {
				return false;
			}
			if (ob instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<String> messages = (List<String>) ob;
				for (String msg : messages) {
					System.out.println(msg);
				}
			}
			return true;
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	boolean uploadUDP(String filePath) {
		try {
			DatagramSocket dSoc = new DatagramSocket(Client.localPort, Client.localIP);
			
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			byte[] buf = new byte[63*1024];
			int len;
			
			DatagramPacket pkg = new DatagramPacket(buf, buf.length, Client.remoteIP, Client.remotePort);
			while ((len = fis.read(buf)) != -1) {
				dSoc.send(pkg);
			}
			buf = "end".getBytes();
			DatagramPacket endpkg = new DatagramPacket(buf, buf.length, Client.remoteIP, Client.remotePort);
			System.out.println("Send the file.");
			dSoc.send(endpkg);
			fis.close();
			dSoc.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
