package classes;

import java.util.HashSet;
import java.util.Set;

public class MoveScoreCombination {
	
	private Set<Move> moves;
	private int score;
	private Move previousMove;
	
	public MoveScoreCombination(Set<Move> movesArg, int scoreArg) {
		this.moves = movesArg;
		this.score = scoreArg;
	}
	
	public int getScore() {
		return score;
	}

	public Set<Move> getMoves() {
		return moves;
	}
	
	public void setPreviousMove(Move previousMoveArg) {
		this.previousMove = previousMoveArg;
	}
	
	public Move getPreviousMove() {
		return previousMove;
	}
}
