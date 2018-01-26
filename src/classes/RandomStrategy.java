package classes;

import java.util.Set;

public class RandomStrategy implements Strategy {

	@Override
	public String getName() {
		return "random-computer";
	}

	@Override
	public Move determineMoveUsingStrategy(Board board, Colour colour) {
		Set<Integer> emptyIntersects = board.getValidMoves(colour);
		int intersectToReturn = (int) Math.floor(Math.random() * emptyIntersects.size());
		int i = 0;
		for (int intersect : emptyIntersects) {
			if (i == intersectToReturn) {
				return new Move(intersect, board.getDimension(), colour);
			}
			i++;
		}
		// TODO Auto-generated method stub
		return null;
	}

}
