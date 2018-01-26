package classes;

public class Intersection {

	private int index;
	private int dimensionOfBoard;
	private Colour colour;


	public Intersection(int index, int dimensionOfBoard) {
		this.index = index;
		this.dimensionOfBoard = dimensionOfBoard;
		this.colour = Colour.EMPTY;
	}

	public int getRow() {
		return indexToRow(index, dimensionOfBoard);
	}

	public int getCol() {
		return indexToCol(index, dimensionOfBoard);		
	}

	public Colour getColour() {
		return this.colour;
	}

	public void setColour(Colour colour) {
		this.colour = colour;
	}

	public int getIndex() {
		return index;
	}

	public int indexToRow(int indexArg, int dimensionOfBoardArg) {
		return indexArg / dimensionOfBoardArg;
	}

	public int indexToCol(int indexArg, int dimensionOfBoardArg) {
		return indexArg % dimensionOfBoardArg;
	}

	public int calculateIndex(int col, int row, int dimensionOfBoardArg) {
		return (row * dimensionOfBoardArg) + col;
	}

	public int getDimension() {
		return this.dimensionOfBoard;
	}
}
