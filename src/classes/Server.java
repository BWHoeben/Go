package classes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

//import GUI.GoGUIIntegrator;
import errors.InvalidColourException;
import errors.InvalidCoordinateException;
import errors.InvalidMoveException;
import errors.InvalidNumberOfArgumentsException;
import errors.NoValidPortException;
import errors.NotAnIntException;
import errors.NotYetImplementedException;

public class Server extends Thread {
	private Set<HashSet<ClientHandler>> clientsInGame;
	private Map<Integer, ClientHandler> allClients;
	private ServerSocket ssock;

	// key is number of desired opponents
	private Map<Integer, HashSet<ClientHandler>> clientsSorted; 
	private Map<ClientHandler, ActualBoard> clientBoardCombinations;

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
		//int port = getPort(scanner);
		int port = Protocol.DEFAULT_PORT;
		// Open socket
		openServerSocket(port, scanner);

		// Initializing variables
		// This hashSet holds all available clients,
		// thus clients who are ready to play a game 

		// This hashSet hold all clients, thus all clients
		// that are currently in a game as well as those that are not
		this.allClients = new HashMap<Integer, ClientHandler>();

		this.clientBoardCombinations = new HashMap<ClientHandler, ActualBoard>();
		this.clientsInGame = new HashSet<HashSet<ClientHandler>>();
		this.clientsSorted = new HashMap<Integer, HashSet<ClientHandler>>();
		// Done with reading input from console, so closing scanner
		scanner.close();
	}

	public int getPort(Scanner scanner) {
		while (true) {
			print("Please provide a port: ");
			String input = scanner.nextLine();
			try {
				int intToReturn = Integer.parseInt(input);
				if (intToReturn <= 0) {
					print("Port should be a postive number");
				} else {
					return Integer.parseInt(input);
				}
			} catch (NumberFormatException e) {
				print("Not a valid port."); 
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
				sock = this.ssock.accept();
				ClientHandler handler = new ClientHandler(this, sock, i);
				print("[client no . " + (i++) + " connected .]");
				handler.announce();
				handler.start();
				addHandler(handler);	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void matched(HashSet<ClientHandler> matchedClients) {
		Vector<String> clientNames = new Vector<String>();
		for (ClientHandler handler : matchedClients) {
			clientNames.add(handler.getClientName());
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
		ClientHandler firstClient = getFirstClient(matchedClients);
		this.clientsInGame.add(matchedClients);
		firstClient.sendMessageToClient(Protocol.START + Protocol.DELIMITER1 +
				matchedClients.size() + Protocol.DELIMITER1 + Protocol.COMMAND_END);
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
		if (allClients.containsKey(handler.getNumber())) {
			print(String.format("Message recieved :%s. From %s", msg, handler.getClientName()));
			String[] split = msg.split("\\" + Protocol.DELIMITER1);
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
			} else if (split[0].equals(Protocol.TIMEOUT)) {
				endGame(handler, Protocol.TIMEOUT);
			} else if (split[0].equals(Protocol.REQUESTGAME)) {
				handleRequest(split, handler);
			} else {
				print("Recieved unknown command");
				handler.sendMessageToClient(Protocol.ERROR + Protocol.DELIMITER1 
						+ Protocol.UNKNOWN + Protocol.DELIMITER1 + Protocol.COMMAND_END);
			}
		}
	}

	public void handleRequest(String[] split, ClientHandler handler) {
		print(String.format("%s requested a game with %s opponents", 
				handler.getClientName(), split[1]));
		int requestedNumberOfOpponents = Integer.parseInt(split[1]);
		match(handler, requestedNumberOfOpponents);
	}

	public void match(ClientHandler handler, int requestedNumberOfOpponents) {
		if (this.clientsSorted.containsKey(requestedNumberOfOpponents)) {
			HashSet<ClientHandler> setToUpdate = clientsSorted.get(requestedNumberOfOpponents);
			setToUpdate.add(handler);
			if (setToUpdate.size() == requestedNumberOfOpponents + 1) {
				clientsSorted.remove(requestedNumberOfOpponents);
				matched(setToUpdate);
			}
		} else {
			HashSet<ClientHandler> setToAdd = new HashSet<ClientHandler>();
			setToAdd.add(handler);
			this.clientsSorted.put(requestedNumberOfOpponents, setToAdd);
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
		try {
			Map<Colour, Integer> scores = getBoardOfClient(handler).getScore();

			if (reason.equals(Protocol.ABORTED) || reason.equals(Protocol.TIMEOUT)) {
				scores.replace(handler.getColour(), 0);
			}

			Map<ClientHandler, Integer> clientScores = new HashMap<ClientHandler, Integer>();
			assert clientsInMyGame.size() == scores.size();
			int score = 0;
			for (ClientHandler clientInGame : clientsInMyGame) {
				score = scores.get(clientInGame.getColour());
				clientScores.put(clientInGame, score);
			}

			for (int i = 0; i < clientsInMyGame.size(); i++) {
				ClientHandler clientWithMaxScore = getClientWithHighestScore(clientScores);
				System.out.println(clientWithMaxScore.getColour().toString() + " Score" + clientScores.get(clientWithMaxScore));
				stringToSend = stringToSend + clientWithMaxScore.getClientName() 
				+ Protocol.DELIMITER1 + clientScores.get(clientWithMaxScore)
				+ Protocol.DELIMITER1;
				clientScores.remove(clientWithMaxScore);

			}
			broadcastToSetOfClients(stringToSend + Protocol.COMMAND_END, clientsInMyGame);

			cleanUpGame(handler);

		} catch (NullPointerException e) {
			print(String.format("Game ended but was not yet initialized. "
					+ "Reason for end: %s", reason));
			if (reason.equals(Protocol.TIMEOUT)) {
				print(String.format("Time-out due to %s. Disconnecting players.", 
						handler.getClientName()));
				String timeOutString = Protocol.ENDGAME + Protocol.DELIMITER1
						+ Protocol.TIMEOUT + Protocol.DELIMITER1;
				for (ClientHandler handlerToAdd : clientsInMyGame) {
					timeOutString = timeOutString + handlerToAdd.getClientName() 
					+ Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1;
				}
				timeOutString = 	timeOutString + Protocol.COMMAND_END;	
				broadcastToSetOfClients(timeOutString, clientsInMyGame);

				// Disconnecting client that caused time-out
				handler.shutdown();

				//for (ClientHandler handlerToShutDown : clientsInMyGame) {
				//	handlerToShutDown.shutdown();
				//}
			}
			cleanUpGame(handler);
		}
	}

	public void cleanUpGame(ClientHandler handler) {
		Set<ClientHandler> clientsInMyGame = getClientsInMyGame(handler);
		Set<ClientHandler> setToRemove = new HashSet<ClientHandler>();
		for (Set<ClientHandler> clientsInAGame : clientsInGame) {
			if (clientsInAGame.contains(handler)) {
				setToRemove = clientsInAGame;
				break;
			}
		}

		clientsInGame.remove(setToRemove);
		if (clientsInMyGame != null) {
			for (ClientHandler handlerToClean : clientsInMyGame) {
				handlerToClean.cancelTimers();
				handlerToClean.resetMoves();
				clientBoardCombinations.remove(handlerToClean);
			}
		}
		removeHandler(handler);
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
		if (handler.isTurn()) {
			handler.tookTurn();
			Boolean gameOver = false;
			ActualBoard board = getBoardOfClient(handler);
			Move move = null;
			if (split[1].equals(Protocol.PASS)) {
				print(handler.getClientName() + " passed.");
				// did this clients also pass last time?
				if (handler.passedOnPreviousTurn()) {
					print(handler.getClientName() + 
							"also passed during his/her previous turn. Terminating game.");
					handleQuit(handler);
				}
				handler.pass(true);
			} else {
				handler.incrementNumberOfMoves();
				handler.pass(false);
				try {
					move = new Move(split[1], board.getDimension(), handler.getColour());
				} catch (InvalidCoordinateException e) {
					e.printStackTrace();
				}
				gameOver = processMoveLocally(move, board, handler);
				if (handler.movesPerformed() >= getNumberOfStones(handler)) {
					gameOver = true;
					print(handler.getClientName() + " ran out of stones");
				}
			}
			// Check for pass
			if (!gameOver) {
				communicateMove(move, handler);
			} else {
				print("Game has ended");
				endGame(handler, Protocol.FINISHED);
			}
		} else {
			print("Recieved move from " + handler.getClientName() + " but it's not his turn!");
		}
	}

	public boolean processMoveLocally(Move move, ActualBoard board, ClientHandler handler) {
		try {
			board.setIntersection(move);
			//processMoveInGui(move, board);
		} catch (InvalidMoveException e) {
			print("That's not a valid move!");
			handler.sendMessageToClient(Protocol.ERROR + Protocol.DELIMITER1 
					+ Protocol.INVALID + Protocol.DELIMITER1 + Protocol.COMMAND_END);
		}
		return board.gameOver();
	}

	public ActualBoard getBoardOfClient(ClientHandler handler) {
		return clientBoardCombinations.get(handler);
	}

	public void communicateMove(Move move, ClientHandler clientWhoMadeMove) {
		Set<ClientHandler> clientsInThisGame = getClientsInMyGame(clientWhoMadeMove);
		if (clientsInThisGame != null) {
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
			nextClient.getsTurn();
			String stringToSend = Protocol.TURN + Protocol.DELIMITER1 + 
					clientWhoMadeMove.getClientName() +
					Protocol.DELIMITER1 + moveAsString + Protocol.DELIMITER1 +
					nextClient.getClientName() + Protocol.DELIMITER1 + Protocol.COMMAND_END;

			broadcastToSetOfClients(stringToSend, clientsInThisGame);
		}
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
		ClientHandler blackClient = null;
		handler.setColour(firstColour);
		if (firstColour.equals(Colour.BLACK)) {
			blackClient = handler;
		}
		Set<ClientHandler> clientsInMyGame = getClientsInMyGame(handler);
		Set<Player> players = new HashSet<Player>();
		ClientHandler lastClient = handler;
		List<Integer> numbersOfClientsInThisGame = new ArrayList<Integer>();
		Set<String> playerNames = new HashSet<String>();
		Map<Colour, ClientHandler> clientColourCombinations = new HashMap<Colour, ClientHandler>();
		clientColourCombinations.put(firstColour, handler);
		playerNames.add(handler.getClientName());

		for (ClientHandler handlerWithNumber : clientsInMyGame) {
			numbersOfClientsInThisGame.add(handlerWithNumber.getNumber());
		}
		Colour lastColour = firstColour;
		for (int i = 0; i < clientsInMyGame.size() - 1; i++) {
			int numberOfNextClient = getNextNumberOfList(
					numbersOfClientsInThisGame, lastClient.getNumber());
			ClientHandler nextClient = allClients.get(numberOfNextClient);
			Colour nextColour = lastColour.next(clientsInMyGame.size());
			Player player = new OpponentPlayer(nextClient.getClientName(), nextColour);
			players.add(player);
			if (nextColour.equals(Colour.BLACK)) {
				blackClient = nextClient;
			}
			nextClient.setColour(nextColour);
			playerNames.add(nextClient.getClientName());
			clientColourCombinations.put(nextColour, nextClient);
			lastClient = nextClient;
			lastColour = nextColour;
		}
		lastColour = Colour.BLACK;

		String stringToSend = Protocol.START + Protocol.DELIMITER1 
				+ clientsInMyGame.size() + Protocol.DELIMITER1 
				+ Protocol.DELIMITER2 + Protocol.DELIMITER1
				+ boardSize + Protocol.DELIMITER1;

		for (int i = 0; i < clientsInMyGame.size(); i++) {
			ClientHandler clientToAdd = clientColourCombinations.get(lastColour);
			stringToSend = stringToSend + clientToAdd.getClientName() + Protocol.DELIMITER1;
			lastColour = lastColour.next(clientsInMyGame.size());			
		}


		for (ClientHandler handlerToAdd : clientsInMyGame) {
			String stringToSendToThisclient = 
					stringToSend.replace(Protocol.DELIMITER2, handlerToAdd.getColour().toString());
			stringToSendToThisclient = stringToSendToThisclient + Protocol.COMMAND_END;
			handlerToAdd.sendMessageToClient(stringToSendToThisclient);
		}
		//GoGUIIntegrator gogui = new GoGUIIntegrator(false, false, boardSize);
		//gogui.startGUI();
		//gogui.setBoardSize(boardSize);

		ActualBoard board = new ActualBoard(boardSize, clientsInMyGame.size()); //, gogui);

		for (ClientHandler clientInGame : clientsInMyGame) {
			clientBoardCombinations.put(clientInGame, board);
		}
		blackClient.getsTurn();
		broadcastToSetOfClients(Protocol.TURN + Protocol.DELIMITER1 + 
				blackClient.getClientName() 	+ Protocol.DELIMITER1 + 
				Protocol.FIRST + Protocol.DELIMITER1 + blackClient.getClientName()
				+ Protocol.DELIMITER1 + Protocol.COMMAND_END, clientsInMyGame);
	}

	public void processMoveInGui(Move move, ActualBoard board) {
		int row = move.getRow();
		int col = move.getCol();
		board.getGoGui().addStone(col, row, move.getColour());
	}

	public static void print(String msg) {
		System.out.println(msg);
	}

	public void addHandler(ClientHandler handler) {
		this.allClients.put(handler.getNumber(), handler);
	}

	public void removeHandler(ClientHandler handler) {
		int index = 0;
		allClients.remove(handler.getNumber());
		for (Map.Entry<Integer, HashSet<ClientHandler>> entry : this.clientsSorted.entrySet()) {
			if (entry.getValue().contains(handler)) {
				index = entry.getKey();
			}
		}
		if (index != 0) {
			this.clientsSorted.get(index).remove(handler);
		}
	}

	public int getNumberOfStones(ClientHandler handler) {
		ActualBoard board = getBoardOfClient(handler);
		int numOfIntersects = board.getDimension() * board.getDimension();
		if (board.getNumberOfPlayer() == 2) {
			if (numOfIntersects % 2 == 0 || !handler.getColour().equals(Colour.BLACK)) {
				return numOfIntersects / 2;
			} else {
				return (numOfIntersects / 2) + 1;
			}
		} else {
			return numOfIntersects / board.getNumberOfPlayer();
		}
	}

	// Return true if clients already exits
	public boolean checkForSameName(String name) {
		for (Map.Entry<Integer, ClientHandler> entry : this.allClients.entrySet()) {
			if (entry.getValue().getClientName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
