package classes;

public class ComputerPlayer extends Player implements Strategy {

	private static String name;
	private Colour colour;
	private Strategy strategy;
	
	public ComputerPlayer(Colour colour, Strategy strategy) {
		super(name, colour);
		this.colour = colour;
		this.strategy = strategy;
		ComputerPlayer.name = strategy.getName() + colour.toString();
	}
	
	public ComputerPlayer(Colour colour) {
		super(name, colour);
		this.strategy = new RandomStrategy();
		ComputerPlayer.name = strategy.getName() + colour.toString();
		this.colour = colour;	
	}
	
	@Override
	public Move determineMove(Board board) {
		return determineMove(board, this.colour);
	}
	
	public Move determineMove(Board board, Colour colourArg) {
		int index = strategy.determineMoveUsingStrategy(board, colourArg);
		return new Move(index, board.getDimension(), colourArg);
	}

	@Override
	public int determineMoveUsingStrategy(Board board, Colour state) {
		// TODO Auto-generated method stub
		return 0;
	}
}
