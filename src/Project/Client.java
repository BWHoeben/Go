package Project;

import Project.Player;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import GUI.GoGUIIntegrator;
import Project.Errors.CouldNotConnectException;
import Project.Errors.InvalidColourException;
import Project.Errors.InvalidCommandException;
import Project.Errors.InvalidCoordinateException;
import Project.Errors.InvalidHostException;
import Project.Errors.NameException;
import Project.Errors.NoValidPortException;
import Project.Errors.NotAnIntException;
import Project.Errors.NotYetImplementedException;
import Project.Colour;


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
	private Game game;

	public Client(String name, InetAddress host, int port) throws CouldNotConnectException {
		try {
			this.sock = new Socket(host, port);
		} catch (IOException e) {
			throw new CouldNotConnectException("Could not connect to server!");
		}
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

		send(Protocol.NAME + Protocol.DELIMITER1 + name + Protocol.DELIMITER1 + Protocol.VERSION + Protocol.DELIMITER1 + 
				Protocol.VERSIONNUMBER + Protocol.DELIMITER1 + Protocol.EXTENSIONS + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1
				+ 0 + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 + Protocol.COMMAND_END);
	}

	public void run() {
		try {
			String msg = in.readLine();
			while (msg != null) {
				try {
					handleMessage(msg);
				} catch (InvalidCommandException | NotAnIntException e) {
					e.printStackTrace();
				}
				msg = in.readLine();
			}
			shutdown();
		} catch (IOException e) {
			shutdown();
		}
	}

	public void checkEndCommand(String[] array) {
		if (!array[array.length - 1].equals(Protocol.COMMAND_END)) {
			try {
				throw new InvalidCommandException("Message did not end with end-command");
			} catch (InvalidCommandException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String[] trimEndCommand(String[] array) {
		checkEndCommand(array);
		return Arrays.copyOfRange(array, array.length - 1, array.length - 2);
	}

	public void handleMessage(String msg) throws InvalidCommandException, NotAnIntException {
		String[] splitString = trimEndCommand(msg.split(Protocol.DELIMITER1));

		if (splitString[0].equals(Protocol.START) && splitString.length == 2) {
			firstToConnect = true;
			try {
				this.numberOfPlayers = Integer.parseInt(splitString[1]);
			} catch (NumberFormatException e) {
				throw new InvalidCommandException("Invalid command! Second argument must be a number.");
			}
			try {
				provideGameSettings(numberOfPlayers);
			} catch (InvalidColourException | NotAnIntException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (splitString[0].equals(Protocol.START) && splitString.length > 2) {
			if (!firstToConnect) {
				try {
					try {
						prepareGame(splitString);
					} catch (NotYetImplementedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (NameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotAnIntException e) {
					e.printStackTrace();
				}
			} else {
				try {
					setupGame(splitString);
				} catch (NotYetImplementedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (splitString[0].equals(Protocol.TURN)) {
			if (splitString.length != 4) {
				throw new InvalidCommandException("Server provided incorrect arguments.");
			} 
			Player playerToMakeMove = getPlayer(splitString[3]);
			if (!splitString[2].equals(Protocol.FIRST)) {
			Player playerWhoMadeMove = getPlayer(splitString[3]);
			int move = getMove(splitString[2]);
			this.game.getBoard().setIntersection(move, playerWhoMadeMove.getState());
			} 
			int moveToMake = playerToMakeMove.determineMove(game.getBoard());
			this.game.getBoard().setIntersection(moveToMake, playerToMakeMove.getState());
			String move = indexToMove(moveToMake);
			send(Protocol.MOVE + Protocol.DELIMITER1 + move);
		}

	}
	
	public String indexToMove(int index) {
		int col = indexToCol(index);
		int row = indexToRow(index);
		return row + Protocol.DELIMITER2 + col;
	}
	
	public int indexToCol(int index) {
		return index - (index % this.boardSize);
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
		int row = Integer.parseInt(moveArray[0]);
		int col = Integer.parseInt(moveArray[1]);
		return calculateIndex(col, row, this.boardSize);
		} catch (NumberFormatException e) {
			try {
				throw new InvalidCoordinateException("Provided coordinates were not valid!");
			} catch (InvalidCoordinateException e1) {
				// TODO Auto-generated catch block
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
			if (player.getName().equals(playerName));
			return player;
		}
		try {
			throw new NameException("Unkown name!");
		} catch (NameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// if the player is the first to connect, the setup of the game has to be completed
	public void setupGame(String[] stringArray) throws NotAnIntException, NotYetImplementedException {
		if (stringArray.length <= 5) {
			try {
				throw new InvalidCommandException("The server provided not enough arguments");
			} catch (InvalidCommandException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			if (this.numberOfPlayers != Integer.parseInt(stringArray[1])) {
				throw new InvalidCommandException("The number of players defined by the server does not match");
			}
		} catch (NumberFormatException e) {
			throw new NotAnIntException("A number as a second argument was expected but not provided");
		} catch (InvalidCommandException e) {
			e.printStackTrace();
		}	
		try {
			if (!getColour(stringArray[2]).equals(clientPlayer.getState())) {
				throw new InvalidColourException("Colour provided by server was not equal to client's colour");
			}
		} catch (InvalidColourException e) {
			e.printStackTrace();
		}
		try {
			if (this.boardSize != Integer.parseInt(stringArray[3])) {
				throw new InvalidCommandException("The boardsize defined buy the server does not match");
			}
		} catch (NumberFormatException e) {
			throw new NotAnIntException("A number as a third argument was expected but not provided");
		} catch (InvalidCommandException e) {
			e.printStackTrace();
		}

		String[] namesOfPlayers = Arrays.copyOfRange(stringArray, 4, stringArray.length - 1);

		if (!namePresent(namesOfPlayers, this.name)) {
			try {
				throw new InvalidCommandException("The names provided by the server do not match");
			} catch (InvalidCommandException e) {
				// TODO Auto-generated catch block
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
			if (clientPlayer.getState().equals(Colour.BLACK)) {
				opponentColour = Colour.WHITE;
			} else {
				opponentColour = Colour.BLACK;
			}
			players.add(new OpponentPlayer(opponentName, opponentColour));
		} 
		else {
			//// remove name of clientPlayer from list of players to implement
			//for (int i = 0; i < namesOfPlayers.length; i++) {
			//	if (!namesOfPlayers[i].equals(name)) {
			//		players.add(new OpponentPlayer(namesOfPlayers[i], ));
			//	}
			//}
			throw new NotYetImplementedException("MULTIPLE PLAYERS IS NOT YET IMPLEMENTED!");
		}	
		startGame();
	}

	public boolean namePresent(String[] array, String name) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void prepareGame(String[] stringArray) throws NameException, NotAnIntException, NotYetImplementedException {
		int boardDimension = 0;
		try {
			this.numberOfPlayers = Integer.parseInt(stringArray[1]);
			boardDimension = Integer.parseInt(stringArray[3]);
		} catch (NumberFormatException e) {
			throw new NotAnIntException("One or more arguments that should have been integers weren't");
		}

		if (numberOfPlayers == 2) {
			Colour colour = null;
			try {
				colour = getColour(stringArray[2]);
			} catch (InvalidColourException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.clientPlayer = generateClientPlayer(colour);
			String[] names = new String[2];
			String opponentName = null;
			names[0] = stringArray[4];
			names[1] = stringArray[5];
			if (names[0].equals(names[1])) {
				throw new NameException(MessageFormat.format("Players must have different names! Both names are {0}", names[0])) ;
			} else if (names[0].equals(name)) {
				opponentName = names[1];
			} else if (names[1].equals(name)) {
				opponentName = names[0];
			} else {
				throw new NameException(MessageFormat.format("Server provided incorrect names. This client's name is {0}. Server provided {1} and {2}", this.name, names[0], names[1])); 
			}

			Player opponentPlayer = null;

			if (colour.equals(colour.BLACK)) {
				opponentPlayer = new OpponentPlayer(opponentName, colour.WHITE);		
			} else {
				opponentPlayer = new OpponentPlayer(opponentName, colour.BLACK);
			} 
			players.add(opponentPlayer);
			players.add(this.clientPlayer);

			startGame();
		} else {
			throw new NotYetImplementedException("MULTIPLE PLAYERS IS NOT YET IMPLEMENTED!");
		}
	}

	public Player generateClientPlayer(Colour colour) {
		if (this.isHuman) {
			return new HumanPlayer(this.name, colour);
		} else {
			Strategy strategy = new RandomStrategy();
			return new ComputerPlayer(colour, strategy);	
		}
	}

	public Colour getColour(String colourString) throws InvalidColourException {
		Colour colour = null;
		colour = colour.first();
		while (!colour.toString().equals(colourString)) {
			colour = colour.next();
			if (colour.equals(colour.first())) {
				throw new InvalidColourException("Invalid/Unkown colour!");
			}
		}
		return colour;
	}

	// if the client is the first player, he/she should provide game settings
	public void provideGameSettings(int numberOfPlayers) throws InvalidColourException, NotAnIntException {
		Colour colour = Colour.EMPTY;
		if (numberOfPlayers == 2) {
			print("You are going to play Go with one other player!");
		} else {
			print("You are going to play Go with " + (numberOfPlayers - 1) + " other players!"); 
		}
		print("Please choose a colour! available options:");
		print(colour.first().toString());
		for (int i = 0; i < numberOfPlayers - 1; i++) {
			colour = colour.next();
			print(colour.toString());
		}
		colour = colour.first();
		Scanner scanner = new Scanner(System.in);
		String chosenColour = scanner.nextLine();
		Colour playerColour = null;
		for (int i = 0; i < numberOfPlayers; i++) {
			if (colour.toString().equals(chosenColour)) {
				playerColour = colour;
			} else {
				colour = colour.next();
			}
		}
		if (playerColour == null) {
			scanner.close();
			throw new InvalidColourException("You did not enter a valid colour! :(");
		}
		this.clientPlayer = generateClientPlayer(playerColour);
		players.add(clientPlayer);

		print("Please enter a boardsize: ");
		try {
			this.boardSize = scanner.nextInt();
		} catch (NumberFormatException e) {
			scanner.close();
			throw new NotAnIntException("You did not enter a valid integer!");
		}
		scanner.close();
		send(Protocol.SETTINGS + Protocol.DELIMITER1 + playerColour.toString() + Protocol.DELIMITER1 + boardSize);

		// Now wait for other player
		System.out.println("Waiting for other player...");
	}

	public void startGame() {
		this.game = new Game(players, this.boardSize, new GoGUIIntegrator(true, true, this.boardSize));
	}

	public void send(String msg) {
		checkEndCommand(msg.split(Protocol.DELIMITER1));
		try {
			out.write(msg);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			shutdown();
		}
	}

	public void shutdown() {
		send(Protocol.QUIT);
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

	public void print(String msg) {
		System.out.println(msg);
	}

	public static void main(String[] args) throws InvalidHostException, NoValidPortException {
		if (args[0].equals("Human")) {
			isHuman = true;
		} else {
			isHuman = false;
		}

		System.out.println("Please provide name: ");
		Scanner scanner = new Scanner(System.in);
		String name = scanner.nextLine();

		int port = 0;
		InetAddress host = null;

		System.out.println("Please provide host-ip: ");
		String hostString = scanner.nextLine();
		try {
			host = InetAddress.getByName(hostString);
		} catch (UnknownHostException e) {
			scanner.close();
			throw new InvalidHostException("Invalid host");
		}

		System.out.println("Please provide port: ");
		String portString = scanner.nextLine();
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			scanner.close();
			throw new NoValidPortException("Not a valid port!");
		}

		Client client;
		try {
			client = new Client(name, host, port);
			client.start();
		} catch (CouldNotConnectException e) {
			e.printStackTrace();
		}
		scanner.close();
	}
}
