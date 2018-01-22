package classes;

public interface Strategy {
	public String getName();
	public int determineMove(Board board, Colour state);
}
