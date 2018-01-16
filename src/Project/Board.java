package Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Board {

	private final int dimension;
	private Map<Integer, Intersection> intersections;
	private List<Group> groups;
	private State lastMove;

	public Board(int dimension) {
		this.dimension = dimension;
		for (int i = 0; i < this.dimension * this.dimension; i++) {
			this.intersections.put(i, new Intersection(i, this.dimension));
		}
		updateGroups();
	}

	public int getDimension() {
		return dimension;
	}

	public void setIntersection(int index, State state) {
		Intersection intersect = intersections.get(index);
		intersect.setState(state);
		this.lastMove = state;		
		updateGroups();
	}

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
				}
			}
		}
		stateToCheck = lastMove.next();
		}
	}

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

	public boolean hasLiberties(Group group) {
		return false;
	}
	
	public void setGroupToEmpty(Group group) {
		Set<Intersection> set = (Set<Intersection>) group.getIntersections().values();
		for (Intersection intersect : set) {
			intersect.setState(State.EMPTY);
		}
	}

	public Set<Intersection> intersectionsWithState(Set<Intersection> intersections, State state) {
		Set<Intersection> setToReturn = new HashSet<Intersection>();
		for (Intersection intersect : intersections) {
			if (intersect.getState().equals(state)) {
				setToReturn.add(intersect);
			}
		}
		return setToReturn;
	}

	public boolean isIntersection(int row, int col) {
		return row >= 0 && row < this.dimension && col >= 0 && col < this.dimension;
	}

	public boolean isIntersection(int index) {
		return index >= 0 && index < this.dimension * this.dimension;
	}

	public boolean setIsHomoState(Set<Intersection> intersections) {
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
