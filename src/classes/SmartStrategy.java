package classes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SmartStrategy implements Strategy {

	@Override
	public Move determineMoveUsingStrategy(ActualBoard board, Colour colour) {
		// get all the moves that yield the best leap in scores
		Set<Move> scorediff = board.calculateScoreDiffs(colour);
		
		if (scorediff.size() == 1) {
			return scorediff.toArray(new Move[1])[0];
		} 
		
		int size = scorediff.size(); 
		int item = new Random().nextInt(size); 
		int i = 0;
		for (Move obj : scorediff) {
			if (i == item) {
				return obj;
			}
			i++;
		}

		return null;
	}
}
