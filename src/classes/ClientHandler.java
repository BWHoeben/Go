package classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import errors.NotYetImplementedException;
import errors.VersionsDoNotMatchException;

public class ClientHandler extends Thread {
	private Server server;
	private BufferedReader in;
	private BufferedWriter out;
	private String clientName;
	private int number;
	private Colour colour;

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
		System.out.println("Clienthandeler created");
	}
	
	public int getNumber() {
		return number;
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
		String msg = in.readLine();
		String[] split = msg.split(Protocol.DELIMITER1);
		if (split[0].equals(Protocol.NAME)) {
			this.clientName = split[1];
		}
		if (!split[3].equals(Protocol.VERSIONNUMBER)) {
			try {
				throw new VersionsDoNotMatchException(String.format(
						"Versions do not match! Recieved message: %s", msg));
			} catch (VersionsDoNotMatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 5; i < split.length - 1; i++) {
			if (!split[i].equals("0")) {
				try {
					throw new NotYetImplementedException("Not yet implemented");
				} catch (NotYetImplementedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
			while (true) {
				Thread.sleep(1000);
				if (msg != null) {
					server.handleMessage(msg, this);
				}
				msg = in.readLine();
				System.out.println("Message recieved");
				System.out.println(msg);
			}
		} catch (IOException | InterruptedException e) {
			shutdown();
		}
	}

	/**
	 * This method can be used to send a message over the socket
	 * connection to the Client. If the writing of a message fails,
	 * the method concludes that the socket connection has been lost
	 * and shutdown() is called.
	 */
	public void sendMessageToClient(String msg) {
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
	private void shutdown() {
		server.removeHandler(this);
		//server.broadcastToAllClients("[" + clientName + " has left]");
	}

	public String getClientName() {
		return clientName;
	}
}