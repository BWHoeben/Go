package Project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import GUI.GoGUIIntegrator;
import Project.Errors.InvalidNumberOfArgumentsException;
import Project.Errors.NoValidPortException;
import Project.Errors.NotYetImplementedException;

public class Server extends Thread {
	private int port;
	private Set<ClientHandler> availableClients;
	private Set<ClientHandler> clientsInGame;
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
    		this.availableClients = new HashSet<ClientHandler>();
		
	}

	public void run() {
		int i = 1;
			while (true) {
				Socket sock;
				try {
					if (this.availableClients.size() == 0) {
					System.out.println("Waiting for client...");
					} else {
					System.out.println("Waiting for another client...");	
					}
					sock = this.ssock.accept();
					ClientHandler handler = new ClientHandler(this, sock);
					print("[client no . " + (i++) + " connected .]");
					//handler.announce();
					handler.start();
					addHandler(handler);	
					if (this.availableClients.size() > 1) {
						matchClients(availableClients);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
	}
	
	public void matchClients(Set<ClientHandler> availableClients) {
		if (availableClients.size() == 2) {
			for (ClientHandler handler : availableClients) {
				availableClients.remove(handler);
				clientsInGame.add(handler);

			}
				
				
		} else {
			try {
				throw new NotYetImplementedException("Not yet implemented!");
			} catch (NotYetImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void broadcast(String msg) {
		print(msg);
		(new Vector<>(availableClients)).forEach(handler -> handler.sendMessage(msg));
	}
	
	public void print(String msg) {
		System.out.println(msg);
	}
	
    public void addHandler(ClientHandler handler) {
    	availableClients.add(handler);
    }
    
    public void removeHandler(ClientHandler handler) {
    	availableClients.remove(handler);
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
