package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import GUI.GoGUIIntegrator;
import errors.InvalidMoveException;

public class ActualBoard extends Board {

	// Instance variables
	private GoGUIIntegrator gogui;

	// Constructor
	public ActualBoard(int dimension, int numberOfPlayersArg, GoGUIIntegrator goguiArg) {
		this(dimension, numberOfPlayersArg);
		this.gogui = goguiArg;
	}

	public GoGUIIntegrator getGoGui() {
		return gogui;
	}

	public ActualBoard(int dimension, int numberOfPlayersArg) {
		super(dimension, numberOfPlayersArg);
		this.numberOfPlayers = numberOfPlayersArg;
		this.dimension = dimension;
		this.intersections = new HashMap<Integer, Intersection>();
		this.groups = new HashSet<Group>();
		// Initialize all intersections
		for (int i = 0; i < this.dimension * this.dimension; i++) {
			this.intersections.put(i, new Intersection(i, this.dimension));
		}

		score = new HashMap<Colour, Integer>();
		boardSituations = new ArrayList<Colour[]>();


		// Black is the first one to move, so white is set as lastMove by default
		lastMove = Colour.WHITE;
		score.put(lastMove, 0);
		Colour colourToCalculate = lastMove.next(this.numberOfPlayers);
		for (int i = 0; i < numberOfPlayers; i++) {
			score.put(colourToCalculate, 0);
			colourToCalculate = colourToCalculate.next(this.numberOfPlayers);
		}
		updateGroups();
	}

	// if a group is captured (it has no more liberties),
	// the stones are removed. I.e. the intersections are set to empty
	@Override
	public void setGroupToEmpty(Group group) {
		Set<Intersection> set = new HashSet<Intersection>(group.getIntersections().values());
		for (Intersection intersect : set) {
			intersect.setColour(Colour.EMPTY);
			if (this.gogui != null) {
				gogui.removeStone(intersect.getCol(), intersect.getRow());
			}
		}
	}

	public Set<Move> calculateScoreDiffs(Colour colour) {		
		int maxScore = - 10000;
		
		Set<Move> setToReturn = new HashSet<Move>();
		// get all valid moves
		Set<Integer> validMoves = this.getValidMoves(colour);
		Set<Integer> movesToEval = new HashSet<Integer>();
		for (Integer i : validMoves) {
			if (i == 119) {
				System.out.println("Hoi");
			}
			if (!isLonely(intersections.get(i), colour)) {
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
			HypotheticalBoard hypoBoard = new HypotheticalBoard(this.currentSituation(), this.dimension, this.numberOfPlayers, this.boardSituations, this.lastMove, this.score);

			// make hypotical move
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
		
		return setToReturn;
	}
}
