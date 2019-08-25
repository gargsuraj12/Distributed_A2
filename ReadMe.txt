Compile with the following command from 'src' folder:
javac $(find . -name '*.java')

Run Server from 'src' folder as:
java server.Server <localPort>

Run Client from 'src' folder as:
java client.Client <remoteIP> <remotePort> <localIP> <localPort>