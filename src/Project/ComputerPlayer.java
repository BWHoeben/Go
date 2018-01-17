package Project;

public class ComputerPlayer extends Player implements Strategy {

	private static String name;
	private State state;
	private Strategy strategy;
	
	public ComputerPlayer(State state, Strategy strategy) {
		super(name, state);
		this.state = state;
		this.strategy = strategy;
		ComputerPlayer.name = strategy.getName() + state.toString();
	}
	
	public ComputerPlayer(State state) {
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
	public int determineMove(Board board, State state) {
		return strategy.determineMove(board, state);
	}
}
