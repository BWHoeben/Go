package classes;

import java.util.Random;
import java.util.Set;

public class SmartStrategy implements Strategy {

	@Override
	public Move determineMoveUsingStrategy(ActualBoard board, Colour colour) {
		long StartTime = System.currentTimeMillis();

		// get all the moves that yield the best leap in scores
		MoveScoreCombination msc = board.calculateScoreDiffs(colour);
		Set<Move> moves = msc.getMoves();
		if (moves.size() == 1) {
			return moves.toArray(new Move[1])[0];
		}
		
		int size = moves.size(); 
		int item = new Random().nextInt(size); 
		int i = 0;
		for (Move obj : moves) {
			if (i == item) {
				System.out.println("Determining move took: " + (System.currentTimeMillis() - StartTime));
				return obj;
			}
			i++;
		}

		return null;
	}

}
