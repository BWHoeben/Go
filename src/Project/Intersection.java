package Project;

public class Intersection {

	private int index;
	private int dimensionOfBoard;
	private State state;
	
	
	public Intersection(int index, int dimensionOfBoard) {
		this.index = index;
		this.dimensionOfBoard = dimensionOfBoard;
	}
	
	public int getRow() {
		return  index % dimensionOfBoard;
	}
	
	public int getCol() {
		return index - getRow();		
	}
	
	public State getState() {
		return this.state;
	}
	
	public void setState(State state) {
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
}
