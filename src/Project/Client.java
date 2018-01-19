package Project;


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
import Project.Errors.AlreadyPassedException;
import Project.Errors.CouldNotConnectException;
import Project.Errors.InvalidColourException;
import Project.Errors.InvalidCommandException;
import Project.Errors.InvalidCoordinateException;
import Project.Errors.InvalidHostException;
import Project.Errors.NameException;
import Project.Errors.NoValidPortException;
import Project.Errors.NotAnIntException;
import Project.Errors.NotYetImplementedException;
import Project.Errors.ScoresDoNotMatchException;

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
	static final Scanner SCANNER = new Scanner(System.in);


	public Client(String name, InetAddress host, int port) throws CouldNotConnectException {
		try {
			this.sock = new Socket(host, port);
		} catch (IOException e) {
			throw new CouldNotConnectException("Could not connect to server!");
		}
		System.out.println("Connection succesfull!");
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

	public void handleMessage(String msg) throws InvalidCommandException, NotAnIntException {
		String[] splitString = trimEndCommand(msg.split(Protocol.DELIMITER1), msg);

		if (splitString[0].equals(Protocol.START) && splitString.length == 2) {
			firstToConnect = true;
			try {
				this.numberOfPlayers = Integer.parseInt(splitString[1]);
			} catch (NumberFormatException e) {
				throw new InvalidCommandException(
						"Invalid command! Second argument must be a number.");
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
						e.printStackTrace();
					}
				} catch (NameException e) {
					e.printStackTrace();
				} catch (NotAnIntException e) {
					e.printStackTrace();
				}
			} else {
				try {
					setupGame(splitString);
				} catch (NotYetImplementedException e) {
					e.printStackTrace();
				}
			}
		} else if (splitString[0].equals(Protocol.TURN)) {
			// incorrect arguments provided
			if (splitString.length != 4) {
				throw new InvalidCommandException("Server provided incorrect arguments.");
			} 

			Player playerWhoJustHadATurn = getPlayer(splitString[1]);
			Player playerToMakeMove = getPlayer(splitString[3]);
			// opponent passed
			if (splitString[2].equals(Protocol.PASS)) {
				if (playerWhoJustHadATurn.getLastMoveWasPass()) {
					try {
						throw new AlreadyPassedException("Player already passed last time!");
					} catch (AlreadyPassedException e) {
						e.printStackTrace();
					}	
				}
				playerWhoJustHadATurn.pass(true);
			} else if (!splitString[2].equals(Protocol.FIRST)) {
				int move = getMove(splitString[2]);
				this.game.getBoard().setIntersection(move, playerWhoJustHadATurn.getState());
			} 
			if (playerToMakeMove.equals(clientPlayer)) { 
				int moveToMake = playerToMakeMove.determineMove(game.getBoard());
				this.game.getBoard().setIntersection(moveToMake, playerToMakeMove.getState());
				String move = indexToMove(moveToMake);
				send(Protocol.MOVE + Protocol.DELIMITER1 + move);
			}
		} else if (splitString[0].equals(Protocol.ENDGAME)) {
			if (splitString[1].equals(Protocol.ABORTED)) {
				System.out.println("Game was aborted!");	
			} else if (splitString[1].equals(Protocol.FINISHED)) {
				System.out.println("Game has finished!");	
			} else if (splitString[1].equals(Protocol.TIMEOUT)) {
				System.out.println("Game was aborted due to timeout!");	
			} else {
				throw new InvalidCommandException("Server provided incorrect arguments.");
			}
			if (numberOfPlayers == 2) {
				if (splitString.length != 6) {
					throw new InvalidCommandException("Server provided incorrect arguments.");
				}

				Map<Player, Integer> playersToCheck = new HashMap<Player, Integer>(); 	 
				checkScores(playersToCheck, this.game.getBoard());
				System.out.println(String.format(
						"%s won with a score of %s, %s lost with a score of %s",
						splitString[3], splitString[4], splitString[5], splitString[6]));
				endGame();
			} else {
				try {
					throw new NotYetImplementedException("Not yet implemented!");
				} catch (NotYetImplementedException e) {
					e.printStackTrace();
				}
			}

		} else {
			throw new InvalidCommandException(String.format(
					"Server provided incorrect arguments. Recieved message: %s", msg));
		}
	}

	public void checkScores(Map<Player, Integer> playersToCheck, Board board) {
		Map<Colour, Integer> scoresFromBoard = game.getBoard().getScore();
		for (Map.Entry<Player, Integer> entry : playersToCheck.entrySet()) {
			if (entry.getValue() != scoresFromBoard.get(entry.getKey().getState())) {
				try {
					throw new ScoresDoNotMatchException(String.format(
							"Scores do not match! Server provided a score of %s for player %s "
									+ "but local administration indicates a score of %s.",
									entry.getValue(), entry.getKey().getName(), 
									scoresFromBoard.get(entry.getKey().getState())));
				} catch (ScoresDoNotMatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void endGame() {
		System.out.println("Ending game");
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

	// if the player is the first to connect, the setup of the game has to be completed
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
			if (!Colour.getColour(stringArray[2]).equals(clientPlayer.getState())) {
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
			if (clientPlayer.getState().equals(Colour.BLACK)) {
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

	public void prepareGame(String[] stringArray) 
			throws NameException, NotAnIntException, NotYetImplementedException {
		int boardDimension;
		try {
			this.numberOfPlayers = Integer.parseInt(stringArray[1]);
			boardDimension = Integer.parseInt(stringArray[3]);
		} catch (NumberFormatException e) {
			throw new NotAnIntException(
					"One or more arguments that should have been integers weren't");
		}

		if (numberOfPlayers == 2) {
			Colour colour = null;
			try {
				colour = Colour.getColour(stringArray[2]);
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
				throw new NameException(String.format(
						"Players must have different names! Both names are %s", names[0])) ;
			} else if (names[0].equals(name)) {
				opponentName = names[1];
			} else if (names[1].equals(name)) {
				opponentName = names[0];
			} else {
				throw new NameException(String.format(
						"Server provided incorrect names. This client's name is %s."
						+ " Server provided %s and %s", this.name, names[0], names[1])); 
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

	// if the client is the first player, he/she should provide game settings
	public void provideGameSettings(int numberOfPlayers) 
			throws InvalidColourException, NotAnIntException {
		Colour colour = Colour.BLACK;
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

		String colourString = "";
		Colour playerColour = null;
		try {
			if (SCANNER.hasNextLine()) {
				colourString = SCANNER.nextLine(); //Should be a blocking call
				playerColour = Colour.getColour(colourString);
				this.clientPlayer = generateClientPlayer(playerColour);
				players.add(clientPlayer);
			} 
		} catch (Project.Errors.InvalidColourException e) {
			System.out.println("Deze kleur mag niet, probeer opnieuw: ");
		}
		print("Please enter a boardsize: ");
		try {
			this.boardSize = Integer.parseInt(SCANNER.nextLine());
		} catch (NumberFormatException e) {
			throw new NotAnIntException("You did not enter a valid integer!");
		}
		send(Protocol.SETTINGS + Protocol.DELIMITER1 + playerColour.toString() + 
				Protocol.DELIMITER1 + boardSize + Protocol.DELIMITER1 + Protocol.COMMAND_END);

		System.out.println("Waiting for other player...");
	}

	public void startGame() {
		this.game = new Game(players, this.boardSize,
				new GoGUIIntegrator(true, true, this.boardSize));
	}

	public void send(String msg) {
		checkEndCommand(msg.split(Protocol.DELIMITER1), msg);
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

	public static void main(String[] args) 
			throws InvalidHostException, NoValidPortException, InvalidCommandException {

		System.out.println("How do you want to play? 1 = human, 2 = computer");
		try {
			int answer = SCANNER.nextInt();
			if (answer == 1) {
				isHuman = true;
				System.out.println("Playing as human.");
			} else if (answer == 2) {
				isHuman = false;
				System.out.println("Playing as computer");
			} else {
				SCANNER.close();
				throw new InvalidCommandException("Wrong input!");
			}
		} catch (NumberFormatException e) {
			throw new InvalidCommandException("Wrong input!");
		}

		SCANNER.nextLine();

		System.out.println("Please provide name: ");
		String name = SCANNER.nextLine();

		System.out.println(String.format("Welcome %s!", name));

		int port = 0;
		InetAddress host = null;

		System.out.println("Please provide host-ip: ");
		String hostString = SCANNER.nextLine();
		try {
			host = InetAddress.getByName(hostString);
		} catch (UnknownHostException e) {
			SCANNER.close();
			throw new InvalidHostException("Invalid host");
		}

		System.out.println("Please provide port: ");
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
}
