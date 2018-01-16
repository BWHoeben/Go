package Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Board {

	// Instance variables
	private final int dimension;
	private Map<Integer, Intersection> intersections;
	private List<Group> groups;
	private State lastMove;
	private Map<State, Integer> score;
	private List<Map<Integer, State>> boardSituations; 
	
	// Constructor
	public Board(int dimension) {
		this.dimension = dimension;
		
		// Initialize all intersections
		for (int i = 0; i < this.dimension * this.dimension; i++) {
			this.intersections.put(i, new Intersection(i, this.dimension));
		}
		
		score = new HashMap<State, Integer>();
		
		// Black is the first one to move, so white is set als lastMove by default
		lastMove = State.WHITE;
		score.put(lastMove, 0);
		State stateToCalculate = lastMove.next();
		while (!stateToCalculate.equals(lastMove)) {
			score.put(stateToCalculate, 0);
			stateToCalculate = stateToCalculate.next();
		}
		updateGroups();
	}

	public int getDimension() {
		return dimension;
	}

	public boolean setIntersection(int index, State state) {
		// make a move, provided the move is valid
		if (isValidMove(index, state)) {
		Intersection intersect = intersections.get(index);
		intersect.setState(state);
		this.lastMove = state;		
		updateGroups();
		updateScore();
		copyBoard();
		return true;
		} 
		return false;
	}
	
	public boolean isValidMove(int index, State state) {
		// a move is valid if:
		// - the coordinates are valid
		// - the intersection is empty
		// - it does not replicate a previous situation
		if (isIntersection(index) && intersections.get(index).getState().equals(State.EMPTY) && !replicatesPreviousBoard(index, state)) {
			return true;
		} 
		return false;
	}
	
	public void copyBoard() {
		// back-up the current situations
		boardSituations.add(currentSituation());
	}
	
	public Map<Integer, State> currentSituation() {
		// make a representation of the current situation
		Map<Integer, State> currentSituation = new HashMap<Integer, State>();
		for (int i = 0; i < intersections.size(); i++) {
			currentSituation.put(i, intersections.get(i).getState());
		}
		return currentSituation;
	}
	
	// indicates whether this situation has already occurred
	public boolean replicatesPreviousBoard(int index, State state) {
		// would this move replicate a previous board situation?
		Map<Integer, State> currentSituation = currentSituation();
		//update with hypothetical move
		currentSituation.replace(index, state);
		for (Map<Integer, State> previousSituation : boardSituations) {
			if (previousSituation.equals(currentSituation)) {
				return true;
			}
		}
		return false;
	}
	
	// updates the score, the score is determined by:
	// - the occupied area
	// - the enclosed area
	public void updateScore() {
		for (Entry<State, Integer> entry : score.entrySet()) 	{
			State state = entry.getKey();
			int sum = occupiedArea(state) + enclosedArea(state);
			score.replace(state, sum);
		}
	}
	
	// count the amount of occupied intersections
	public int occupiedArea(State state) {
		int sum = 0;
		Intersection intersect = null;
		for (int i = 0; i < intersections.size(); i++) {
			intersect = intersections.get(i);
			if (intersect.getState().equals(state)) {
				sum++;
			}
		}
		return sum;
	}
	
	// calculate the enclosed area
	public int enclosedArea(State state) {
		int sum = 0;
		// get all groups with state empty
		List<Group> emptyGroups = new ArrayList<Group>();
		for (Group group : groups) {
			if (group.getState().equals(State.EMPTY)) {
				emptyGroups.add(group);
			}
		}
		
		// a group is enclosed if all neighbours have a similar state
		for (Group group : emptyGroups) {
		Set<Intersection> adjacentIntersections = adjacentIntersectionsGroup(group);
		if (setHasHomoState(adjacentIntersections)) {
			sum = sum + group.getIntersections().size();
		}
		}
		return sum;
	}
	
	public Map<State, Integer> getScore() {
		return score;
	}

	// update all the groups
	// groups are defined as orthogonally adjacent intersections with the same state, thus this also includes empty area's
	public void updateGroups() {
		for (int i = 0; i < intersections.size(); i++) {
			Intersection intersectToEval = intersections.get(i);
			
			// get all adjacent intersections with same state of intersection to evaluate
			Set<Intersection> adjacentIntersects = adjecentIntersectionsIntersectWithEqualState(intersectToEval);
			
			// are there more adjacent intersections with the same state that do not yet belong to adjacentIntersects?
			while (adjacentIntersectionsSetWithEqualState(adjacentIntersects).size() > 0) {
				// add these intersections to the set adjacentIntersects
				adjacentIntersects.addAll(adjacentIntersectionsSetWithEqualState(adjacentIntersects));
			}
			
			// do any of these intersections belong to a group?
			Group group = getGroupOfSetOfIntersections(adjacentIntersects);
			if (group != null) {
				// add the current intersection to this group
				group.addIntersection(intersectToEval);	
				// add all adjacent intersections of this group with the same state to this group
				group.addSetOfIntersections(adjacentIntersectionsGroupWithEqualState(group));

				// does this group have any adjacent intersections with the same state that do net yet belong to this group?
				while (adjacentIntersectionsGroupWithEqualState(group).size() > 0) {
					// add these intersections to the current group
					group.addSetOfIntersections(adjacentIntersectionsGroupWithEqualState(group));
				}
			} else {
				// create a new group for the current intersection
				Map<Integer, Intersection> map = new HashMap<Integer, Intersection>();
				map.put(intersectToEval.getIndex(), intersectToEval);
				Group groupToAdd = new Group(map, intersectToEval.getState());
				groupToAdd.addSetOfIntersections(adjacentIntersects);
				groups.add(groupToAdd);
			}
		}
		
		// are there any groups without liberties?
		// first check the intersection of the player who didn't commit the last move
		State stateToCheck = lastMove.next();
		while (!stateToCheck.equals(lastMove)) {
		for (Group group : groups) {
			if (group.getState().equals(stateToCheck)) {
				if(!hasLiberties(group)) {
					setGroupToEmpty(group);
					updateGroups();
				}
			}
		}
		stateToCheck = lastMove.next();
		}
	}

	// Does any of the intersections in this set belong to a certain group?
	// No --> return null
	// Yes --> return that group 
	public Group getGroupOfSetOfIntersections(Set<Intersection> intersections) {
		// cycle through intersections
		for (Intersection intersect : intersections) {
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
			Map<Integer, Intersection> intersections = group.getIntersections();

			// then cycle through intersections in group
			for (Map.Entry<Integer, Intersection> entry : intersections.entrySet()) {
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
		Set<Intersection> emptyIntersects = intersectionsWithState(adjacentIntersects, State.EMPTY);
		if (emptyIntersects.size() == 0) {
		return false;
		} else {
			return true;
		}
	}
	
	// if a group is captured (it has no more liberties), the stones are removed. I.e. the intersections are set to empty
	public void setGroupToEmpty(Group group) {
		Set<Intersection> set = (Set<Intersection>) group.getIntersections().values();
		for (Intersection intersect : set) {
			intersect.setState(State.EMPTY);
		}
	}

	// return a set of intersections with a predefined state
	public Set<Intersection> intersectionsWithState(Set<Intersection> intersections, State state) {
		Set<Intersection> setToReturn = new HashSet<Intersection>();
		for (Intersection intersect : intersections) {
			if (intersect.getState().equals(state)) {
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

	// indicates whether the set is homogeneous. I.e. all the intersections have the same state
	public boolean setHasHomoState(Set<Intersection> intersections) {
		int i = 0;
		State state = null;
		for (Intersection intersect : intersections) {
			if (i > 0) {
				if (!intersect.getState().equals(state)) {
					return false;
				}
			}
			state = intersect.getState();
			i++;
		}

		return true;
	}

	// all adjacent intersection of a set of intersections
	public Set<Intersection> adjacentIntersectionsSetWithEqualState(Set<Intersection> intersections) {
		Set<Intersection> setToReturn = new HashSet<Intersection>();
		for (Intersection intersect : intersections) {
			Set<Intersection> adjacentForThisIntersect = adjecentIntersectionsIntersectWithEqualState(intersect);
			setToReturn.addAll(adjacentForThisIntersect);
		}
		
		// remove intersections that were already in the original set
		for (Intersection intersect : intersections) {
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
			Set<Intersection>  adjacentIntersectionsIntersection = adjacentIntersectionsIntersect(intersect);
			adjacentIntersectionsGroup.addAll(adjacentIntersectionsIntersection);
		}

		// remove intersections that are already in the group
		for (Intersection intersect : adjacentIntersectionsGroup) {
			if (group.containsIntersection(intersect)) {
				adjacentIntersectionsGroup.remove(intersect);
			}
		}

		return adjacentIntersectionsGroup;
	}

	// all adjacent intersections of a group with a state similar to the group
	public Set<Intersection> adjacentIntersectionsGroupWithEqualState(Group group) {
		State state = group.getState();
		Set<Intersection> adjacentIntersectionsGroup = adjacentIntersectionsGroup(group);
		for (Intersection intersect : adjacentIntersectionsGroup) {
			if (!intersect.getState().equals(state)) {
				adjacentIntersectionsGroup.remove(intersect);
			}
		}

		return adjacentIntersectionsGroup;
	}

	// all adjacent intersections of a intersection
	public Set<Intersection> adjacentIntersectionsIntersect(Intersection intersect) {
		int col = intersect.getCol();
		int row = intersect.getRow();
		int dimension = intersect.getDimension();

		Set<Intersection> adjacentIntersections = new HashSet<Intersection>();

		int index1 = intersect.calculateIndex(col + 1, row, dimension);
		if (isIntersection(index1)) {
			adjacentIntersections.add(intersections.get(index1));
		}

		int index2 = intersect.calculateIndex(col - 1, row, dimension);
		if (isIntersection(index2)) {
			adjacentIntersections.add(intersections.get(index2));
		}

		int index3 = intersect.calculateIndex(col, row + 1, dimension);
		if (isIntersection(index3)) {
			adjacentIntersections.add(intersections.get(index3));
		}

		int index4 = intersect.calculateIndex(col, row - 1, dimension);
		if (isIntersection(index4)) {
			adjacentIntersections.add(intersections.get(index4));
		}

		return adjacentIntersections;
	}

	// all adjacent intersections of a intersection with a state similar to the provided intersection
	public Set<Intersection> adjecentIntersectionsIntersectWithEqualState(Intersection intersect) {
		State state = intersect.getState();
		Set<Intersection> intersections = adjacentIntersectionsIntersect(intersect);
		for (Intersection intersectToCheck : intersections) {
			if (!intersectToCheck.getState().equals(state)) {
				intersections.remove(intersectToCheck);
			}
		}
		return intersections;
	}
}
