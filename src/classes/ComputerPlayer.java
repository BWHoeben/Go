package classes;

public class ComputerPlayer extends Player implements Strategy {

	private Strategy strategy;
	
	public ComputerPlayer(String name, Colour colour, Strategy strategy) {
		super(name, colour);
		this.strategy = strategy;
	}
	
	public ComputerPlayer(String name, Colour colour) {
		super(name, colour);
		this.strategy = new RandomStrategy();
	}
	
	@Override
	public Move determineMove(Board board) {
		return determineMove(board, getColour());
	}
	
	public Move determineMove(Board board, Colour colourArg) {
		return strategy.determineMoveUsingStrategy(board, colourArg);
	}

	@Override
	public Move determineMoveUsingStrategy(Board board, Colour colourArg) {
		return strategy.determineMoveUsingStrategy(board, colourArg);
	}
}
