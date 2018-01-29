package classes;

public interface Strategy {
	public Move determineMoveUsingStrategy(Board board, Colour colour);
}
