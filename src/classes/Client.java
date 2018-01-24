package classes;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import GUI.GoGUIIntegrator;
import errors.AlreadyPassedException;
import errors.CouldNotConnectException;
import errors.InvalidColourException;
import errors.InvalidCommandException;
import errors.InvalidCoordinateException;
import errors.InvalidHostException;
import errors.NameException;
import errors.NoValidPortException;
import errors.NotAnIntException;
import errors.NotYetImplementedException;
import errors.ScoresDoNotMatchException;

public class Client extends Thread {

	private Player clientPlayer;
	private Set<Player> players;
	private String name;
	private Socket sock;
	private BufferedReader in;
	private BufferedWriter out;
	private int numberOfPlayers;
	private static boolean isHuman;
	private boolean firstToConnect = false;
	private int boardSize;
	private Board board;
	private Colour clientColour;
	static final Scanner SCANNER = new Scanner(System.in);

	public static void main(String[] args) 
			throws InvalidHostException, NoValidPortException, InvalidCommandException {

		//askForInput();
		try {
			useDefaultInput();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void askForInput() 
			throws InvalidCommandException, InvalidHostException, NoValidPortException {
		print("How do you want to play? 1 = human, 2 = computer");
		try {
			int answer = SCANNER.nextInt();
			if (answer == 1) {
				isHuman = true;
				print("Playing as human.");
			} else if (answer == 2) {
				isHuman = false;
				print("Playing as computer");
			} else {
				SCANNER.close();
				throw new InvalidCommandException("Wrong input!");
			}
		} catch (NumberFormatException e) {
			throw new InvalidCommandException("Wrong input!");
		}

		SCANNER.nextLine();

		print("Please provide name: ");
		String name = SCANNER.nextLine();

		print(String.format("Welcome %s!", name));

		int port = 0;
		InetAddress host = null;

		print("Please provide host-ip: ");
		String hostString = SCANNER.nextLine();
		try {
			host = InetAddress.getByName(hostString);
		} catch (UnknownHostException e) {
			SCANNER.close();
			throw new InvalidHostException("Invalid host");
		}

		print("Please provide port: ");
		String portString = SCANNER.nextLine();
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			SCANNER.close();
			throw new NoValidPortException("Not a valid port!");
		}

		Client client;
		try {
			client = new Client(name, host, port);
			client.start();
		} catch (CouldNotConnectException e) {
			e.printStackTrace();
		}
	}
	public static String randomString() {
		int length = (int) (Math.random() * 5) + 6; // create an integer between 6 and 10
		String password = "";
		char c = 'a';
		for (int i = 0; i < length - 1; i++) {
			// Randomly choose a letter or number
			if ((int) Math.random() == 0) {
				c = (char) ('a' + 26 * Math.random());		
			} else {
				c = (char) ('0' + 10 * Math.random());		
			}
			password = password + c;
		}
		return password;
	}
	public static void useDefaultInput() throws UnknownHostException {
		isHuman = true;
		print("Playing as human.");
		String name = randomString();

		print(String.format("Welcome %s!", name));

		int port = 5000;
		InetAddress host = null;

		host = InetAddress.getLocalHost();

		Client client;
		try {
			client = new Client(name, host, port);
			client.start();
		} catch (CouldNotConnectException e) {
			e.printStackTrace();
		}
	}
	public Client(String name, InetAddress host, int port) throws CouldNotConnectException {
		try {
			this.sock = new Socket(host, port);
		} catch (IOException e) {
			throw new CouldNotConnectException("Could not connect to server!");
		}
		print("Connection succesfull!");
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

		players = new HashSet<Player>();

		send(Protocol.NAME + Protocol.DELIMITER1 + name + Protocol.DELIMITER1 +
				Protocol.VERSION + Protocol.DELIMITER1 + Protocol.VERSIONNUMBER +
				Protocol.DELIMITER1 + Protocol.EXTENSIONS + Protocol.DELIMITER1 +
				0 + Protocol.DELIMITER1 	+ 0 + Protocol.DELIMITER1 + 0 +
				Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 +
				Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 
				+ 0 + Protocol.DELIMITER1 + Protocol.COMMAND_END);
	}
	public void run() {
		try {
			String msg = in.readLine();
			while (true) {
				print(String.format("Message recieved: %s", msg));	
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					handleMessage(msg);
				} catch (InvalidCommandException | NotAnIntException e) {
					e.printStackTrace();
					print("Stopped listening");
					break;
				}
				msg = in.readLine();
			}
			shutdown();
		} catch (IOException e) {
			shutdown();
		}
	}

	public void handleMessage(String msg) throws InvalidCommandException, NotAnIntException {
		print(String.format("Handling message: %s", msg));
		String[] splitString = trimEndCommand(msg.split(Protocol.DELIMITER1), msg);

		if (splitString[0].equals(Protocol.START)) {
			handleMessageStart(splitString, msg);
		} else if (splitString[0].equals(Protocol.TURN)) {
			handleMessageTurn(splitString, msg);
		} else if (splitString[0].equals(Protocol.ENDGAME)) {
			handleMessageStart(splitString, msg);
		} else {
			throw new InvalidCommandException(String.format(
					"Server provided unkown arguments. Recieved message: %s", msg));
		}
	}

	public void handleMessageStart(String[] split, String msg) {

		// Case START <number of players>
		// This client was the first to connect and may decide on the game settings
		if (split.length == 2) {
			firstToConnect = true;
			try {
				this.numberOfPlayers = Integer.parseInt(split[1]);
			} catch (NumberFormatException e) {
				try {
					throw new InvalidCommandException(
							"Invalid command! Second argument must be a number.");
				} catch (InvalidCommandException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			try {
				provideGameSettings(numberOfPlayers);
			} catch (InvalidColourException | NotAnIntException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Case Start <number of players> <colour of this client> <boardsize> <player1> <player2> (3 etc.) to all players 
		} else if (split.length > 2) {

			// the other client decided on the game settings, lets implement them
			if (!firstToConnect) {
				implementSettings(split, msg);
				print("Settings implemented.");
			}

			// implements all the players (add them to this.players)
			implementPlayers(split, msg);
			print("Players implemented.");

			// create a new board
			this.board = new Board(boardSize, numberOfPlayers);
			print("Board generated");
		}
	}

	public void implementSettings(String[] split, String msg) {
		this.numberOfPlayers = Integer.parseInt(split[1]); 
		try {
			this.clientColour = Colour.getColour(split[2]);
		} catch (InvalidColourException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.boardSize = Integer.parseInt(split[3]);
	}

	public void implementPlayers(String[] split, String msg) {
		String[] playerNames = Arrays.copyOfRange(split, 4, split.length);
		if (playerNames.length == 2) {
			Colour opponentColour = clientColour.next(numberOfPlayers);
			for (int i = 0; i < playerNames.length; i++) {
				if (playerNames[i].equals(getClientName())) {
					players.add(generateClientPlayer(clientColour));
				} else {
					players.add(new OpponentPlayer(playerNames[i], opponentColour));
				}
			}
		} else {
			try {
				throw new NotYetImplementedException(String.format("Not yet implemented. Message: %s", msg));
			} catch (NotYetImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setupGame(String[] stringArray) 
			throws NotAnIntException, NotYetImplementedException {
		if (stringArray.length <= 5) {
			try {
				throw new InvalidCommandException("The server provided not enough arguments");
			} catch (InvalidCommandException e) {
				e.printStackTrace();
			}
		}

		try {
			if (this.numberOfPlayers != Integer.parseInt(stringArray[1])) {
				throw new InvalidCommandException(
						"The number of players defined by the server does not match");
			}
		} catch (NumberFormatException e) {
			throw new NotAnIntException(
					"A number as a second argument was expected but not provided");
		} catch (InvalidCommandException e) {
			e.printStackTrace();
		}	
		try {
			if (!Colour.getColour(stringArray[2]).equals(this.clientPlayer.getColour())) {
				throw new InvalidColourException(
						"Colour provided by server was not equal to client's colour");
			}
		} catch (InvalidColourException e) {
			e.printStackTrace();
		}
		try {
			if (this.boardSize != Integer.parseInt(stringArray[3])) {
				throw new InvalidCommandException(
						"The boardsize defined buy the server does not match");
			}
		} catch (NumberFormatException e) {
			throw new NotAnIntException(
					"A number as a third argument was expected but not provided");
		} catch (InvalidCommandException e) {
			e.printStackTrace();
		}
		String[] namesOfPlayers = Arrays.copyOfRange(stringArray, 4, stringArray.length - 1);
		if (!namePresent(namesOfPlayers, this.name)) {
			try {
				throw new InvalidCommandException("The names provided by the server do not match");
			} catch (InvalidCommandException e) {
				e.printStackTrace();
			}
		}

		if (numberOfPlayers == 2) {
			String opponentName = null;
			Colour opponentColour = null;
			if (namesOfPlayers[0].equals(name)) {
				opponentName = namesOfPlayers[1];
			} else {
				opponentName = namesOfPlayers[0];
			}
			if (this.clientPlayer.getColour().equals(Colour.BLACK)) {
				opponentColour = Colour.WHITE;
			} else {
				opponentColour = Colour.BLACK;
			}
			players.add(new OpponentPlayer(opponentName, opponentColour)); 
		} else {
			//// remove name of clientPlayer from list of players to implement
			//for (int i = 0; i < namesOfPlayers.length; i++) {
			//	if (!namesOfPlayers[i].equals(name)) {
			//		players.add(new OpponentPlayer(namesOfPlayers[i], ));
			//	}
			//}
			throw new NotYetImplementedException("MULTIPLE PLAYERS IS NOT YET IMPLEMENTED!");
		}	
	}

	public void handleMessageTurn(String[] split, String msg) {
		while (this.board == null) {
			try {
				Thread.sleep(500);
				print("Waiting...");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// incorrect arguments provided, a TURN-message should be 4 arguments
		if (split.length != 4) {
			try {
				throw new InvalidCommandException(String.format(
						"Server provided incorrect arguments. Message was %s", msg));
			} catch (InvalidCommandException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 

		Player playerWhoJustHadATurn = getPlayer(split[1]);
		Player playerToMakeMove = getPlayer(split[3]);

		print(String.format("Player who just had a move: %s", playerWhoJustHadATurn.getName()));
		print(String.format("Player to make a move: %s", playerToMakeMove.getName()));
		print(String.format("Client player: %s",  this.clientPlayer.getName()));
		// opponent passed
		if (split[2].equals(Protocol.PASS)) {
			if (playerWhoJustHadATurn.getLastMoveWasPass()) {
				try {
					throw new AlreadyPassedException("Player already passed last time!");
				} catch (AlreadyPassedException e) {
					e.printStackTrace();
				}	
			}
			playerWhoJustHadATurn.pass(true);


		} else if (!split[2].equals(Protocol.FIRST)) {
			print("Processing opponents move");
			int move = getMove(split[2]);
			board.setIntersection(move, playerWhoJustHadATurn.getColour());
		} 
		
		if (playerToMakeMove.getName().equals(this.clientPlayer.getName())) { 
			print("Asking player to make a move");
			int moveToMake = playerToMakeMove.determineMove(board);
			board.setIntersection(moveToMake, playerToMakeMove.getColour());
			String move = indexToMove(moveToMake);
			send(Protocol.MOVE + Protocol.DELIMITER1 + move + 
					Protocol.DELIMITER1 + Protocol.COMMAND_END);
		} else {
			print("Waiting for opponent to make a move");
		}
	}

	public void handleMessageEndGame(String[] split, String msg) {
		if (split[1].equals(Protocol.ABORTED)) {
			print("Game was aborted!");	
		} else if (split[1].equals(Protocol.FINISHED)) {
			print("Game has finished!");	
		} else if (split[1].equals(Protocol.TIMEOUT)) {
			print("Game was aborted due to timeout!");	
		} else {
			try {
				throw new InvalidCommandException("Server provided incorrect arguments.");
			} catch (InvalidCommandException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (numberOfPlayers == 2) {
			if (split.length != 6) {
				try {
					throw new InvalidCommandException("Server provided incorrect arguments.");
				} catch (InvalidCommandException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			Map<Player, Integer> playersToCheck = new HashMap<Player, Integer>(); 	 
			checkScores(playersToCheck, board);
			print(String.format(
					"%s won with a score of %s, %s lost with a score of %s",
					split[3], split[4], split[5], split[6]));
			endGame();
		} else {
			try {
				throw new NotYetImplementedException("Not yet implemented!");
			} catch (NotYetImplementedException e) {
				e.printStackTrace();
			}
		}
	}

	public void checkEndCommand(String[] array, String message) {
		if (!array[array.length - 1].equals(Protocol.COMMAND_END)) {
			try {
				throw new InvalidCommandException(String.format(
						"Message did not end with end-command. Message was: %s", message));
			} catch (InvalidCommandException e) {
				e.printStackTrace();
			}
		}
	}

	public String[] trimEndCommand(String[] array, String msg) {
		checkEndCommand(array, msg);
		return Arrays.copyOfRange(array, 0, array.length - 1);
	}

	public void recieveSettings(String[] msg) {
		if (numberOfPlayers == 2) {
			Colour opponentColour;
			try {
				opponentColour = Colour.getColour(msg[1]);

				Colour myColour = null;
				if (opponentColour.equals(Colour.BLACK)) {
					myColour = Colour.WHITE;
				} else {
					myColour = Colour.BLACK;
				}
				print(String.format("Your opponents color is %s. "
						+ "You will play as %s. The boardSize is "
						+ msg[2], opponentColour.toString(), myColour.toString()));
			} catch (InvalidColourException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void checkScores(Map<Player, Integer> playersToCheck, Board boardArg) {
		Map<Colour, Integer> scoresFromBoard = boardArg.getScore();
		for (Map.Entry<Player, Integer> entry : playersToCheck.entrySet()) {
			if (entry.getValue() != scoresFromBoard.get(entry.getKey().getColour())) {
				try {
					throw new ScoresDoNotMatchException(String.format(
							"Scores do not match! Server provided a score of %s for player %s "
									+ "but local administration indicates a score of %s.",
									entry.getValue(), entry.getKey().getName(), 
									scoresFromBoard.get(entry.getKey().getColour())));
				} catch (ScoresDoNotMatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void endGame() {
		print("Ending game");
		try {
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String indexToMove(int index) {
		return indexToRow(index) + Protocol.DELIMITER2 + indexToCol(index);
	}

	public int indexToCol(int index) {
		return index - ((index % this.boardSize) * boardSize);
	}

	public int indexToRow(int index) {
		return index % this.boardSize;
	}

	public int getMove(String move) {
		String[] moveArray = move.split(Protocol.DELIMITER2);
		if (moveArray.length != 2) {
			try {
				throw new InvalidCoordinateException("Provided coordinates were not valid!");
			} catch (InvalidCoordinateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			return calculateIndex(Integer.parseInt(moveArray[1]),
					Integer.parseInt(moveArray[0]), this.boardSize);
		} catch (NumberFormatException e) {
			try {
				throw new InvalidCoordinateException("Provided coordinates were not valid!");
			} catch (InvalidCoordinateException e1) {
				e1.printStackTrace();
			}
		}
		return -1000;
	}

	public int calculateIndex(int col, int row, int dimensionOfBoard) {
		return (row * dimensionOfBoard) + col;
	}

	public Player getPlayer(String playerName) {
		for (Player player : players) {
			if (player.getName().equals(playerName)) {
				return player;
			}
		}
		try {
			throw new NameException("Unkown name!");
		} catch (NameException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean namePresent(String[] array, String nameArg) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(nameArg)) {
				return true;
			}
		}
		return false;
	}

	public Player generateClientPlayer(Colour colour) {
		if (Client.isHuman) {
			return new HumanPlayer(this.name, colour);
		} else {
			Strategy strategy = new RandomStrategy();
			return new ComputerPlayer(colour, strategy);	
		}
	}

	// if the client is the first player, he/she should provide game settings
	public void provideGameSettings(int numberOfPlayersArg) 
			throws InvalidColourException, NotAnIntException {
		Colour colour = Colour.BLACK;
		if (numberOfPlayersArg == 2) {
			print("You are going to play Go with one other player!");
		} else {
			print("You are going to play Go with " + (numberOfPlayersArg - 1) + " other players!"); 
		}
		print("Please choose a colour! available options:");
		print(colour.first().toString());
		for (int i = 0; i < numberOfPlayersArg - 1; i++) {
			colour = colour.next(numberOfPlayersArg);
			print(colour.toString());
		}
		colour = colour.first();

		String colourString = "";

		while (true) {
			try {
				if (SCANNER.hasNextLine()) {
					colourString = SCANNER.nextLine(); //Should be a blocking call
					this.clientColour = Colour.getColour(colourString.toUpperCase());
					print(String.format("You choose %s", this.clientColour));
					break;
				} 
			} catch (errors.InvalidColourException e) {
				print("This is not a valid colour. Please try again.");
			}
		}
		print("Please enter a board size: ");
		while (true) {
			try {
				this.boardSize = Integer.parseInt(SCANNER.nextLine());
				if (this.boardSize > 0 && this.boardSize < 100) {
					break;
				}
				print("Board size should be a postive integer "
						+ "smaller than 100. Please try again.");
			} catch (NumberFormatException e) {
				throw new NotAnIntException("You did not enter a valid integer!");
			}
		}
		send(Protocol.SETTINGS + Protocol.DELIMITER1 + clientColour.toString() + 
				Protocol.DELIMITER1 + boardSize + Protocol.DELIMITER1 + Protocol.COMMAND_END);
	}

	public void send(String msg) {
		checkEndCommand(msg.split(Protocol.DELIMITER1), msg);
		try {
			out.write(msg);
			out.newLine();
			out.flush();
			print("Message sent");
			print(msg);
		} catch (IOException e) {
			shutdown();
		}
	}

	public void shutdown() {
		send(Protocol.QUIT + Protocol.DELIMITER1 + Protocol.COMMAND_END);
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

	public static void print(String msg) {
		System.out.println(msg);
	}

	public void printArray(String[] array) {
		for (int i = 0; i < array.length; i++) {
			print(array[i]);
		}
	}
}
