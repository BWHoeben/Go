package classes;

public interface Strategy {
	public String getName();
	public Move determineMoveUsingStrategy(Board board, Colour state);
}
