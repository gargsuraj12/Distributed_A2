package server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerStructures {

	static Set<String> activeUserSet = new HashSet<String>();
	static Map<String, Set<String>> groupMap = new HashMap<String, Set<String>>();
	static Map<String, Set<String>> usersMap = new HashMap<String, Set<String>>();
}
