package classes;

import java.util.Map;
import java.util.Set;

public class Group {
	
	private Map<Integer, Intersection> intersections;
	private Colour colour;

	public Group(Map<Integer, Intersection> intersections, Colour colour) {
		this.colour = colour;
		this.intersections = intersections;
	}
		
	public void setColour(Colour colour) {
		this.colour = colour;
	}
	
	public Colour getColour() {
		return this.colour;
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
