package Project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;

import GUI.GoGUIIntegrator;
import Project.Errors.InvalidNumberOfArgumentsException;
import Project.Errors.NoValidPortException;

public class Server extends Thread {
	private int port;
	private Collection<ClientHandler> clients;
	private Set<Player> playersSet;
	
	public Server(int port) {
		this.port = port;
		this.clients = new Vector<ClientHandler>();
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
    	
    	if (args.length != 1) {
    		throw new InvalidNumberOfArgumentsException("Incorrect amount of arguments provided!");
    	}
    	try {
    	Integer.parseInt(args[0]);
    	} catch (NumberFormatException e) {
    		throw new NoValidPortException("Not a valid port!");
    	}
    	new Server(Integer.parseInt(args[0]));
    }
}
