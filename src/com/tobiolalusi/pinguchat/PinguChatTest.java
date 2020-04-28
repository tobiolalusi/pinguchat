package com.tobiolalusi.pinguchat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

// TODO: Test semaphore

public class PinguChatTest {
	private final String SERVER = "127.0.0.1";
	private final int PORT = 3000;
	private final int SEMAPHORE_LIMIT = 49;
	private ChatServer chatServer;
	private Thread chatServerThread;
	private List<Thread> chatClientThreads = new ArrayList<>();
	private List<ChatClient> chatClients = new ArrayList<>();
	private Thread testThread;
	private Semaphore semaphore;

	private Socket client;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean connectionIsEstablished;

//	@BeforeEach
	public void establishConnection() throws IOException {
		client = new Socket(SERVER, PORT);
		connectionIsEstablished = true;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
	}

//	@AfterEach
	public void closeConnection() throws IOException {
		connectionIsEstablished = false;
		in.close();
		out.close();
		client.close();
	}

	@Test
	@Tag("testSomophore")
	@DisplayName("Test how many clients can be connected to server")
	public synchronized void testSomophore() throws InterruptedException {
		List<Thread> messenger = new ArrayList<>();
		Thread server = new Thread(() -> {
			ChatServer chatServer = new ChatServer(PORT);
			try {
				chatServer.startServer();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		});
		server.start();
//		for (int j = 0; j < 100; j++)
		for (int i = 0; i < 3; i++) {
			int id = i;
			chatClientThreads.add(new Thread(() -> {
				try {
//					Socket client = new Socket(SERVER, PORT);
//					ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
//					System.out.println("out is ready......");
//					out.writeObject("user" + (id + 1));
//					out.writeObject("Hello PinguServer!");
//					out.writeObject("Hello PinguServer!");
//					out.writeObject("Hello PinguServer!");
//					ObjectInputStream in = new ObjectInputStream(client.getInputStream());
//					System.out.println("in is ready....");
//					String response = (String) in.readObject();
//					System.out.println(response);
//					assertEquals("userx" + (id + 1) + ": Hello PinguServer!", response.split(": ", 2)[1]);
					ChatClient chatClient = new ChatClient(SERVER, PORT, "user" + (id + 1));
					chatClients.add(chatClient);
					chatClient.establishConnection();
//					closeConnection();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}));
		}
		for (Thread chatClientThread : chatClientThreads) chatClientThread.start();
		server.join();
		for (Thread chatClientThread : chatClientThreads) chatClientThread.join();
		for (ChatClient chatClient : chatClients) {
			messenger.add(new Thread(() -> {
				System.out.println("----------------");
				try {
					chatClient.getOutputStream().writeObject("Hello");
				} catch (IOException e) {
					e.printStackTrace();
				}
				String response = null;
				try {
					response = (String) chatClient.getInputStream().readObject();
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
				System.out.println(response);
			}));
		}
		for (Thread mess : messenger) mess.start();
		for (Thread mess : messenger) mess.join();
	}


//	@Test
//	@DisplayName("Can connect to server")
//	public void canConnectToServer() throws IOException, ClassNotFoundException {
//		ChatServer chatServer = new ChatServer(3000);
//		chatServer.startServer();
//		chatServer.closeServer();
//		System.out.println("-----------");
//		System.out.println(System.console().readLine());
//	}

}
