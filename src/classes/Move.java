package classes;

public class Move {

	private String move;
	private int boardSize;
	private Colour colour;
	private int index;
	private int col;
	private int row;

	public Move(String moveArg, int boardSizeArg, Colour colourArg) {
		this.move = moveArg;
		this.index = moveToIndex(moveArg);
		new Move(this.index, boardSizeArg, colourArg);
	}

	public Move(int indexArg, int boardSizeArg, Colour colourArg) {
		this.index = indexArg;
		this.boardSize = boardSizeArg;
		this.colour = colourArg;
		this.row = this.index % this.boardSize;
		this.col = this.index - this.row;
	}

	public int getIndex() {
		return index;
	}

	public String getMoveAsString() {
		return move;
	}
		
	public int moveToIndex(String moveArg) {
		String[] moveArray = moveArg.split(Protocol.DELIMITER2);
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
