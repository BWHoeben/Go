package classes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import errors.InvalidMoveException;

public class SmarterStrategy implements Strategy {

	private boolean firstMove = true;
	
	@Override
	public Move determineMoveUsingStrategy(ActualBoard board, Colour colour) {
		
		if (firstMove) {
			firstMove = false;
			if (colour.equals(Colour.BLACK)) {
				return new Move(1, board.dimension, colour);
			}
		}
		
		// get all the moves that yield the best leap in scores
		MoveScoreCombination msc = board.calculateScoreDiffs1(colour);
		
		//System.out.println("Colour: " + colour.toString());
		Set<Move> moves = msc.getMoves();
		
		//for (Move move : moves) {
		//	//System.out.println("moves to eval: " + move.toString());
		//}
		
		if (moves.size() == 1) {
			return moves.toArray(new Move[1])[0];
		} else {
			moves = getTheBestOfTheBest(moves, board, colour);
		}
		
		int size = moves.size(); 
		int item = new Random().nextInt(size); 
		int i = 0;
		for (Move obj : moves) {
			if (i == item) {
				//if (board.isSuicide(obj)) {
				//	return new Move(Protocol.PASS);
				//} else {
					return obj;
				//}
			}
			i++;
		}


		return null;
	}
	
	public Set<Move> getTheBestOfTheBest(Set<Move> moves, ActualBoard board, Colour colour) {
		Set<MoveScoreCombination> mscs = new HashSet<MoveScoreCombination>();
		for (Move move : moves) {
			ArrayList<char[]> bS = new ArrayList<char[]>(board.boardSituations);
			HypotheticalBoard hypoBoard = new 
					HypotheticalBoard(board.currentSituation(), board.getDimension(), 
							board.numberOfPlayers, bS, 
							board.lastMove, board.score);
			try {
				hypoBoard.setIntersection(move);
			} catch (InvalidMoveException e) {
				e.printStackTrace();
			}
			MoveScoreCombination msc = hypoBoard.calculateScoreDiffs2(colour);
			msc.setPreviousMove(move);
			mscs.add(msc);
		}
		int maxScore = -1000;
		HashSet<Move> setToReturn = new HashSet<Move>();
		for (MoveScoreCombination msc : mscs) {
			if (msc.getScore() > maxScore) {
				maxScore = msc.getScore();
				setToReturn.clear();
				setToReturn.add(msc.getPreviousMove());
			} else if (msc.getScore() == maxScore) {
				setToReturn.add(msc.getPreviousMove());
			}
		}
		return setToReturn;
	}
}
