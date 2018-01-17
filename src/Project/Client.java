package Project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread {
	
	private Player player;
	private String name;
	private Socket sock;
	private BufferedReader in;
	private BufferedWriter out;
	
	public Client(String name, InetAddress host, int port) {
		try {
			this.sock = new Socket(host, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.name = name;
		try {
			this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			String msg = in.readLine();
			while (msg != null) {
				print(msg);
				msg = in.readLine();
			}
			shutdown();
		} catch (IOException e) {
			shutdown();
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
}
