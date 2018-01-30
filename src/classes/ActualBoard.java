package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import GUI.GoGUIIntegrator;

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

	
}
