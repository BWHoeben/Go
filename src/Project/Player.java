package Project;

import Project.Errors.AlreadyInGameException;

public abstract class Player {

	private String name;
	private Colour colour;
	private boolean isInGame;
	private boolean lastMoveWasPass;

	public Player(String name, Colour colour) {
		this.name = name;
		this.colour = colour;
		this.isInGame = false;
	}

	public void pass(boolean pass) {
		lastMoveWasPass = pass;
	}

	public boolean getLastMoveWasPass() {
		return lastMoveWasPass;
	}

	public boolean isInGame() {
		return isInGame();
	}

	public void enterGame() throws AlreadyInGameException {
		if (!isInGame) {
			this.isInGame = true; 
		} else {
			throw new AlreadyInGameException("Player is already in a game!");
		}
	}

	public void leaveGame() {

	}

	public String getName() {
		return name;
	}

	public Colour getState() {
		return colour;
	}

	public abstract int determineMove(Board board);

	//public void makeMove (Board board) {
	//	int move = determineMove(board);
	//	board.setIntersection(move, getState());
	//}
}
