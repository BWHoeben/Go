package classes;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import errors.InvalidMoveException;

public class HypotheticalBoard extends Board {

	public HypotheticalBoard(Colour[] state, int dimension, int numberOfPlayersArg,
			List<Colour[]> boardSituationsArg, Colour lastMoveArg, 
			Map<Colour, Integer> scoreArg) {
		super(dimension, numberOfPlayersArg);
		assert state.length == dimension * dimension;
		intersections = new HashMap<Integer, Intersection>();
		for (int i = 0; i < state.length; i++) {
			intersections.put(i, new Intersection(i, dimension, state[i]));
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
		updateGroups();
		updateScore();
	}

}
