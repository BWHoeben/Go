package Project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import Project.Errors.CouldNotConnectException;
import Project.Errors.InvalidCommandException;
import Project.Errors.InvalidHostException;
import Project.Errors.NoValidPortException;

public class Client extends Thread {

	private Player player;
	private String name;
	private Socket sock;
	private BufferedReader in;
	private BufferedWriter out;
	private int numberOfPlayers;
		
	public Client(String name, InetAddress host, int port) throws CouldNotConnectException {
		try {
			this.sock = new Socket(host, port);
		} catch (IOException e) {
			throw new CouldNotConnectException("Could not connect to server!");
		}
		this.name = name;
		try {
			this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} catch (IOException e) {
			throw new CouldNotConnectException("Could not initialize buffered reader!");
		}
		try {
			this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		} catch (IOException e) {
			throw new CouldNotConnectException("Could not initialize buffered writer!");
		}
		
		try {
			out.write(Protocol.NAME + Protocol.DELIMITER1 + name + Protocol.DELIMITER1 + Protocol.VERSION + Protocol.DELIMITER1 + 
					Protocol.VERSIONNUMBER + Protocol.DELIMITER1 + Protocol.EXTENSIONS + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1
					 + 0 + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 + Protocol.COMMAND_END);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			String msg = in.readLine();
			while (msg != null) {
				try {
					handleMessage(msg);
				} catch (InvalidCommandException e) {
					e.printStackTrace();
				}
				msg = in.readLine();
			}
			shutdown();
		} catch (IOException e) {
			shutdown();
		}
	}
	
	public void handleMessage(String msg) throws InvalidCommandException {
		String[] splitString = msg.split(Protocol.DELIMITER1);
		if (splitString[0].equals(Protocol.START) && splitString.length == 2) {
			try {
			this.numberOfPlayers = Integer.parseInt(splitString[1]);
			} catch (NumberFormatException e) {
				throw new InvalidCommandException("Invalid command! Second argument must be a number.");
			}
			provideGameSettings(numberOfPlayers);
		}
		
	}
	
	public void provideGameSettings(int numberOfPlayers) {
		if (numberOfPlayers == 2) {
		print("You are going to play Go with one other player!");
		} else {
		print("You are going to play Go with " + (numberOfPlayers - 1) + " other players!"); 
		}
		
		
	}

	public void sendMessage(String msg) {
		try {
			out.write(msg);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			shutdown();
		}
	}

	public void shutdown() {
		print("Closing socket connection...");
		try {
			sock.close();
		} catch (IOException e) {
			print("Error: closing the socket connection");
		}
	}

	public String getClientName() {
		return name;
	}

	public void print(String msg) {
		System.out.println(msg);
	}

	public Player getPlayer() {
		return player;
	}

	public void addPlayer(Player player) {
		this.player = player;
	}

	public static void main(String[] args) throws InvalidHostException, NoValidPortException {
		System.out.println("Please provide name: ");
		Scanner scanner = new Scanner(System.in);
		String name = scanner.nextLine();

		int port = 0;
		InetAddress host = null;
		
		System.out.println("Please provide host-ip: ");
		String hostString = scanner.nextLine();
		try {
			host = InetAddress.getByName(hostString);
		} catch (UnknownHostException e) {
			scanner.close();
			throw new InvalidHostException("Invalid host");
		}

		System.out.println("Please provide port: ");
		String portString = scanner.nextLine();
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			scanner.close();
			throw new NoValidPortException("Not a valid port!");
		}

		Client client;
		try {
			client = new Client(name, host, port);
			client.start();
		} catch (CouldNotConnectException e) {
			e.printStackTrace();
		}
		scanner.close();
	}
}
