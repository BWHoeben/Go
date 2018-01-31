package classes;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import errors.InvalidMoveException;

public class HypotheticalBoard extends Board {

	public HypotheticalBoard(char[] state, int dimension, int numberOfPlayersArg,
			List<char[]> boardSituationsArg, Colour lastMoveArg, 
			Map<Colour, Integer> scoreArg) {
		super(dimension, numberOfPlayersArg);
		assert state.length == dimension * dimension;
		intersections = new HashMap<Integer, Intersection>();
		for (int i = 0; i < state.length; i++) {
			intersections.put(i, new Intersection(i, dimension, Colour.useChar(state[i])));
		}
		this.score = scoreArg;
		this.boardSituations = boardSituationsArg;
		this.lastMove = lastMoveArg;
	}

	@Override
	public void setIntersection(Move move) throws InvalidMoveException {
		// make a move hypothetical move, 
		// whether this move is valid has already been checked elsewhere!
		Intersection intersect = intersections.get(move.getIndex());
		intersect.setColour(move.getColour());	
		copyBoard();
		updateGroups(move.getColour());
		updateScore();
		copyBoard();
	}

	public MoveScoreCombination calculateScoreDiffs2(Colour colour) {		
		int maxScore = -10000;
		
		Set<Move> setToReturn = new HashSet<Move>();
		// get all valid moves
		Set<Integer> validMoves = this.getValidMoves(colour);
		Set<Integer> movesToEval = new HashSet<Integer>();
		for (Integer i : validMoves) {
			if (!isLonely(intersections.get(i), colour) 
					&& !isCrowded(intersections.get(i), colour)) {
				movesToEval.add(i);
			}
		}
		if (movesToEval.size() == 0) {
			movesToEval.addAll(validMoves);
		}
		
		//int currentDiff = scoreDiff(colour);
		for (Integer move : movesToEval) {
			Move moveToMake = new Move(move, this.dimension, colour);
			
			// create new hypothetical board
			HypotheticalBoard hypoBoard = new HypotheticalBoard(this.currentSituation(), 
					this.dimension, this.numberOfPlayers, 
					this.boardSituations, this.lastMove, this.score);

			// make hypothetical move
			try {
				hypoBoard.setIntersection(moveToMake);
			} catch (InvalidMoveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// calculate hypothetical score
			int newScoreDiff = hypoBoard.scoreDiff(colour);
			
			if (newScoreDiff > maxScore) {
				maxScore = newScoreDiff;
				setToReturn.clear();
				setToReturn.add(moveToMake);
			} else if (newScoreDiff == maxScore) {
				setToReturn.add(moveToMake);
			}
		}
		
		return new MoveScoreCombination(setToReturn, maxScore);
	}
}
