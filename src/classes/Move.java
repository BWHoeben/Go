package classes;

import errors.InvalidCoordinateException;

public class Move {

	private String move;
	private int boardSize;
	private Colour colour;
	private int index;
	private int col;
	private int row;
	private boolean hasPassed = false;
	private boolean hasQuit = false;

	public Move(String moveArg, int boardSizeArg, Colour colourArg) 
			throws InvalidCoordinateException {
		this.move = moveArg;
		this.index = moveToIndex();
		new Move(this.index, boardSizeArg, colourArg);
	}

	public Move(int indexArg, int boardSizeArg, Colour colourArg) {
		this.index = indexArg;
		this.boardSize = boardSizeArg;
		this.colour = colourArg;
		this.row = this.index % this.boardSize;
		this.col = this.index - this.row;
		this.move = row + Protocol.DELIMITER2 + col;
	}
	
	public Move(String passOrQuit) {
		if (passOrQuit.equals(Protocol.PASS)) {
			hasPassed = true;
		} else if (passOrQuit.equals(Protocol.QUIT)) {
			hasQuit = true;
		}
	}
	
	public boolean getPass() {
		return hasPassed;
	}
	
	public boolean getQuit() {
		return hasQuit;
	}

	public int getIndex() {
		return index;
	}

	public String toString() {
		return move;
	}
		
	public int moveToIndex() throws InvalidCoordinateException {
		String[] moveArray = this.move.split(Protocol.DELIMITER2);
		if (moveArray.length != 2) {
			throw new InvalidCoordinateException("Provided coordinates were not valid!");
		}
		this.row = Integer.parseInt(moveArray[0]);
		this.col = Integer.parseInt(moveArray[1]);	
		return this.row * this.boardSize + this.col;
	}
	
	public int getCol() {
		return this.col;
	}

	public int getRow() {
		return this.row;
	}
	
	public Colour getColour() {
		return this.colour;
	}
}
