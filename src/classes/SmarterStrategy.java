package classes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import errors.InvalidMoveException;

public class SmarterStrategy implements Strategy {

	@Override
	public Move determineMoveUsingStrategy(ActualBoard board, Colour colour) {
		long StartTime = System.currentTimeMillis();

		// get all the moves that yield the best leap in scores
		MoveScoreCombination msc = board.calculateScoreDiffs(colour);
		Set<Move> moves = msc.getMoves();
		if (moves.size() == 1) {
			System.out.println("Determining move took: " + (System.currentTimeMillis() - StartTime));
			return moves.toArray(new Move[1])[0];
		} else {
			moves = getTheBestOfTheBest(moves, board, colour);
		}
		
		int size = moves.size(); 
		int item = new Random().nextInt(size); 
		int i = 0;
		for (Move obj : moves) {
			if (i == item) {
				System.out.println("Determining move took: " + (System.currentTimeMillis() - StartTime));
				return obj;
			}
			i++;
		}

		return null;
	}
	
	public Set<Move> getTheBestOfTheBest(Set<Move> moves, ActualBoard board, Colour colour) {
		Set<HypotheticalBoard> boards = new HashSet<HypotheticalBoard>();
		Set<MoveScoreCombination> mscs = new HashSet<MoveScoreCombination>();

		for (Move move : moves) {
			HypotheticalBoard hypoBoard = new HypotheticalBoard(board.currentSituation(), board.getDimension(), board.numberOfPlayers, board.boardSituations, board.lastMove, board.score);
			try {
				hypoBoard.setIntersection(move);
			} catch (InvalidMoveException e) {
				e.printStackTrace();
			}
			MoveScoreCombination msc = hypoBoard.calculateScoreDiffs(colour);
			msc.setPreviousMove(move);
			mscs.add(msc);
		}
//		System.out.println("Measurement 1: " + (StartTime - System.currentTimeMillis()));
		int maxScore = - 1000;
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
