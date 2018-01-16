package Project;

public abstract class Player {

	private String name;
	private int score;
	private State colour;
	
	public Player(String name, State colour) {
		this.name = name;
		this.colour = colour;
	}
	
	public String getName() {
		return name;
	}
	
	public State getCoulour() {
		return colour;
	}
	
	public abstract int determineMove();
	
	public void makeMove (Board board) {
		int move = determineMove();
		board.setIntersection(move, getCoulour());
	}
}
