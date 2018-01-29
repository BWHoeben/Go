package classes;

import java.util.HashMap;
import java.util.Map;

public class SmartStrategy implements Strategy {

	@Override
	public Move determineMoveUsingStrategy(Board board, Colour colour) {
		if (board.getNumberOfPlayer() != 2) {
			System.out.println("Smart-strategy is only suitable for 2-player games."
					+ " Switching to random strategy");
			RandomStrategy random = new RandomStrategy();
			return random.determineMoveUsingStrategy(board, colour);
		}
		
		Map<Move, Integer> scorediff = board.calculateScoreDiff(colour);
		return null;
	}

}
