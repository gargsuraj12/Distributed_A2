Compile with the following command from 'src' folder:
	javac $(find . -name '*.java')

Run Server from 'src' folder as:
	java server.Server <localPort>
	eg: java server.Server 4000

Run Client from 'src' folder as:
	java client.Client <remoteIP> <remotePort> <localIP> <localPort>
	eg: java client.Client 10.1.37.203 4000 10.1.37.204 4001


Commands Formats:

Following are the below commands to execute the program : 

1. 	create_user <username>
		- If not registered at server then this command will register the user and create a default directory for the user at the server otherwise if user if 			already registered then will try to login for the user if another user with the same username has not already connected with the server.

2. 	upload <filepath> / upload_udp <filepath> 
		- Filepath can be relative or absolute but paths like '~/folder/file.txt' will not work.
		eg:	upload temp.txt
			upload_udp /home/suraj/Desktop/temp1.txt 	

3.	create_folder <folderpath>
		- If single foldername is given then folder will created at user's default directory at the server.  
		- If folder hierarchy is given then will be create if previous path is valid.
		eg: create_folder f1 : will create the folder at default directory if not already exist otherwise do nothing.
			create_folder f1/f2 : will create f2/ inside f1/ if f1/ already exist.

4. move_file <filename> <destnFolder> 
		- Do not append 'filename' after 'destnFolder'
		eg: move_file temp.txt f1
			move_file temp.txt f1/temp.txt will not work.

5. create_group <groupname>
		- If group already exist then gives error message otherwise creates the group.
		- The user who is creating the group will be automatically added in the group which is being created by the user.
		eg: create_group g1

6. list_groups
		- list all the groups present in the system.

7. list_detail <groupname>
		- list the users and their files(if exist) in the group.

8. share_msg <message>
		- Sends the message to all the users to all the groups which the current user is part of.
		- Users will recieves the message after they execute something.
		eg: share_msg Hi from the user.

9.	get_file <groupname/username/filepath>
		- File will be downloaded successfull via TCP connection if groupname, username and filepath all are valid otherwise appropriate error message will 		generated.
		- File will be downloaded at the current location from where the application is launched.



Note: Check getUserHome() in ServerUtilities.java if gives error while user creation. 

Extra Functionalities implemented:
- Error Handling
- Message Buffer for ofline users
