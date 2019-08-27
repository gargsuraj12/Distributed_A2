package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerStructures implements Serializable {

	private static final long serialVersionUID = 2L;

	static Set<String> activeUserSet = new HashSet<String>();
	static Map<String, Set<String>> groupMap = new HashMap<String, Set<String>>();
	static Map<String, Set<String>> userMap = new HashMap<String, Set<String>>();
	static Map<String, List<String>> msgBuffer = new HashMap<String, List<String>>();

	private static final String currPath = Paths.get("").toAbsolutePath().toString() + "/server/";

	// Let's serialize an Object
	static void serialize() {
		File mbFile = new File(currPath + "msgBuffer.dump");
		File umFile = new File(currPath + "userMap.dump");
		File gmFile = new File(currPath + "groupMap.dump");
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;

		try {
			fos = new FileOutputStream(mbFile);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(msgBuffer);
			oos.close();
			fos.close();

			// fos = new FileOutputStream(currPath + "userMapFile.dump");
			fos = new FileOutputStream(umFile);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(userMap);
			oos.close();
			fos.close();

			// fos = new FileOutputStream(currPath + "groupMapFile.dump");
			fos = new FileOutputStream(gmFile);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(groupMap);
			oos.close();
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Let's deserialize an Object
	@SuppressWarnings("unchecked")
	static void deserialize() {
		File mbFile = new File(currPath + "msgBuffer.dump");
		File umFile = new File(currPath + "userMap.dump");
		File gmFile = new File(currPath + "groupMap.dump");
		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try {
			if (mbFile.createNewFile() == false) {
				fis = new FileInputStream(mbFile);
				ois = new ObjectInputStream(fis);
				msgBuffer = (Map<String, List<String>>) ois.readObject();
				ois.close();
				fis.close();
			}
			if (umFile.createNewFile() == false) {
				// fis = new FileInputStream(currPath + "userMapFile.dump");
				fis = new FileInputStream(umFile);
				ois = new ObjectInputStream(fis);
				userMap = (Map<String, Set<String>>) ois.readObject();
				ois.close();
				fis.close();
			}
			if (gmFile.createNewFile() == false) {
				// fis = new FileInputStream(currPath + "groupMapFile.dump");
				fis = new FileInputStream(gmFile);
				ois = new ObjectInputStream(fis);
				groupMap = (Map<String, Set<String>>) ois.readObject();
				ois.close();
				fis.close();
			}
//			umFile.createNewFile();
//			gmFile.createNewFile();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
