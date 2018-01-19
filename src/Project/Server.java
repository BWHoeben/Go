package Project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
	private Set<Game> games;

	public Server() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please provide a port: ");
		try {
			this.port = scanner.nextInt();
			System.out.println("Using port " + this.port);
		} catch (NumberFormatException e) {
			try {
				scanner.close();
				throw new NoValidPortException("Not a valid port!");
			} catch (NoValidPortException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		boolean loop = true;
		while (loop) {
			try {

				this.ssock = new ServerSocket(port);
				loop = false;
			} catch (IOException e) {
				System.out.println("Port is already used!");
				System.out.println("Please provide a port: ");
				this.port = scanner.nextInt();
			}
		}
		this.availableClients = new HashSet<ClientHandler>();
		scanner.close();
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
				ClientHandler handler = new ClientHandler(this, sock, i);
				print("[client no . " + (i++) + " connected .]");
				handler.announce();
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
		clientsInGame = new HashSet<ClientHandler>();
		HashSet<ClientHandler> clientsInCurrentGame = new HashSet<ClientHandler>();
		Vector<String> clientNames = new Vector<String>();
		if (availableClients.size() == 2) {
			for (ClientHandler handler : availableClients) {
				clientsInCurrentGame.add(handler);
				clientsInGame.add(handler);
				clientNames.add(handler.getClientname());
			}
			availableClients.removeAll(clientsInGame);

		} else {
			try {
				throw new NotYetImplementedException("Not yet implemented!");
			} catch (NotYetImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.print("Clients matched! Starting game between ");
		int i = 0;
		for (String name : clientNames) {
			i++;
			if (i == clientNames.size()) {
				System.out.println(" and " + name + ".");
			} else if (i == clientNames.size() - 1) {
				System.out.print(name);
			} else {
				System.out.print(name + ", ");
			}
		}
		ClientHandler firstClient = getFirstClient(clientsInCurrentGame);
		Settings settings = null;
		try {
			settings = firstClient.getSettings(clientsInCurrentGame.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<Player> currentPlayers = new HashSet<Player>();
		currentPlayers.add(new OpponentPlayer(firstClient.getClientname(), settings.getColour()));
		Colour playerColour = settings.getColour();
		for (ClientHandler handler : clientsInCurrentGame) {
			if (!handler.equals(firstClient)) {
				playerColour = playerColour.next();
				currentPlayers.add(new OpponentPlayer(handler.getClientname(), playerColour));
			}
		}
		Game game = new Game(currentPlayers, settings.getBoardSize());
		System.out.println("Game created");
		games.add(game);
	}

	public ClientHandler getFirstClient(HashSet<ClientHandler> clients) {
		int lowestNumber = 9000;
		ClientHandler clientToReturn = null;
		for (ClientHandler handler : clients) {
			if (handler.getNumber() < lowestNumber) {
				lowestNumber = handler.getNumber();
				clientToReturn = handler;
			}
		}
		return clientToReturn;
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

	public static void main(String[] args) 
			throws InvalidNumberOfArgumentsException, NoValidPortException {
		System.out.println("Starting server...");
		Server server = new Server();
		server.start();
	}
}
