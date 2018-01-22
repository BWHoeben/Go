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
	public int determineMove(Board board) {
		return determineMove(board, this.colour);
	}
	
	@Override
	public int determineMove(Board board, Colour colourArg) {
		return strategy.determineMove(board, colourArg);
	}
}
