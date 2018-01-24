package classes;

public interface Strategy {
	public String getName();
	public int determineMoveUsingStrategy(Board board, Colour state);
}
