package classes;

import java.util.Set;

public class RandomStrategy implements Strategy {

	@Override
	public String getName() {
		return "random-computer";
	}

	@Override
	public int determineMoveUsingStrategy(Board board, Colour colour) {
		Set<Integer> emptyIntersects = board.getValidMoves(colour);
		int intersectToReturn = (int) Math.floor(Math.random() * emptyIntersects.size());
		int i = 0;
		for (int intersect : emptyIntersects) {
			if (i == intersectToReturn) {
				return intersect;
			}
			i++;
		}
		// TODO Auto-generated method stub
		return 0;
	}

}
