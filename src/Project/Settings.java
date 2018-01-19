package Project;

public class Settings {

	private int boardSize;
	private Colour colour;
	
	public Settings(int boardSize, Colour colour) {
		this.boardSize = boardSize;
		this.colour = colour;
	}
	
	public Colour getColour() {
		return this.colour;
	}
	
	public int getBoardSize() {
		return boardSize;
	}
}
