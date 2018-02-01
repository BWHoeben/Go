package classes;

import java.util.Set;

public class RandomStrategy implements Strategy {

	@Override
	public Move determineMoveUsingStrategy(ActualBoard board, Colour colour) {
		Set<Integer> emptyIntersects = board.getValidMoves(colour);
		int intersectToReturn = (int) Math.floor(Math.random() * emptyIntersects.size());
		int i = 0;
		for (int intersect : emptyIntersects) {
			if (i == intersectToReturn) {
				Move move = new Move(intersect, board.getDimension(), colour);
				if (board.isSuicide(move)) {
					return new Move(Protocol.PASS);
				} else {
				return move;
				}
			}
			i++;
		}
		return null;
	}

}
