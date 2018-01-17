package Project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Vector;

public class Server extends Thread {
	private int port;
	private Collection<ClientHandler> threads;
	
	public Server(int port) {
		this.port = port;
		this.threads = new Vector<ClientHandler>();
	}

	public void run() {
		try (ServerSocket ssock = new ServerSocket(port);) {
			int i = 0;
			while (true) {
				Socket sock = ssock.accept();
				ClientHandler handler = new ClientHandler(this, sock);
				print("[client no . " + (i++) + " connected .]");
				handler.announce();
				handler.start();
				addHandler(handler);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void broadcast(String msg) {
		print(msg);
		(new Vector<>(threads)).forEach(handler -> handler.sendMessage(msg));
	}
	
	public void print(String msg) {
		System.out.println(msg);
	}
	
    public void addHandler(ClientHandler handler) {
        threads.add(handler);
    }
    
    public void removeHandler(ClientHandler handler) {
        threads.remove(handler);
    }
}
