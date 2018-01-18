package Project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import GUI.GoGUIIntegrator;
import Project.Errors.InvalidNumberOfArgumentsException;
import Project.Errors.NoValidPortException;

public class Server extends Thread {
	private int port;
	private Collection<ClientHandler> clients;
	private Set<Player> playersSet;
	private ServerSocket ssock;
	
	public Server() {
    		Scanner scanner = new Scanner(System.in);
    		System.out.println("Please provide a port: ");
    		try {
    		this.port = scanner.nextInt();
    		System.out.println("Using port " + this.port);
    		} catch (NumberFormatException e) {
    			try {
					throw new NoValidPortException("Not a valid port!");
				} catch (NoValidPortException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    		}
    		
    		boolean loop = true;
    		while (loop)
    		try {
    				this.ssock = new ServerSocket(port);
				loop = false;
    		} catch (IOException e) {
				System.out.println("Port is already used!");
			}
    		this.clients = new Vector<ClientHandler>();
		
	}

	public void run() {
		int i = 0;
			while (true) {
				Socket sock;
				try {
					System.out.println("Waiting for client...");
					sock = this.ssock.accept();
					ClientHandler handler = new ClientHandler(this, sock);
					print("[client no . " + (i++) + " connected .]");
					handler.announce();
					handler.start();
					addHandler(handler);	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
	}
	
	public void broadcast(String msg) {
		print(msg);
		(new Vector<>(clients)).forEach(handler -> handler.sendMessage(msg));
	}
	
	public void print(String msg) {
		System.out.println(msg);
	}
	
    public void addHandler(ClientHandler handler) {
        clients.add(handler);
    }
    
    public void removeHandler(ClientHandler handler) {
        clients.remove(handler);
    }
    
    public void startGame() {
    		int boardsize = 9;
        GoGUIIntegrator gogui = new GoGUIIntegrator(true, true, boardsize);
        gogui.startGUI();
        gogui.setBoardSize(boardsize);
    		new Game(playersSet, boardsize, gogui);
    }
    
    public static void main(String[] args) throws InvalidNumberOfArgumentsException, NoValidPortException {
    	System.out.println("Starting server...");
    	Server server = new Server();
    	server.start();
    	}
}
