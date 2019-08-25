package server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerStructures implements Serializable {

	private static final long serialVersionUID = 2L;

	static Set<String> activeUserSet = new HashSet<String>();
	static Map<String, Set<String>> groupMap = new HashMap<String, Set<String>>();
	static Map<String, Set<String>> userMap = new HashMap<String, Set<String>>();
	private static final String currPath = Paths.get("").toAbsolutePath().toString() + "/server/";

	// Let's serialize an Object
	static void serialize() {
		try {
			FileOutputStream userMapFile = new FileOutputStream(currPath + "userMapFile.dump");
			ObjectOutputStream out = new ObjectOutputStream(userMapFile);
			out.writeObject(userMap);
			out.close();
			userMapFile.close();

			FileOutputStream groupMapFile = new FileOutputStream(currPath + "groupMapFile.dump");
			out = new ObjectOutputStream(groupMapFile);
			out.writeObject(groupMap);
			out.close();
			groupMapFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Let's deserialize an Object
	@SuppressWarnings("unchecked")
	static void deserialize() {
		try {

			FileInputStream userMapFile = new FileInputStream(currPath + "userMapFile.dump");
			ObjectInputStream in = new ObjectInputStream(userMapFile);
			userMap = (Map<String, Set<String>>) in.readObject();
			in.close();
			userMapFile.close();

			FileInputStream groupMapFile = new FileInputStream(currPath + "groupMapFile.dump");
			in = new ObjectInputStream(groupMapFile);
			groupMap = (Map<String, Set<String>>) in.readObject();
			in.close();
			groupMapFile.close();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
