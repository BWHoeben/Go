package classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import errors.InvalidMoveException;


public abstract class Board {

	// Instance variables
	protected int dimension;
	protected Map<Integer, Intersection> intersections;
	protected Set<Group> groups;
	protected Colour lastMove;
	protected Map<Colour, Integer> score;
	protected List<char[]> boardSituations; 
	protected int numberOfPlayers;

	public Board(int dimension, int numberOfPlayersArg) {
		this.dimension = dimension;
		this.numberOfPlayers = numberOfPlayersArg;
	}

	public int getDimension() {
		return dimension;
	}

	public int getNumberOfPlayer() {
		return this.numberOfPlayers;
	}

	public boolean gameOver() {
		if (getValidMoves(lastMove.next(this.numberOfPlayers)).size() > 0) {
			return false;
		}
		return true;
	}

	public void copyBoard() {
		// back-up the current situations
		this.boardSituations.add(currentSituation());
	}



	public char[] currentSituation() {
		// make a representation of the current situation
		char[] arrayToReturn = new char[dimension * dimension];
		for (int i = 0; i < intersections.size(); i++) {
			arrayToReturn[i] = intersections.get(i).getColour().toChar();
		}
		return arrayToReturn;
	}

	// indicates whether this situation has already occurred
	public boolean replicatesPreviousBoard(int index, Colour colour) {
		// would this move replicate a previous board situation?
		char[] currentSituation = currentSituation();
		//update with hypothetical move
		currentSituation[index] = colour.toChar();
		for (char[] array : boardSituations) {
			if (Arrays.equals(array, currentSituation)) {
				return true;
			}
		}
		return false;
	}

	public void setIntersection(Move move) throws InvalidMoveException {
		// make a move, provided the move is valid
		if (isValidMove(move)) {		
			Intersection intersect = intersections.get(move.getIndex());
			intersect.setColour(move.getColour());
			copyBoard();
			updateGroups(move.getColour());
			updateScore();	
			copyBoard();
			this.lastMove = move.getColour();	
		} else {
			throw new InvalidMoveException(String.format(
					"Invalid move! Index: %s Colour: %s",
					move.getIndex(), move.getColour().toString()));
		}
	}


	// returns the indexes of all valid moves
	public Set<Integer> getValidMoves(Colour colour) {
		Set<Integer> returnSet = new HashSet<Integer>();
		for (Map.Entry<Integer, Intersection> entry : intersections.entrySet()) {
			if (isValidMove(new Move(entry.getKey(), this.dimension, colour))) {
				returnSet.add(entry.getKey());
			}
		}
		return returnSet;
	}


	// updates the score, the score is determined by:
	// - the occupied area
	// - the enclosed area
	public void updateScore() {
		for (Entry<Colour, Integer> entry : score.entrySet()) 	{
			Colour colour = entry.getKey();
			int sum = occupiedArea(colour) + enclosedArea(colour);
			score.replace(colour, sum);
		}
	}

	public boolean isValidMove(Move move) {
		int index = move.getIndex();
		Colour colour = move.getColour();
		// a move is valid if:
		// - the coordinates are valid
		// - the intersection is empty
		// - it does not replicate a previous situation

		if (isIntersection(index) && intersections.get(index).getColour().equals(Colour.EMPTY) 
				&& !replicatesPreviousBoard(index, colour)) {
			return true;
		} 
		return false;
	}


	// count the amount of occupied intersections
	public int occupiedArea(Colour colour) {
		int sum = 0;
		Intersection intersect = null;
		for (int i = 0; i < intersections.size(); i++) {
			intersect = intersections.get(i);
			if (intersect.getColour().equals(colour)) {
				sum++;
			}
		}
		return sum;
	}

	// calculate the enclosed area
	public int enclosedArea(Colour colour) {
		int sum = 0;
		// get all groups with colour empty
		List<Group> emptyGroups = new ArrayList<Group>();
		for (Group group : groups) {
			if (group.getColour().equals(Colour.EMPTY)) {
				emptyGroups.add(group);
			}
		}

		// a group is enclosed if all neighbours have a similar colour
		for (Group group : emptyGroups) {
			Set<Intersection> adjacentIntersections = adjacentIntersectionsGroup(group);
			if (setHasHomoColour(adjacentIntersections) != null 
					&& setHasHomoColour(adjacentIntersections).equals(colour)) {
				sum = sum + group.getIntersections().size();
			}
		}
		return sum;
	}

	public Map<Colour, Integer> getScore() {
		return score;
	}

	public void resetIntersections() {
		for (int i = 0; i < intersections.size(); i++) {
			Intersection intersect = intersections.get(i);
			intersect.removedFromGroup();
		}
	}

	// update all the groups
	// groups are defined as orthogonally adjacent intersections with the same colour,
	// thus this also includes empty area's
	public void updateGroups(Colour colour) {
		resetIntersections();
		this.groups = new HashSet<Group>();	
		for (int i = 0; i < this.intersections.size(); i++) {
			Intersection intersectToEval = intersections.get(i);
			if (!intersectToEval.isInGroup()) {

				// get all adjacent intersections with same colour of intersection to evaluate
				Set<Intersection> adjacentIntersects = 
						adjecentIntersectionsIntersectWithEqualColour(intersectToEval);


				// are there more adjacent intersections with the same colour
				// that do not yet belong to adjacentIntersects?
				while (adjacentIntersectionsSetWithEqualColour(adjacentIntersects).size() > 0) {
					// add these intersections to the set adjacentIntersects
					adjacentIntersects.addAll(
							adjacentIntersectionsSetWithEqualColour(adjacentIntersects));
				}
				// do any of these intersections belong to a group?
				Group group = getGroupOfSetOfIntersections(adjacentIntersects);
				if (group != null) {
					// add the current intersection to this group
					group.addIntersection(intersectToEval);	
					// add all adjacent intersections of 
					// this group with the same colour to this group
					group.addSetOfIntersections(adjacentIntersectionsGroupWithEqualColour(group));

					// does this group have any adjacent intersections with 
					// the same colour that do net yet belong to this group?
					while (adjacentIntersectionsGroupWithEqualColour(group).size() > 0) {
						// add these intersections to the current group
						group.addSetOfIntersections(
								adjacentIntersectionsGroupWithEqualColour(group));
					}
				} else {
					// create a new group for the current intersection
					Map<Integer, Intersection> map = new HashMap<Integer, Intersection>();
					map.put(intersectToEval.getIndex(), intersectToEval);
					Group groupToAdd = new Group(map, intersectToEval.getColour());
					groupToAdd.addSetOfIntersections(adjacentIntersects);
					groups.add(groupToAdd);
					intersectToEval.addedToGroup();
				}
				// after this all intersections in adjacentIntersects should belong to a group
				for (Intersection intersect : adjacentIntersects) {
					intersect.addedToGroup();
				}
			}
		}

		resetIntersections();
		// are there any groups without liberties?
		// first check the intersection of the player who didn't commit the last move
		Colour colourToCheck = colour.next(this.numberOfPlayers);

		for (int i = 0; i < numberOfPlayers; i++) {
			for (Group group : groups) {
				if (group.getColour().equals(colourToCheck)) {
					if (!hasLiberties(group)) {
						setGroupToEmpty(group);
						updateGroups(colour);
					}
				}
			}
			colourToCheck = colourToCheck.next(this.numberOfPlayers);
		}
	}

	// Does any of the intersections in this set belong to a certain group?
	// No --> return null
	// Yes --> return that group 
	public Group getGroupOfSetOfIntersections(Set<Intersection> intersectionsArg) {
		// cycle through intersections
		for (Intersection intersect : intersectionsArg) {
			Group group = getGroupOfIntersection(intersect);
			if (group != null) {
				return group;
			}
		}

		return null;
	}

	/**
	 * Returns the group the intersection belongs to.
	 * @param intersect = intersection to evaluate
	 * @return Group of intersection, returns null if intersection does not belong to a group
	 */
	public Group getGroupOfIntersection(Intersection intersect) {
		// first cycle through list of groups
		for (Group group : groups) {
			Map<Integer, Intersection> intersectionsLocal = group.getIntersections();

			// then cycle through intersections in group
			for (Map.Entry<Integer, Intersection> entry : intersectionsLocal.entrySet()) {
				if (entry.getValue().equals(intersect)) {
					return group;
				}
			}
		}
		return null;
	}

	// indicates if a group has liberties
	public boolean hasLiberties(Group group) {
		Set<Intersection> adjacentIntersects = adjacentIntersectionsGroup(group);
		Set<Intersection> emptyIntersects = 
				intersectionsWithColour(adjacentIntersects, Colour.EMPTY);
		if (emptyIntersects.size() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isSuicide(Move move) {
		Intersection intersect = intersections.get(move.getIndex());
		Set<Intersection> adjacentIntersects = adjacentIntersectionsIntersect(intersect);
		Set<Intersection> emptyIntersects = 
				intersectionsWithColour(adjacentIntersects, Colour.EMPTY);
		Set<Intersection> ownIntersects = 
				intersectionsWithColour(adjacentIntersects, move.getColour());
		
		
		if (emptyIntersects.size() + ownIntersects.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	// if a group is captured (it has no more liberties),
	// the stones are removed. I.e. the intersections are set to empty
	public void setGroupToEmpty(Group group) {
		Set<Intersection> set = new HashSet<Intersection>(group.getIntersections().values());
		for (Intersection intersect : set) {
			intersect.setColour(Colour.EMPTY);
		}
	}

	// return a set of intersections with a predefined colour
	public Set<Intersection> intersectionsWithColour(
			Set<Intersection> intersectionsArg, Colour colour) {
		Set<Intersection> setToReturn = new HashSet<Intersection>();
		for (Intersection intersect : intersectionsArg) {
			if (intersect.getColour().equals(colour)) {
				setToReturn.add(intersect);
			}
		}
		return setToReturn;
	}

	// indicates whether the coordinates for an intersection are valid
	public boolean isIntersection(int row, int col) {
		return row >= 0 && row < this.dimension && col >= 0 && col < this.dimension;
	}

	// indicates whether the coordinates for an intersection are valid
	public boolean isIntersection(int index) {
		return index >= 0 && index < this.dimension * this.dimension;
	}

	// indicates whether the set is homogeneous. I.e. all the intersections have the same colour
	public Colour setHasHomoColour(Set<Intersection> intersectionsArg) {
		int i = 0;
		Colour colour = null;
		for (Intersection intersect : intersectionsArg) {
			if (i > 0) {
				if (!intersect.getColour().equals(colour)) {
					return null;
				}
			}
			colour = intersect.getColour();
			i++;
		}

		return colour;
	}

	// all adjacent intersection of a set of intersections
	public Set<Intersection> adjacentIntersectionsSetWithEqualColour(
			Set<Intersection> intersectionsArg) {
		Set<Intersection> setToReturn = new HashSet<Intersection>();
		for (Intersection intersect : intersectionsArg) {
			Set<Intersection> adjacentForThisIntersect = 
					adjecentIntersectionsIntersectWithEqualColour(intersect);
			setToReturn.addAll(adjacentForThisIntersect);
		}

		// remove intersections that were already in the original set
		for (Intersection intersect : intersectionsArg) {
			if (setToReturn.contains(intersect)) {
				setToReturn.remove(intersect);
			}
		}

		return setToReturn;
	}

	// all adjacent intersections of a group
	public Set<Intersection> adjacentIntersectionsGroup(Group group) {
		Set<Intersection> adjacentIntersectionsGroup = new HashSet<Intersection>();
		for (Entry<Integer, Intersection> entry : group.getIntersections().entrySet()) {
			Intersection intersect = entry.getValue();
			Set<Intersection>  adjacentIntersectionsIntersection
				= adjacentIntersectionsIntersect(intersect);
			adjacentIntersectionsGroup.addAll(adjacentIntersectionsIntersection);
		}

		Set<Intersection> intersectionsToRemove = new HashSet<Intersection>();

		// remove intersections that are already in the group
		for (Intersection intersect : adjacentIntersectionsGroup) {
			if (group.containsIntersection(intersect)) {
				intersectionsToRemove.add(intersect);
			}
		}

		adjacentIntersectionsGroup.removeAll(intersectionsToRemove);

		return adjacentIntersectionsGroup;
	}

	// all adjacent intersections of a group with a colour similar to the group
	public Set<Intersection> adjacentIntersectionsGroupWithEqualColour(Group group) {
		Colour colour = group.getColour();
		Set<Intersection> adjacentIntersectionsGroup = adjacentIntersectionsGroup(group);
		Set<Intersection> intersectsToRemove = new HashSet<Intersection>();
		for (Intersection intersect : adjacentIntersectionsGroup) {
			if (!intersect.getColour().equals(colour)) {
				intersectsToRemove.add(intersect);
			}
		}
		adjacentIntersectionsGroup.removeAll(intersectsToRemove);
		return adjacentIntersectionsGroup;
	}

	// all adjacent intersections of a intersection
	public Set<Intersection> adjacentIntersectionsIntersect(Intersection intersect) {
		int dimensionLocal = intersect.getDimension();
		int col = intersect.getCol();
		int row = intersect.getRow();

		Set<Intersection> adjacentIntersections = new HashSet<Intersection>();

		if (isIntersection(row + 1, col)) {
			adjacentIntersections.add(intersections.get(
					intersect.calculateIndex(col, row + 1, dimensionLocal)));
		}

		if (isIntersection(row - 1, col)) {
			adjacentIntersections.add(intersections.get(
					intersect.calculateIndex(col, row - 1, dimensionLocal)));
		}

		if (isIntersection(row, col + 1)) {
			adjacentIntersections.add(intersections.get(
					intersect.calculateIndex(col + 1, row, dimensionLocal)));
		}

		if (isIntersection(row, col - 1)) {
			adjacentIntersections.add(intersections.get(
					intersect.calculateIndex(col - 1, row, dimensionLocal)));
		}

		return adjacentIntersections;
	}

	// all adjacent intersections of a intersection
	public Set<Intersection> adjacentIntersectionsIntersectDiag(
			Intersection intersect, Colour colour) {
		int dimensionLocal = intersect.getDimension();
		int col = intersect.getCol();
		int row = intersect.getRow();
		int index;
		Colour colourOfIntersect;

		Set<Intersection> adjacentIntersections = new HashSet<Intersection>();

		if (isIntersection(row + 1, col)) {
			adjacentIntersections.add(intersections.get(
					intersect.calculateIndex(col, row + 1, dimensionLocal)));
		}

		// diagonal
		if (isIntersection(row + 1, col + 1)) {
			index = 	intersect.calculateIndex(col + 1, row + 1, dimensionLocal);
			colourOfIntersect = intersections.get(index).getColour();
			if (colourOfIntersect.equals(colour)) {
				adjacentIntersections.add(intersections.get(
						intersect.calculateIndex(col + 1, row + 1, dimensionLocal)));
			}
		}

		// diagonal
		if (isIntersection(row + 1, col - 1)) {
			index = 	intersect.calculateIndex(col - 1, row + 1, dimensionLocal);
			colourOfIntersect = intersections.get(index).getColour();
			if (colourOfIntersect.equals(colour)) {
				adjacentIntersections.add(intersections.get(
						intersect.calculateIndex(col - 1, row + 1, dimensionLocal)));
			}
		}

		if (isIntersection(row - 1, col)) {
			adjacentIntersections.add(intersections.get(
					intersect.calculateIndex(col, row - 1, dimensionLocal)));
		}

		// diagonal
		if (isIntersection(row - 1, col - 1)) {
			index = 	intersect.calculateIndex(col - 1, row - 1, dimensionLocal);
			colourOfIntersect = intersections.get(index).getColour();
			if (colourOfIntersect.equals(colour)) {
				adjacentIntersections.add(intersections.get(
						intersect.calculateIndex(col - 1, row - 1, dimensionLocal)));
			} 
		}

		// diagonal
		if (isIntersection(row - 1, col + 1)) {
			index = 	intersect.calculateIndex(col + 1, row - 1, dimensionLocal);
			colourOfIntersect = intersections.get(index).getColour();
			if (colourOfIntersect.equals(colour)) {
				adjacentIntersections.add(intersections.get(
						intersect.calculateIndex(col + 1, row - 1, dimensionLocal)));
			} 
		}

		if (isIntersection(row, col + 1)) {
			adjacentIntersections.add(intersections.get(
					intersect.calculateIndex(col + 1, row, dimensionLocal)));
		}

		if (isIntersection(row, col - 1)) {
			adjacentIntersections.add(intersections.get(
					intersect.calculateIndex(col - 1, row, dimensionLocal)));
		}

		return adjacentIntersections;
	}

	// all adjacent intersections of a intersection
	// with a colour similar to the provided intersection
	public Set<Intersection> adjecentIntersectionsIntersectWithEqualColour(Intersection intersect) {
		Colour colour = intersect.getColour();
		Set<Intersection> intersectionsLocal = adjacentIntersectionsIntersect(intersect);

		Set<Intersection> intersectsToReturn = new HashSet<Intersection>();
		for (Intersection intersectToCheck : intersectionsLocal) {
			if (intersectToCheck.getColour().equals(colour)) {
				intersectsToReturn.add(intersectToCheck);
			}
		}
		return intersectsToReturn;
	}

	public int scoreDiff(Colour colour) {
		int myCurrentScore = score.get(colour);
		int myOpponentsScore = 0;
		Colour colourToEvaluate = colour.next(numberOfPlayers);
		for (int i = 0; i < numberOfPlayers; i++) {
			if (!colour.equals(colourToEvaluate)) {			
				myOpponentsScore = myOpponentsScore + score.get(colourToEvaluate); 
			}
			colourToEvaluate = colourToEvaluate.next(numberOfPlayers);
		}
		return myCurrentScore - myOpponentsScore;
	}

	public boolean isLonely(Intersection intersect, Colour colour) {
		Set<Intersection> adjacentIntersects = 
				adjacentIntersectionsIntersectDiag(intersect, colour);
		for (Intersection intersectToEval : adjacentIntersects) {
			if (!intersectToEval.getColour().equals(Colour.EMPTY)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isCrowded(Intersection intersect, Colour colour) {
		Set<Intersection> adjacentIntersects = 
				adjacentIntersectionsIntersect(intersect);
		for (Intersection intersectToEval : adjacentIntersects) {
			if (!intersectToEval.getColour().equals(colour)) {
				return false;
			}
		}
		return true;
	}
	
	public MoveScoreCombination calculateScoreDiffs1(Colour colour) {		
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
			
			ArrayList<char[]> bS = new ArrayList<char[]>(this.boardSituations);
			
			// create new hypothetical board
			HypotheticalBoard hypoBoard = new HypotheticalBoard(this.currentSituation(), 
					this.dimension, this.numberOfPlayers, 
					bS, this.lastMove, this.score);

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
