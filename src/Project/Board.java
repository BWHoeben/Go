package Project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Board {
	
	private final int dimension;
	private Map<Integer, Intersection> intersections;
	private List<Group> groups;
	
	public Board(int dimension) {
		this.dimension = dimension;
		for (int i = 0; i < this.dimension * this.dimension; i++) {
			this.intersections.put(i, new Intersection(i, this.dimension));
		}
	}
	
	public int getDimension() {
		return dimension;
	}
	
	public void setIntersection(int index, State state) {
		intersections[index].setState(state);
		updateGroups();
	}
	
	public void updateGroups() {

		
	}
	
	public Set<Intersection> adjacentIntersections(Group group) {
		for (Entry<Integer, Intersection> entry : group.getIntersections().entrySet()) {
			Intersection intersect = entry.getValue();
			
			int col = intersect.getCol();
			int row = intersect.getRow();
			
			
			
			
			Set<Intersection> adjacentIntersections = new HashSet<Intersection>();
			
		}
		return null;
	}
}
