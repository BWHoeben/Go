package classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Timer;

import errors.NotYetImplementedException;

public class ClientHandler extends Thread {
	private Server server;
	private BufferedReader in;
	private BufferedWriter out;
	private String clientName;
	private int number;
	private Colour colour;
	private HashSet<Timer> timers = new HashSet<Timer>();
	private boolean run = true;
	private boolean lastMoveWasPass = false;
	private int movesPerformed;
	private boolean turn = false;

	/**
	 * Constructs a ClientHandler object
	 * Initialises both Data streams.
	 *@ requires server != null && sock != null;
	 */
	public ClientHandler(Server serverArg, Socket sockArg, int number) throws IOException {
		this.server = serverArg;
		this.number = number;
		in = new BufferedReader(new InputStreamReader(sockArg.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(sockArg.getOutputStream()));
		this.movesPerformed = 0;
	}

	public boolean isTurn() {
		return turn;
	}

	public void tookTurn() {
		turn = false;
	}

	public void getsTurn() {
		turn = true;
	}

	public int getNumber() {
		return number;
	}

	public void pass(boolean passed) {
		this.lastMoveWasPass = passed;
	}

	public boolean passedOnPreviousTurn() {
		return lastMoveWasPass;
	}

	public void setColour(Colour colourArg) {
		this.colour = colourArg;
	}

	public Colour getColour() {
		return this.colour;
	}

	/**
	 * Reads the name of a Client from the input stream and sends 
	 * a broadcast message to the Server to signal that the Client
	 * is participating in the chat. Notice that this method should
	 * be called immediately after the ClientHandler has been constructed.
	 */
	public void announce() throws IOException {
		//NAME serverpiet VERSION 2 EXTENSIONS 0 0 1 1 0 0 0
		sendMessageToClient(Protocol.NAME + Protocol.DELIMITER1 + "CelineDionneLover12" 
				+ Protocol.DELIMITER1 
				+ Protocol.VERSION + 
				Protocol.DELIMITER1 + Protocol.VERSIONNUMBER +
				Protocol.DELIMITER1 + Protocol.EXTENSIONS 
				+ Protocol.DELIMITER1 + 0 
				+ Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 
				+ Protocol.DELIMITER1 + 0 + Protocol.DELIMITER1 + 0 +
				Protocol.DELIMITER1 + 0 + Protocol.COMMAND_END);
		String msg = in.readLine();

		if (msg == null) {
			shutdown();
		} else {
			String[] split = msg.split("\\" + Protocol.DELIMITER1);
			if (split[0].equals(Protocol.NAME)) {
				this.clientName = split[1];
				if (server.checkForSameName(this.clientName)) {
					Server.print("Name already exists. Terminating connection.");
					sendMessageToClient(Protocol.ERROR + Protocol.DELIMITER1 + 
							Protocol.NAMETAKEN + Protocol.COMMAND_END);
					shutdown();
				}
			}
			if (!split[3].equals(Protocol.VERSIONNUMBER)) {
				sendMessageToClient(Protocol.ERROR + Protocol.DELIMITER1 
						+ Protocol.INCOMPATIBLEPROTOCOL + Protocol.COMMAND_END);
			}
		}
	}

	/**
	 * This method takes care of sending messages from the Client.
	 * Every message that is received, is preprended with the name
	 * of the Client, and the new message is offered to the Server
	 * for broadcasting. If an IOException is thrown while reading
	 * the message, the method concludes that the socket connection is
	 * broken and shutdown() will be called.
	 */
	public void run() {
		try {
			String msg = in.readLine();
			while (msg != null && run) {
				server.handleMessage(msg, this);
				msg = in.readLine();
				cancelTimers();
				if (msg != null) {
					System.out.println("Message recieved");
					System.out.println(msg);
				} else {
					shutdown();
				}
			}
		} catch (IOException e) {
			shutdown();
		}
	}

	public void cancelTimers() {
		for (Timer timer : timers) {
			timer.cancel();
		}
		timers.removeAll(timers);
	}

	/**
	 * This method can be used to send a message over the socket
	 * connection to the Client. If the writing of a message fails,
	 * the method concludes that the socket connection has been lost
	 * and shutdown() is called.
	 */
	public void sendMessageToClient(String msg) {
		String[] array = msg.split("\\" + Protocol.DELIMITER1);
		if (array.length > 1 && 
				(array[0].equals(Protocol.START) || array[0].equals(Protocol.ENDGAME) ||
						(array[0].equals(Protocol.TURN) && array[1].equals(clientName)))) {
			ClientHandler handlerToSend = this;

			Timer timer = new Timer();
			timers.add(timer);
			timer.schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					System.out.println(String.format("%s timed out", clientName));
					server.handleMessage(Protocol.TIMEOUT, handlerToSend);
					cancelTimers();
					shutdown();
				}
			}, 
					Protocol.TIMEOUTSECONDS * 1000 
					);

		}
		try {
			System.out.println(String.format("Sending message to %s: %s", clientName, msg)); 
			out.write(msg);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			shutdown();
		}
	}

	/**
	 * This ClientHandler signs off from the Server and subsequently
	 * sends a last broadcast to the Server to inform that the Client
	 * is no longer participating in the chat.
	 */
	public void shutdown() {
		if (run) {
			server.endGame(this, Protocol.ABORTED);
			Server.print("[" + clientName + " disconnected]");
			server.removeHandler(this);
			this.run = false;
		}
	}

	public String getClientName() {
		return clientName;
	}

	public int movesPerformed() {
		return movesPerformed;
	}

	public void incrementNumberOfMoves() {
		movesPerformed++;
	}

	public void resetMoves() {
		this.movesPerformed = 0;
	}
}