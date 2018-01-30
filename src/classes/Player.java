package classes;

public abstract class Player {

	private String name;
	private Colour colour;
	private boolean lastMoveWasPass;

	public Player(String name, Colour colour) {
		this.name = name;
		this.colour = colour;
	}

	public void pass(boolean pass) {
		lastMoveWasPass = pass;
	}

	public boolean getLastMoveWasPass() {
		return lastMoveWasPass;
	}

	public String getName() {
		return name;
	}

	public Colour getColour() {
		return colour;
	}
	
	public abstract Move determineMove(ActualBoard board);
}
