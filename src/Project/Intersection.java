package Project;

public class Intersection {

	private int index;
	private int dimensionOfBoard;
	private Colour state;


	public Intersection(int index, int dimensionOfBoard) {
		this.index = index;
		this.dimensionOfBoard = dimensionOfBoard;
		this.state = Colour.EMPTY;
	}

	public int getRow() {
		return  index % dimensionOfBoard;
	}

	public int getCol() {
		return index - getRow();		
	}

	public Colour getState() {
		return this.state;
	}

	public void setState(Colour state) {
		this.state = state;
	}

	public int getIndex() {
		return index;
	}

	public int indexToRow(int indexArg, int dimensionOfBoardArg) {
		return indexArg % dimensionOfBoardArg;
	}

	public int indexToCol(int indexArg, int dimensionOfBoardArg) {
		return indexArg - (index % dimensionOfBoardArg);
	}

	public int calculateIndex(int col, int row, int dimensionOfBoardArg) {
		return (row * dimensionOfBoardArg) + col;
	}

	public int getDimension() {
		return this.dimensionOfBoard;
	}
}
