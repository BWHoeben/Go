package classes;

public class ComputerPlayer extends Player implements Strategy {

	private Strategy strategy;
	
	//public ComputerPlayer(String name, Colour colour, Strategy strategy) {
	//	super(name, colour);
	//	this.strategy = strategy;
	//}
	
	public ComputerPlayer(String name, Colour colour) {
		super(name, colour);
		this.strategy = new SmarterStrategy();
	}
	
	public Move determineMove(ActualBoard board) {
		return determineMove(board, getColour());
	}
	
	public Move determineMove(ActualBoard board, Colour colourArg) {
		return strategy.determineMoveUsingStrategy(board, colourArg);
	}

	public Move determineMoveUsingStrategy(ActualBoard board, Colour colourArg) {
		return strategy.determineMoveUsingStrategy(board, colourArg);
	}
}
