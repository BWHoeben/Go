package classes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import GUI.GoGUIIntegrator;
import errors.InvalidColourException;
import errors.InvalidCoordinateException;
import errors.InvalidNumberOfArgumentsException;
import errors.NoValidPortException;
import errors.NotAnIntException;
import errors.NotYetImplementedException;

public class Server extends Thread {
	private Set<ClientHandler> availableClients;
	private Set<HashSet<ClientHandler>> clientsInGame;
	private Map<Integer, ClientHandler> allClients;
	private ServerSocket ssock;
	private Set<Game> games;
	private ClientHandler blackClient;
	//private Set<Board> boards;
	private Map<ClientHandler, Board> clientBoardCombinations;

	public static void main(String[] args) 
			throws InvalidNumberOfArgumentsException, NoValidPortException {
		print("Starting server...");
		Server server = new Server();
		server.start();
	}

	// Initialization of a new server
	public Server() {
		// Scanner to get input from console 
		Scanner scanner = new Scanner(System.in);

		// Ask for port
		int port = getPort(scanner);

		// Open socket
		openServerSocket(port, scanner);

		// Initializing variables
		// This hashSet holds all available clients,
		// thus clients who are ready to play a game 
		this.availableClients = new HashSet<ClientHandler>();

		// This hashSet hold all clients, thus all clients
		// that are currently in a game as well as those that are not
		this.allClients = new HashMap<Integer, ClientHandler>();

		this.clientBoardCombinations = new HashMap<ClientHandler, Board>();
		this.clientsInGame = new HashSet<HashSet<ClientHandler>>();
		// Done with reading input from console, so closing scanner
		scanner.close();
	}

	public int getPort(Scanner scanner) {
		print("Please provide a port: ");
		while (true) {
			String input = scanner.nextLine();
			try {
				return Integer.parseInt(input);
			} catch (NumberFormatException e) {
				print("Not a valid port, please try again.");
			}
		}
	}
	public void openServerSocket(int portArg, Scanner scanner) {
		int port = portArg;
		while (true) {
			try {
				this.ssock = new ServerSocket(port);
				print("Opened socket on port " + port);
				break;
			} catch (IOException e) {
				print("Port is already used!");
				port = getPort(scanner);
			}
		}
	}

	public void run() {
		int i = 1;
		while (true) {
			Socket sock;
			try {
				if (this.availableClients.size() == 0) {
					print("Waiting for client...");
				} else {
					print("Waiting for another client...");	
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
		HashSet<ClientHandler> clientsInCurrentGame = new HashSet<ClientHandler>();
		Vector<String> clientNames = new Vector<String>();
		if (availableClients.size() == 2) {
			for (ClientHandler handler : availableClients) {
				clientsInCurrentGame.add(handler);
				clientNames.add(handler.getClientName());
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
				print(" and " + name + ".");
			} else if (i == clientNames.size() - 1) {
				System.out.print(name);
			} else {
				System.out.print(name + ", ");
			}
		}
		ClientHandler firstClient = getFirstClient(clientsInCurrentGame);

		// request settings
		firstClient.sendMessageToClient(Protocol.START + Protocol.DELIMITER1 +
				clientsInCurrentGame.size() + Protocol.DELIMITER1 + Protocol.COMMAND_END);
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
		print(String.format("Message recieved :%s. From %s", msg, handler.getClientName()));
		String[] split = msg.split(Protocol.DELIMITER1);
		if (split[0].equals(Protocol.SETTINGS)) {
			try {
				int boardSize = Integer.parseInt(split[2]);
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
		} else if (split[0].equals(Protocol.MOVE)) {
			processMove(split, msg, handler);
		} else if (split[0].equals(Protocol.QUIT)) {
			handleQuit(handler);
		}
	}

	public void handleQuit(ClientHandler handler) {
		print(handler.getClientName() + " has quit");
		Set<ClientHandler> clientsInMyGame = getClientsInMyGame(handler);
		if (clientsInMyGame.size() == 2) {
			endGame(handler, Protocol.ABORTED);
		} else {
			try {
				throw new NotYetImplementedException("not yet implemented");
			} catch (NotYetImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void endGame(ClientHandler handler, String reason) {
		String stringToSend = Protocol.ENDGAME + Protocol.DELIMITER1 + reason + Protocol.DELIMITER1;
		Set<ClientHandler> clientsInMyGame = getClientsInMyGame(handler);
		Map<Colour, Integer> scores = getBoardOfClient(handler).getScore();
		Map<ClientHandler, Integer> clientScores = new HashMap<ClientHandler, Integer>();
		assert clientsInMyGame.size() == scores.size();
		int score = 0;
		for (ClientHandler clientInGame : clientsInMyGame) {
			score = scores.get(clientInGame.getColour());
			clientScores.put(clientInGame, score);
		}
				
		for (int i = 0; i < clientsInMyGame.size(); i++) {
			ClientHandler clientWithMaxScore = getClientWithHighestScore(clientScores);
			stringToSend = stringToSend + clientWithMaxScore.getClientName() 
				+ Protocol.DELIMITER1 + clientScores.get(clientWithMaxScore) + Protocol.DELIMITER1;
			clientScores.remove(clientWithMaxScore);
			
		}
		broadcastToSetOfClients(stringToSend + Protocol.COMMAND_END, clientsInMyGame);
	}
	
	public ClientHandler getClientWithHighestScore(Map<ClientHandler, Integer> clientScores) {
		int max = 0;
		ClientHandler maxClient = null;
		for (Map.Entry<ClientHandler, Integer> entry : clientScores.entrySet()) {
			if (entry.getValue() >= max) {
				max = entry.getValue();
				maxClient = entry.getKey();
			}
		}
		return maxClient;
	}

	// CASE MOVE row_column
	public void processMove(String[] split, String msg, ClientHandler handler) {
		//String playerWhoMadeMove = handler.getClientName();
		//Set<ClientHandler> clientsInThisGame = getClientsInMyGame(handler);
		Boolean gameOver = false;
		Board board = getBoardOfClient(handler);
		Move move = null;
		if (split[1].equals(Protocol.PASS)) {
			print(handler.getClientName() + " passed.");
			System.out.println("Todo: insert check for subsequent passes!");
		} else {
			try {
				move = new Move(split[1], board.getDimension(), handler.getColour());
			} catch (InvalidCoordinateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gameOver = processMoveLocally(move.getIndex(), handler.getColour(), board);
		}
		// Check for pass
		if (!gameOver) {
			communicateMove(move, handler);
		} else {
			print("Game has ended");
		}
	}

	public boolean processMoveLocally(int index, Colour colour, Board board) {
		board.setIntersection(index, colour);
		return board.gameOver();
	}

	public Board getBoardOfClient(ClientHandler handler) {
		return clientBoardCombinations.get(handler);
	}

	public void communicateMove(Move move, ClientHandler clientWhoMadeMove) {
		Set<ClientHandler> clientsInThisGame = getClientsInMyGame(clientWhoMadeMove);
		int numberOfClientWhoMadeMove = clientWhoMadeMove.getNumber();
		List<Integer> numbersOfClientsInThisGame = new ArrayList<Integer>();

		for (ClientHandler handler : clientsInThisGame) {
			numbersOfClientsInThisGame.add(handler.getNumber());
		}

		int numberOfNextClient = getNextNumberOfList(
				numbersOfClientsInThisGame, numberOfClientWhoMadeMove);

		ClientHandler nextClient = allClients.get(numberOfNextClient);
		String moveAsString  = null;
		if (move != null) {
			moveAsString = move.toString();
		} else {
			moveAsString = Protocol.PASS;
		}
		String stringToSend = Protocol.TURN + Protocol.DELIMITER1 + clientWhoMadeMove.getClientName() +
				Protocol.DELIMITER1 + moveAsString + Protocol.DELIMITER1 + nextClient.getClientName() + Protocol.DELIMITER1 + Protocol.COMMAND_END;

		broadcastToSetOfClients(stringToSend, clientsInThisGame);
	}

	public int getNextNumberOfList(List<Integer> list, int i) {
		List<Integer> numbersToRemove = new ArrayList<Integer>();
		for (int j : list) {
			if (j <= i) {
				numbersToRemove.add(j);
			}
		}
		if (list.size() != numbersToRemove.size()) {
			list.removeAll(numbersToRemove);
		}
		return getMinimumValueFromList(list);
	}

	public int getMinimumValueFromList(List<Integer> list) {
		return list.stream().reduce(Integer::min).get();
	}

	// handler = first player!
	public void handleSettings(int boardSize, ClientHandler handler, Colour firstColour) {
		handler.setColour(firstColour);
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
					Player player = new OpponentPlayer(clientInGame.getClientName(), firstColour);
					players.add(player);	
					if (firstColour.equals(Colour.BLACK)) {
						blackClient = clientInGame;
					}
				} else {
					clientInGame.setColour(secondColour);
					Player player = new OpponentPlayer(clientInGame.getClientName(), secondColour);
					players.add(player);
					if (secondColour.equals(Colour.BLACK)) {
						blackClient = clientInGame;
					}
				}
			}

			String stringToSend = Protocol.START + Protocol.DELIMITER1 + 
					clientsInMyGame.size() +
					Protocol.DELIMITER1 + secondColour.toString() + Protocol.DELIMITER1 + 
					boardSize + Protocol.DELIMITER1;

			Set<String> playerNames = new HashSet<String>();
			for (ClientHandler handlerToAdd : clientsInMyGame) {
				playerNames.add(handlerToAdd.getClientName());
				stringToSend = stringToSend + handlerToAdd.getClientName() + Protocol.DELIMITER1;
			}

			stringToSend = stringToSend + Protocol.COMMAND_END;
			broadcastToSetOfClients(stringToSend, clientsInMyGame);

			Board board = new Board(boardSize, clientsInMyGame.size());

			for (ClientHandler clientInGame : clientsInMyGame) {
				clientBoardCombinations.put(clientInGame, board);
			}

			broadcastToSetOfClients(Protocol.TURN + Protocol.DELIMITER1 + 
					blackClient.getClientName() 	+ Protocol.DELIMITER1 + 
					Protocol.FIRST + Protocol.DELIMITER1 + blackClient.getClientName()
					+ Protocol.DELIMITER1 + Protocol.COMMAND_END, clientsInMyGame);
		} else {
			try {
				throw new NotYetImplementedException("Not yet implemented");
			} catch (NotYetImplementedException e) {
				e.printStackTrace();
			}
		}


	}

	public static void print(String msg) {
		System.out.println(msg);
	}

	public void addHandler(ClientHandler handler) {
		availableClients.add(handler);
		allClients.put(handler.getNumber(), handler);
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
}
