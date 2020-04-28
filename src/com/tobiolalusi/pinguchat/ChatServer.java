package com.tobiolalusi.pinguchat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ChatServer {
	private List<ClientThread> clientThreads;
	private int port;
	private ServerSocket serverSocket;
	private boolean serverIsOn;
	private Semaphore semaphore;

	public ChatServer(int port) {
		// number of clients should be less than 50
		this.semaphore = new Semaphore(49);
		this.port = port;
		this.clientThreads = new ArrayList<>();
	}

	public static void main(String[] args) {
		int port = 3000;
		if (args.length != 0 && args.length != 1) {
			String exception = "Invalid ChatServer arguments.\n";
			exception += "Valid usages are: > java ChatServer\n";
			exception += "                  > java ChatServer <port>\n";
			System.out.println(exception);
			System.exit(-1);
		}

		if (args.length == 1) port = Integer.parseInt(args[0]);

		ChatServer chatServer = new ChatServer(port);
		try {
			chatServer.startServer();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void logOnServerConsole(String message, LocalTime time) {
		System.out.println(time + ": " + message);
	}

	private static void logOnServerConsole(String message) {
		logOnServerConsole(message, LocalTime.now());
	}

	private static void logAlertOnServerConsole(String message) {
		logOnServerConsole("!!! " + message + " !!!");
	}

	public void startServer() throws IOException, ClassNotFoundException {
		serverSocket = new ServerSocket(port);
		serverIsOn = true; // turn on server
		while (serverIsOn) {
			logOnServerConsole("Server is waiting on port " + port + ".");
			Socket socket = serverSocket.accept();
			ClientThread clientThread = new ClientThread(socket, semaphore);
			clientThreads.add(clientThread);
			clientThread.start();
		}
		// server turns off
		closeServer();
	}

	// sends message to all clients
	private synchronized void broadcastMessage(String message, ClientThread sender) {
		LocalTime sentTime = LocalTime.now();
		// message is notification when sender is null
		// case message is private
		String[] messageSplit = message.trim().split(" ", 3);
		if (messageSplit.length > 1) // send message only with valid input
			if (!message.startsWith("*** ") && !message.startsWith("!!! ") && messageSplit[1].charAt(0) == '@') {
				String userToDM = messageSplit[1].substring(1);
				boolean foundUserToDM = false;
				for (Iterator<ClientThread> iterator = clientThreads.iterator(); iterator.hasNext(); ) {
					ClientThread client = iterator.next();
					if (client.username.equals(userToDM)) {
						try {
							client.sendClientMessage(sentTime + ": " + message);
							foundUserToDM = true;
						} catch (IOException e) {
							logAlertOnServerConsole("Error sending message to @" + client.username);
							logOnServerConsole(e.toString());
							iterator.remove();
							clientLeftChatNotifier(client.username);
						}
					}
				}
				if (!foundUserToDM) {
					try {
						sender.sendClientMessage(sentTime + ": !!! @" + userToDM + " is currently not on PinguChat. !!!");
					} catch (IOException e) {
						logOnServerConsole("Error sending message to @" + sender.username + ".");
						sender.connectionIsOn = false;
					}
				}
			} else {
				logOnServerConsole(message, sentTime);
				for (Iterator<ClientThread> iterator = clientThreads.iterator(); iterator.hasNext(); ) {
					ClientThread client = iterator.next();
					try {
						client.sendClientMessage(sentTime + ": " + message);
					} catch (IOException e) {
						logAlertOnServerConsole("Error sending message to @" + client.username);
						logOnServerConsole(e.toString());
						iterator.remove();
						clientLeftChatNotifier(client.username);
					}
				}
			}
	}

	// Notification broadcast message
	private void broadcastNotifiable(String message) {
		broadcastMessage("*** " + message + " ***", null);
	}

	private void clientLeftChatNotifier(String username) {
		logOnServerConsole("@" + username + " has been disconnected from PinguChat.");
		broadcastNotifiable("@" + username + " has left PinguChat.");
	}

	private synchronized void removeClient(ClientThread client, boolean withNotification) {
		clientThreads.remove(client);
		if (withNotification) clientLeftChatNotifier(client.username);
	}

	public void closeServer() throws IOException {
		serverIsOn = false;
		serverSocket.close();
		// close client connections
		for (ClientThread client : clientThreads) client.connectionIsOn = false;
	}

	private class ClientThread extends Thread {
		private String username;
		private LocalTime joinTime;
		private Socket socket;
		private ObjectInputStream inputStream;
		private ObjectOutputStream outputStream;
		private boolean connectionIsOn;
		private Semaphore semaphore;
		private String closeConnectionMessage;
		private String cannotSendClientMessage;
		private boolean broadcastConnectionOnClose = true;

		ClientThread(Socket socket, Semaphore semaphore) throws IOException, ClassNotFoundException {
			this.socket = socket;
			this.semaphore = semaphore;
			inputStream = new ObjectInputStream(socket.getInputStream());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			// username is the first alert server receives
			username = (String) inputStream.readObject();
			joinTime = LocalTime.now();
			closeConnectionMessage = "Closed connection with @" + username;
			cannotSendClientMessage = "Error sending message to @" + username;
			connectionIsOn = true;
			if (!semaphore.tryAcquire()) {
				connectionIsOn = false;
				closeConnectionMessage = "A new user attempted to log into PinguChat. Server is currently full.";
				broadcastConnectionOnClose = false;
				sendClientMessage("Sorry @" + username + ", PinguChat is currently full :(");
				sendClientMessage("Try again later in case someone logs out.");
			} else broadcastNotifiable("@" + username + " has joined PinguChat.");
		}

		@Override
		public void run() {
			while (connectionIsOn) {
				String message;
				try {
					message = (String) inputStream.readObject();
				} catch (IOException | ClassNotFoundException e) {
					closeConnectionMessage = "@" + username + " couldn't read message.";
					semaphore.release();
					connectionIsOn = false;
					break;
				}
				switch (message.toUpperCase()) {
					case "LOGOUT":
						closeConnectionMessage = "@" + username + " disconnected with a LOGOUT message.";
						connectionIsOn = false;
						semaphore.release();
						break;
					case "WHOIS":
						try {
							sendClientMessage("List of the users connected at " + LocalTime.now());
						} catch (IOException e) {
							closeConnectionMessage = cannotSendClientMessage;
							connectionIsOn = false;
							break;
						}
						for (int i = 0; i < clientThreads.size(); i++) {
							ClientThread client = clientThreads.get(i);
							try {
								sendClientMessage(i + 1 + ") @" + client.username + " since " + client.joinTime);
							} catch (IOException e) {
								closeConnectionMessage = cannotSendClientMessage;
							}
						}
						break;
					case "PENGU":
						broadcastMessage(PenguinFacts.getRandomPenguinFact(), null);
						break;
					default:
						broadcastMessage(username + ": " + message, this);
						break;
				}
			}
			try {
				closeConnection(closeConnectionMessage);
			} catch (IOException e) {
				try {
					sendClientMessage("@" + username + " has been disconnected from PinguChat.");
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}

		void closeConnection(String message) throws IOException {
			inputStream.close();
			outputStream.close();
			socket.close();
			logAlertOnServerConsole(message);
			removeClient(this, broadcastConnectionOnClose);
		}

		private void sendClientMessage(String message) throws IOException {
			outputStream.writeObject(message);
		}
	}
}
    
