package classes;

public interface Strategy {
	public Move determineMoveUsingStrategy(ActualBoard board, Colour colour);
}
