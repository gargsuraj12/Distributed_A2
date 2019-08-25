package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Constants;
import common.FileDetails;
import common.Messages;

public class ServerUtilities {

	String getUserHome(String username) {
		String path = Paths.get("").toAbsolutePath().toString();
		return path + "/server/users/" + username;
	}

	String getFilePathForUser(String username, String filePath) {
		String homeDir = getUserHome(username);
		filePath = homeDir + "/" + filePath;
		return filePath;
	}

	private boolean createOSDirectory(String dirPath) {
		File dir = new File(dirPath);
		if (dir.exists()) {
			return true;
		}
		return dir.mkdir();
	}

	private boolean moveOSFile(String srcPath, String destPath) throws IOException {

		// Path temp = Files.move(Paths.get(srcPath), Paths.get(destPath));
		Path temp = Files.move(new File(srcPath).toPath(), new File(destPath).toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		if (temp != null) {
			return true;
		}
		return false;
	}

	private void listFiles(String path, String userHomeDir, List<FileDetails> fileList) {
		File folder = new File(path);
		File[] files = folder.listFiles();

		for (File file : files) {
			if (file.isFile()) {
				String fileName = file.getName();
				String currPath = file.getAbsolutePath().replaceAll(userHomeDir, "").replaceAll(fileName, "");
				fileList.add(new FileDetails(fileName, currPath));
			} else if (file.isDirectory()) {
				listFiles(file.getAbsolutePath(), userHomeDir, fileList);
			}
		}
	}

	private List<FileDetails> listAllFilesByUser(String username) {
		String userHomeDir = getUserHome(username);
		System.out.println("User homeDir is: " + userHomeDir);
		List<FileDetails> fileList = new ArrayList<FileDetails>();
		listFiles(userHomeDir, userHomeDir, fileList);
		return fileList;
	}

	String createUser(String username) {
		if (ServerStructures.userMap.containsKey(username)) {
			return Messages.USERNAME_ALREADY_EXIST;
		}
		String homeDir = getUserHome(username);
		System.out.println("Folder to be created at: " + homeDir);
		boolean rv = createOSDirectory(homeDir);
		if (!rv) {
			System.out.println("Error while creating folder for new user..");
			return Messages.CREATE_USER_ERROR;
		}
		ServerStructures.userMap.put(username, new HashSet<String>());
		return Messages.CREATE_USER_SUCCESS;
	}

	String createFolderByUser(String username, String folderPath) {
		String homeDir = getUserHome(username);
		String dirPath = homeDir + "/" + folderPath;
		if (createOSDirectory(dirPath)) {
			return Messages.FOLDER_CREATE_SUCCESS;
		}
		return Messages.FOLDER_CREATE_ERROR;
	}

	String moveFileByUser(String username, String srcPath, String destPath) {
		System.out.println("Inside moveFileByUser()");

		String fileName = "";
		if (srcPath.indexOf('/') != -1) {
			fileName = srcPath.substring(srcPath.lastIndexOf('/'));
		}
		System.out.println("Filename is: " + fileName);

		String homeDir = getUserHome(username);
		srcPath = homeDir + "/" + srcPath;
		destPath = homeDir + "/" + destPath;
//		destPath = destPath.replaceAll(fileName, "");

		System.out.println("srcPath is: " + srcPath);
		System.out.println("Before destPath is: " + destPath);

		if (new File(srcPath).exists() == false) {
			return Messages.SRCPATH_NOT_EXIST;
		}
		if (new File(destPath).exists() == false) {
			return Messages.DESTPATH_NOT_EXIST;
		}

		destPath += fileName;
		System.out.println("After destPath is: " + destPath);
		try {
			if (moveOSFile(srcPath, destPath)) {
				return Messages.MOVE_FILE_SUCCESS;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Messages.MOVE_FILE_ERROR;
	}

	String createGroup(String username, String groupname) {
		if (ServerStructures.groupMap.containsKey(groupname)) {
			return Messages.GROUP_ALREADY_EXIST;
		}
		Set<String> set = new HashSet<String>();
		set.add(username);
		ServerStructures.groupMap.put(groupname, set);

		ServerStructures.userMap.get(username).add(groupname);
		return Messages.CREATE_GROUP_SUCCESS;
	}

	List<String> listGroups() {
		if (ServerStructures.groupMap.isEmpty()) {
			return null;
		}
		List<String> groupNames = new ArrayList<String>();
		for (String name : ServerStructures.groupMap.keySet()) {
			groupNames.add(name);
		}
		return groupNames;
	}

	String joinGroup(String username, String groupname) {
		if (ServerStructures.groupMap.containsKey(groupname) == false) {
			return Messages.GROUP_NOT_EXIST;
		}
		if (ServerStructures.groupMap.get(groupname).contains(username)) {
			return Messages.USER_EXIST_IN_GROUP;
		}
		ServerStructures.groupMap.get(groupname).add(username);
		ServerStructures.userMap.get(username).add(groupname);
		return Messages.JOIN_GROUP_SUCCESS;
	}

	String leaveGroup(String username, String groupname) {
		if (ServerStructures.groupMap.containsKey(groupname) == false) {
			return Messages.GROUP_NOT_EXIST;
		}
		if (ServerStructures.groupMap.get(groupname).contains(username) == false) {
			return Messages.USER_NOT_IN_GROUP;
		}
		ServerStructures.groupMap.get(groupname).remove(username);
		ServerStructures.userMap.get(username).remove(groupname);
		return Messages.LEAVE_GROUP_SUCCESS;
	}

	Map<String, List<FileDetails>> listGroupDetails(String groupname) {
		if (ServerStructures.groupMap.containsKey(groupname) == false) {
			return null;
		}
		Map<String, List<FileDetails>> groupDetails = new HashMap<String, List<FileDetails>>();
		Set<String> userSet = ServerStructures.groupMap.get(groupname);
		List<FileDetails> fileDetails = null;

		for (String username : userSet) {
			fileDetails = listAllFilesByUser(username);
			groupDetails.put(username, fileDetails);
		}

		return groupDetails;
	}

	String receiveFile(String username, ObjectInputStream ois) {

		FileOutputStream fos = null;
		byte[] buffer = new byte[Constants.BUFFER_SIZE];
		String retValue = null;
		String filePath = getUserHome(username);
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
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			// return e.getMessage();
			return Messages.FILE_UPLOAD_ERROR;
		}
		return Messages.FILE_UPLOAD_SUCCESS;
	}

	String sendFile(String filePath, ObjectOutputStream oos) {

		File file = new File(filePath);
		System.out.println("File path to send is: " + filePath);
		if (file.exists() == false) {
			return Messages.FILEPATH_NOT_EXIST;
		}
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
		} catch (IOException e) {
			e.printStackTrace();
			// return e.getMessage();
			return Messages.FILE_DOWNLOAD_ERROR;
		}
		return Messages.FILE_DOWNLOAD_SUCCESS;
	}

	String validateGetFileCommand(String grpUsrPath) {
		int index = grpUsrPath.indexOf("/");
		String grpName = grpUsrPath.substring(0, index);
		if (ServerStructures.groupMap.containsKey(grpName) == false) {
			return Messages.GROUP_NOT_EXIST;
		}
		grpUsrPath = grpUsrPath.substring(index + 1);
		index = grpUsrPath.indexOf("/");
		String username = grpUsrPath.substring(0, index);
		if (ServerStructures.groupMap.get(grpName).contains(username) == false) {
			return Messages.USER_NOT_IN_GROUP;
		}
//		grpUsrPath = grpUsrPath.substring(index+1);
		String absPath = grpUsrPath.substring(index + 1);
		absPath = getFilePathForUser(username, absPath);
		if (new File(absPath).exists() == false) {
			return Messages.FILEPATH_NOT_EXIST;
		}
		return absPath;
	}
}
