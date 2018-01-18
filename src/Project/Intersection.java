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
	
	public int indexToRow(int index, int dimensionOfBoard) {
		return index % dimensionOfBoard;
	}
	
	public int indexToCol(int index, int dimensionOfBoard) {
		return index - (index % dimensionOfBoard);
	}
	
	public int calculateIndex(int col, int row, int dimensionOfBoard) {
		return (row * dimensionOfBoard) + col;
	}
	
	public int getDimension() {
		return this.dimensionOfBoard;
	}
}
