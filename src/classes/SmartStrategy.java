package classes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SmartStrategy implements Strategy {

	@Override
	public Move determineMoveUsingStrategy(Board board, Colour colour) {
		if (board.getNumberOfPlayer() != 2) {
			System.out.println("Smart-strategy is only suitable for 2-player games."
					+ " Switching to random strategy");
			RandomStrategy random = new RandomStrategy();
			return random.determineMoveUsingStrategy(board, colour);
		}

		// get all the moves that yield the best leap in scores
		Map<Integer, Set<Move>> scorediff = board.calculateScoreDiffs(colour);
		Set<Move> bestMoves = scorediff.get(Collections.max(scorediff.keySet()));
		int size = bestMoves.size(); 
		int item = new Random().nextInt(size); 
		int i = 0;
		for (Move obj : bestMoves) {
			if (i == item) {
				return obj;
			}
			i++;
		}

		return null;
	}

}
