package Project;

public class ComputerPlayer extends Player implements Strategy {

	private static String name;
	private Colour state;
	private Strategy strategy;
	
	public ComputerPlayer(Colour state, Strategy strategy) {
		super(name, state);
		this.state = state;
		this.strategy = strategy;
		ComputerPlayer.name = strategy.getName() + state.toString();
	}
	
	public ComputerPlayer(Colour state) {
		super(name, state);
		this.strategy = new RandomStrategy();
		ComputerPlayer.name = strategy.getName() + state.toString();
		this.state = state;	
	}
	
	@Override
	public int determineMove(Board board) {
		return determineMove(board, this.state);
	}
	
	@Override
	public int determineMove(Board board, Colour state) {
		return strategy.determineMove(board, state);
	}
}
