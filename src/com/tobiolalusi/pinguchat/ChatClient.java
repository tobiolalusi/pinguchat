package com.tobiolalusi.pinguchat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Scanner;

public class ChatClient {
	private String server;
	private int port;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private String username;
	private boolean establishedConnection;

	public ChatClient(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}

	public static void main(String[] args) throws IOException {
		String server = "127.0.0.1";
		int port = 3000;

		if (args.length != 0 && args.length != 2) {
			String exception = "Invalid ChatClient arguments.\n";
			exception += "Valid usages are: > java ChatClient\n";
			exception += "                  > java ChatClient [portNumber] [serverAddress]\n";
			System.out.println(exception);
			System.exit(-1);
		}

		if (args.length == 2) {
			port = Integer.parseInt(args[0]);
			server = args[1];
		}

		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter a username: ");
		System.out.print("> ");
		String username = scanner.nextLine();
		while (!username.matches("^[a-z0-9_.]{3,15}$")) {
			System.out.println(
				"+-------------------------------------------------------------------------------------+\n" +
				"|                                                                                     |\n" +
				"|     Input a valid username.                                                         |\n" +
				"|     Username can only contain lowercase letters, number, period and underscore.     |");
			if (username.length() > 15 || username.length() < 3)
				System.out.println(
				"|     Username should contain between 3 and 15 characters.                            |");
			System.out.println(
				"|                                                                                     |\n" +
				"+-------------------------------------------------------------------------------------+");
			System.out.println("< Enter a username: ");
			System.out.print("> ");
			username = scanner.nextLine();
		}
		System.out.println();

		ChatClient chatClient = new ChatClient(server, port, username);
		chatClient.establishConnection();

		System.out.println(
			"+------------------------------------------------------------------------------------------------+\n" +
			"|                                                                                                |\n" +
			"|     Hello @" + chatClient.username + "! Welcome to PinguChat." +
			" ".repeat(61 - chatClient.username.length()) + "|\n" +
			"|                                                                                                |\n" +
			"+------------------------------------------------------------------------------------------------+\n" +
			"|                                                                                                |\n" +
			"|     Instructions:                                                                              |\n" +
			"|     1. Simply type the message to send broadcast to all active clients                         |\n" +
			"|     2. Type '@username<space>yourmessage' without quotes to send message to desired client     |\n" +
			"|     3. Type 'WHOIS' without quotes to see list of active clients                               |\n" +
			"|     4. Type 'LOGOUT' without quotes to logoff from server                                      |\n" +
			"|     5. Type 'PENGU' without quotes to request a random penguin fact                            |\n" +
			"|                                                                                                |\n" +
			"+------------------------------------------------------------------------------------------------+\n"
		);

		while (chatClient.establishedConnection) {
			System.out.print("> ");
			String message = scanner.nextLine();
			chatClient.sendMessageToServer(message);
		}

		scanner.close();
		chatClient.closeConnection();
	}

	public void establishConnection() throws IOException {
		try {
			socket = new Socket(server, port);
		} catch (IOException e) {
			logOnClientConsole("Cannot connect to server at the moment. Please try again later.");
			System.exit(-1);
		}
		establishedConnection = true;
		logOnClientConsole("< Connection to PinguChat established on " + server + ":" + socket.getPort() + "\n");
		outputStream = new ObjectOutputStream(socket.getOutputStream());
		inputStream = new ObjectInputStream(socket.getInputStream());

		// Thread to keep receiving messages
		new Thread(() -> {
			while (establishedConnection)
				try {
					System.out.println("\b\b< " + inputStream.readObject());
					System.out.print("> ");
				} catch (IOException | ClassNotFoundException e) {
					try {
						closeConnection();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
		}).start();
		// alert server first about current user
		outputStream.writeObject(username);
	}

	public void closeConnection() throws IOException {
		System.out.print("\b\b"); // Erase line content
		establishedConnection = false;
		inputStream.close();
		outputStream.close();
		socket.close();
		System.exit(0);
	}

	private void sendMessageToServer(String message) throws IOException {
		outputStream.writeObject(message);
	}

	private void logOnClientConsole(String message) {
		System.out.println(LocalTime.now() + ": " + message);
	}

	public ObjectOutputStream getOutputStream() {
		return outputStream;
	}

	public ObjectInputStream getInputStream() {
		return inputStream;
	}
}
