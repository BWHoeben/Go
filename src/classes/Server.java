package classes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import GUI.GoGUIIntegrator;
import errors.InvalidColourException;
import errors.InvalidNumberOfArgumentsException;
import errors.NoValidPortException;
import errors.NotAnIntException;
import errors.NotYetImplementedException;

public class Server extends Thread {
	private int port;
	private Set<ClientHandler> availableClients;
	private Set<HashSet<ClientHandler>> clientsInGame;
	private Set<ClientHandler> allClients;
	//	private Set<Player> playersSet;
	private ServerSocket ssock;
	private Set<Game> games;
	private ClientHandler blackClient;

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
		this.allClients = new HashSet<ClientHandler>();
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
					matchClients();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void matchClients() {
		//clientsInGame = new HashSet<ClientHandler>();
		clientsInGame = new HashSet<HashSet<ClientHandler>>();
		HashSet<ClientHandler> clientsInCurrentGame = new HashSet<ClientHandler>();
		Vector<String> clientNames = new Vector<String>();
		if (availableClients.size() == 2) {
			for (ClientHandler handler : availableClients) {
				clientsInCurrentGame.add(handler);
				clientNames.add(handler.getClientname());
			}
			clientsInGame.add(clientsInCurrentGame);
			availableClients.removeAll(clientsInCurrentGame);

		} else {
			try {
				throw new NotYetImplementedException("Not yet implemented!");
			} catch (NotYetImplementedException e) {
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

	public void broadcastToSetOfClients(String msg, Set<ClientHandler> clients) {
		(new Vector<>(clients)).forEach(handler -> handler.sendMessageToClient(msg));	
	}

	public void broadcastToClientsInMyGame(String msg, ClientHandler handlerArg) {
		Set<ClientHandler> clientsInThisGame = getClientsInMyGame(handlerArg);
		broadcastToSetOfClients(msg, clientsInThisGame);
	}

	public Set<ClientHandler> getClientsInMyGame(ClientHandler handler) {
		// Cycle through set of handler
		for (Set<ClientHandler> handlerSet : clientsInGame) {
			// cycle through specific set
			for (ClientHandler handlerOfSet : handlerSet) {
				if (handler.equals(handlerOfSet)) {
					return handlerSet;
				}
			}
		}
		return null;
	}

	public void handleMessage(String msg, ClientHandler handler) {
		System.out.println(String.format("Message recieved :%s. From ", msg, handler.getName()));
		String[] split = msg.split(Protocol.DELIMITER1);
		if (split[0].equals(Protocol.SETTINGS)) {
			try {
				int boardSize = Integer.parseInt(split[2]);
				System.out.println(String.format("Board size is : %s", boardSize));
				Colour colour = Colour.getColour(split[1]);
				handleSettings(boardSize, handler, colour);
			} catch (NumberFormatException e) {
				try {
					throw new NotAnIntException("Not an int!");
				} catch (NotAnIntException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (InvalidColourException e) {
				try {
					throw new InvalidColourException("Invalid colour!");
				} catch (InvalidColourException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

	}

	// handler = first player!
	public void handleSettings(int boardSize, ClientHandler handler, Colour firstColour) {
		Colour secondColour = null;
		Set<ClientHandler> clientsInMyGame = getClientsInMyGame(handler);
		Set<Player> players = new HashSet<Player>();
		if (firstColour.equals(Colour.BLACK)) {
			secondColour = Colour.WHITE;

		} else {
			secondColour = Colour.BLACK;
		}
		if (clientsInMyGame.size() == 2) {
			for (ClientHandler clientInGame : clientsInMyGame) {



				if (clientInGame.equals(handler)) {
					Player player = new OpponentPlayer(clientInGame.getClientname(), firstColour);
					players.add(player);	
					if (firstColour.equals(Colour.BLACK)) {
						blackClient = clientInGame;
					}
				} else {
					clientInGame.sendMessageToClient(Protocol.START + Protocol.DELIMITER1 + 
							clientsInMyGame.size() +
							Protocol.DELIMITER1 + secondColour.toString() + Protocol.DELIMITER1 + 
							boardSize + Protocol.DELIMITER1 + handler.getClientname() + 
							Protocol.DELIMITER1 + clientInGame.getClientname() + 
							Protocol.DELIMITER1 + Protocol.COMMAND_END);
					Player player = new OpponentPlayer(clientInGame.getClientname(), secondColour);
					players.add(player);
					if (secondColour.equals(Colour.BLACK)) {
						blackClient = clientInGame;
					}
				}
			}

			broadcastToSetOfClients(Protocol.TURN + Protocol.DELIMITER1 + blackClient.getClientname()
			+ Protocol.DELIMITER1 + Protocol.FIRST + Protocol.DELIMITER1 + blackClient.getClientname() + Protocol.DELIMITER1 + Protocol.COMMAND_END, clientsInMyGame);

			startGame(players, boardSize);

		} else {
			try {
				throw new NotYetImplementedException("Not yet implemented");
			} catch (NotYetImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	public void print(String msg) {
		System.out.println(msg);
	}

	public void addHandler(ClientHandler handler) {
		availableClients.add(handler);
		allClients.add(handler);
	}

	public void removeHandler(ClientHandler handler) {
		availableClients.remove(handler);
	}

	public void startGame(Set<Player> playersSet, int boardSize) {
		GoGUIIntegrator gogui = new GoGUIIntegrator(true, true, boardSize);
		//gogui.startGUI();
		gogui.setBoardSize(boardSize);
		new Game(playersSet, boardSize, gogui);
	}

	public static void main(String[] args) 
			throws InvalidNumberOfArgumentsException, NoValidPortException {
		System.out.println("Starting server...");
		Server server = new Server();
		server.start();
	}
}
