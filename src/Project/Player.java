package Project;

import Project.Errors.AlreadyInGameException;

public abstract class Player {

	private String name;
	private int score;
	private State colour;
	private boolean isInGame;
	
	public Player(String name, State colour) {
		this.name = name;
		this.colour = colour;
		this.isInGame = false;
	}
	
	public boolean isInGame() {
		return isInGame();
	}
	
	public void EnterGame() throws AlreadyInGameException {
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
	
	public State getState() {
		return colour;
	}
	
	public abstract int determineMove(Board board);
	
	public void makeMove (Board board) {
		int move = determineMove(board);
		board.setIntersection(move, getState());
	}
}
