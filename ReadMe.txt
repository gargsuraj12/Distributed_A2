Compile with the following command from 'src' folder:
javac $(find . -name '*.java')

Run Server from 'src' folder as:
java server.Server <localPort>
eg: java server.Server 4000

Run Client from 'src' folder as:
java client.Client <remoteIP> <remotePort> <localIP> <localPort>
eg: java client.Client 10.1.37.203 4000 10.1.37.204 4001
