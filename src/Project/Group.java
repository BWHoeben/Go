package Project;

import java.util.Map;
import java.util.Set;

public class Group {
	
	private Map<Integer, Intersection> intersections;
	private Colour state;

	public Group(Map<Integer, Intersection> intersections, Colour state) {
		this.state = state;
		this.intersections = intersections;
	}
		
	public void setState(Colour state) {
		this.state = state;
	}
	
	public Colour getState() {
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
	
	public void addSetOfIntersections(Set<Intersection> intersectionsArg) {
		for (Intersection intersect : intersectionsArg) {
			addIntersection(intersect);
		}
	}
	
	public boolean containsIntersection(Intersection intersect) {
		return intersections.containsKey(intersect.getIndex());
	}
}
