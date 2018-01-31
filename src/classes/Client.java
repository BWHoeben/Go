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
import errors.CouldNotConnectException;
import errors.InvalidColourException;
import errors.InvalidCommandException;
import errors.InvalidCoordinateException;
import errors.InvalidHostException;
import errors.InvalidMoveException;
import errors.NameException;
import errors.NoValidPortException;
import errors.NotAnIntException;
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
	private ActualBoard board;
	private Colour clientColour;
	static final Scanner SCANNER = new Scanner(System.in);
	private int opponents;
	private GoGUIIntegrator gogui;

	public static void main(String[] args) 
			throws InvalidHostException, NoValidPortException, InvalidCommandException {

		//askForInput();

		try {
			useDefaultInput();
		} catch (UnknownHostException e) {
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
		while (true) {
			print("Please provide host-ip: ");
			String hostString = SCANNER.nextLine();
			try {
				host = InetAddress.getByName(hostString);
				break;
			} catch (UnknownHostException e) {
				print("Invalid host, try again");
			}
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

	public int askForOpponents() {
		while (true) {
			print("How many opponents do you want: 1, 2 or 3?");
			String answer = SCANNER.nextLine();
			if (answer.equals("1") || answer.equals("2") || answer.equals("3")) {
				return Integer.parseInt(answer);
			} else {
				print("Thats not a valid answer, please try again");
			}
		}
	}

	public static void useDefaultInput() throws UnknownHostException {
		isHuman = false;
		print("Playing as Computer.");
		String name = randomString();

		print(String.format("Welcome %s!", name));

		int port = Protocol.DEFAULT_PORT;
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
		int i = 0;
		while (true) {
			try {
				this.sock = new Socket(host, port);
				break;
			} catch (IOException e) {
				i++;
				print(String.format("Could not connect to server! "
						+ "Trying again in %s seconds", 5 * i));
				try {
					Thread.sleep(5000 * i);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		print("Connection succesfull!");

		//this.opponents = askForOpponents();

		this.opponents = 1;

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

		this.players = new HashSet<Player>();
		// CASE 
		// NAME <name> VERSION <#> EXTENSIONS CHAT CHALLENGE 
		// LEADERBOARD SECURITY 2+ SIMULTANEOUS MULTIPLEMOVES
		int multi;
		if (this.opponents > 1) {
			multi = 1;
		} else {
			multi = 0;
		}
		
		send(Protocol.NAME + Protocol.DELIMITER1 + name + Protocol.DELIMITER1 +
				Protocol.VERSION + Protocol.DELIMITER1 + Protocol.VERSIONNUMBER +
				Protocol.DELIMITER1 + Protocol.EXTENSIONS + Protocol.DELIMITER1 +
				0 + Protocol.DELIMITER1 	+ 0 + Protocol.DELIMITER1 + 0 +
				Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + multi +
				Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 
				+ 0 + Protocol.DELIMITER1 + Protocol.COMMAND_END);
		sendRequest(this.opponents);
	}

	public void sendRequest(int numberOfopponents) {
		send(Protocol.REQUESTGAME + Protocol.DELIMITER1 + numberOfopponents + 
				Protocol.DELIMITER1 + Protocol.RANDOM + Protocol.DELIMITER1 + Protocol.COMMAND_END);
	}

	public void run() {
		try {
			String msg = in.readLine();
			while (msg != null) {
				print(String.format("Message recieved: %s", msg));	
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
		String[] splitString = trimEndCommand(msg.split("\\" + Protocol.DELIMITER1), msg);

		if (splitString[0].equals(Protocol.START)) {
			handleMessageStart(splitString, msg);
		} else if (splitString[0].equals(Protocol.TURN)) {
			handleMessageTurn(splitString, msg);
		} else if (splitString[0].equals(Protocol.ENDGAME)) {
			handleMessageEndGame(splitString, msg);
		} else if (splitString[0].equals(Protocol.ERROR))	 {
			handleMessageError(splitString, msg);
		} else {
			throw new InvalidCommandException(String.format(
					"Server provided unkown arguments. Recieved message: %s", msg));
		}
	}

	public void handleMessageError(String[] split, String msg) {
		if (split[1].equals(Protocol.NAMETAKEN)) {
			print("Name is occupied. Please try to reconnect with another name.");
			shutdown();
		} else {
			print("Recieved error message from server. Message was: " + msg);
			print("Honestly, I don't really care for this server. Bye.");
			shutdown();
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
					e1.printStackTrace();
				}
			}
			try {
				provideGameSettings(numberOfPlayers);
			} catch (InvalidColourException | NotAnIntException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Case Start <number of players> <colour of this client> 
			// <boardsize> <player1> <player2> (3 etc.) to all players 
		} else if (split.length > 2) {

			// the other client decided on the game settings, lets implement them
			if (!firstToConnect) {
				implementSettings(split, msg);
			}

			// implements all the players (add them to this.players)
			implementPlayers(split, msg);

			// start the GUI
			if (this.gogui == null) {
				this.gogui = new GoGUIIntegrator(false, false, boardSize);
				this.gogui.startGUI();
			}
			this.gogui.setBoardSize(boardSize);
			// create a new board
			this.board = new ActualBoard(boardSize, numberOfPlayers, gogui);
		}
	}

	public void implementSettings(String[] split, String msg) {
		this.numberOfPlayers = Integer.parseInt(split[1]); 
		try {
			this.clientColour = Colour.getColour(split[2]);
			print("You're playing as " + this.clientColour.toString());
		} catch (InvalidColourException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.boardSize = Integer.parseInt(split[3]);
	}

	public void implementPlayers(String[] split, String msg) {
		String[] playerNames = Arrays.copyOfRange(split, 4, split.length);
		if (numberOfPlayers == 2) {
			Colour opponentColour = clientColour.next(numberOfPlayers);
			this.clientPlayer = generateClientPlayer(clientColour);
			players.add(this.clientPlayer);
			String opponentName;
			if (playerNames[0].equals(clientPlayer.getName())) {
				opponentName = playerNames[1];
			} else {
				opponentName = playerNames[0];
			}
			players.add(new OpponentPlayer(opponentName, opponentColour));
		} else {
			Colour colourToAdd = Colour.BLACK;
			for (int i = 0; i < playerNames.length; i++) {
				if (playerNames[i].equals(getClientName())) {
					this.clientPlayer = generateClientPlayer(colourToAdd);
					players.add(this.clientPlayer);
				} else {
					players.add(new OpponentPlayer(playerNames[i], colourToAdd));
				}
				colourToAdd = colourToAdd.next(numberOfPlayers);
			}
		}
	}

	public void handleMessageTurn(String[] split, String msg) {
		if (this.board == null) {
			print("Board is not yet implemented, ignoring message.");
		} else {

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
			boolean opponentDoublePass = false;
			// opponent passed
			if (split[2].equals(Protocol.PASS) && !split[1].equals(clientPlayer.getName())) {
				if (playerWhoJustHadATurn.getLastMoveWasPass()) {
					print(playerWhoJustHadATurn.getName() + " passed two times in a row!");	
					opponentDoublePass = true;
				}
				playerWhoJustHadATurn.pass(true);


			} else if (!split[2].equals(Protocol.FIRST) 
					&& !split[1].equals(clientPlayer.getName())) {
				print("Processing opponents move");
				playerWhoJustHadATurn.pass(false);
				Move move;
				try {
					move = new Move(split[2], boardSize, playerWhoJustHadATurn.getColour());
					board.setIntersection(move);
					processMoveInGui(move);
				} catch (InvalidCoordinateException e) {
					print("That's not a valid coordinate");
					e.printStackTrace();
				} catch (InvalidMoveException e) {
					print("That's not a valid move");
				}
			} 

			if (playerToMakeMove.getName().equals(this.clientPlayer.getName()) 
					&& !opponentDoublePass) { 
				Move moveToMake = playerToMakeMove.determineMove(board);

				if (moveToMake.getQuit()) {
					send(Protocol.QUIT + Protocol.DELIMITER1 + Protocol.COMMAND_END);
				} else if (moveToMake.getExit()) {	
					send(Protocol.EXIT + Protocol.DELIMITER1 + Protocol.COMMAND_END);
				} else {
					String move = null;
					if (moveToMake.getPass()) {
						move = "PASS";
					} else {
						move = moveToMake.toString();
						try {
							board.setIntersection(moveToMake);
						} catch (InvalidMoveException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (!moveToMake.getPass() && !moveToMake.getQuit()) {
						processMoveInGui(moveToMake);
					}
					send(Protocol.MOVE + Protocol.DELIMITER1 + move + 
							Protocol.DELIMITER1 + Protocol.COMMAND_END);
				}
			} 
		}
	}

	public void processMoveInGui(Move move) {
		int row = move.getRow();
		int col = move.getCol();
		gogui.addStone(col, row, move.getColour());
	}

	// CASE: ENDGAME REASON PLAYER1 SCORE PLAYER2 SCORE PLAYER3 SCORE... 
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
		if (board != null) {
			try {
				Map<Player, Integer> playersToCheck = new HashMap<Player, Integer>();
				for (int i = 0; i < numberOfPlayers; i++) {
					playersToCheck.put(getPlayer(split[(i * 2) + 2]), 
							Integer.parseInt(split[(i * 2) + 3]));
				}
				//checkScores(playersToCheck, board);
			} catch (NullPointerException e) {
				print("Unanble to verify scores with local data");
			}

			if (split[3].equals(split[5])) {
				print("It's a draw!");
			} else {
				print(String.format(
						"%s won with a score of %s", split[2], split[3]));
			}
		}
		while (true) {
			print("Do you want to play again? YES or NO");
			String answer = SCANNER.nextLine();
			if (answer.toUpperCase().equals(Protocol.YES)) {
				this.opponents = askForOpponents();
				resetVariables();
				sendRequest(this.opponents);
				break;
			} else if (answer.equals(Protocol.NO)) {
				endGame();	
				gogui.quit();
				break;
			} else {
				print("Invalid command try again.");
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

	public void checkScores(Map<Player, Integer> playersToCheck, ActualBoard boardArg) {
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
			return new HumanPlayer(this.name, colour, Client.SCANNER);
		} else {
			//Strategy strategy = new RandomStrategy();
			return new ComputerPlayer(this.name, colour); //, strategy);	
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
		if (Client.isHuman) {
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
					if (this.boardSize > 4 && this.boardSize < 20) {
						break;
					}
					print("Board size should be a integer bigger than four and"
							+ "smaller than twenty. Please try again.");
				} catch (NumberFormatException e) {
					print("You did not enter a valid integer. Please try again.");
				}
			}
		} else {
			clientColour = Colour.BLACK;
			boardSize = 10;
		}
		send(Protocol.SETTINGS + Protocol.DELIMITER1 + clientColour.toString() + 
				Protocol.DELIMITER1 + boardSize + Protocol.DELIMITER1 + Protocol.COMMAND_END);
	} 
	public void send(String msg) {
		checkEndCommand(msg.split("\\" + Protocol.DELIMITER1), msg);
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
		System.out.flush();
	}

	public void resetVariables() {
		this.clientPlayer = null;
		this.players = new HashSet<Player>();
		this.board = null;
		this.clientColour = null;
		if (this.gogui != null) {
			this.gogui.clearBoard();
		}
	}

	public void printArray(String[] array) {
		for (int i = 0; i < array.length; i++) {
			print(array[i]);
		}
	}
}
