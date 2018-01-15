package Project;

import java.util.Map;

public class Group {
	
	private Map<Integer, Intersection> intersections;
	private State state;

	public Group(Map<Integer, Intersection> intersections, State state) {
		this.state = state;
		this.intersections = intersections;
	}
	
	public State getState() {
		return this.state;
	}
	
	public Map<Integer, Intersection> getIntersections() {
		return intersections;
	}
	
	public void addIntersection(Intersection intersect) {
		intersections.put(intersect.getIndex(), intersect);
	}
	
	public void removeIntersection(Intersection intersect) {
		intersections.remove(intersect.getIndex());
	}
}
