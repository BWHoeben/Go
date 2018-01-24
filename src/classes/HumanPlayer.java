package classes;

import java.util.Scanner;

import errors.InvalidCoordinateException;
import errors.NotAnIntException;

public class HumanPlayer extends Player {

	private Scanner scanner;
	
	public HumanPlayer(String name, Colour colour, Scanner scannerArg) {
		super(name, colour);
		this.scanner = scannerArg;
	}

	@Override
	public Move determineMove(Board board) {
		int boardSize = board.getDimension();
		int row = -1000;
		int col = -1000;
		
		System.out.println("Please enter row_column, PASS or QUIT: ");			
		String answer = scanner.nextLine();
		if (answer.toUpperCase().equals(Protocol.PASS)) {
			System.out.println("Passing turn.");
			if (getLastMoveWasPass()) {
				System.out.println("Last move was also a pass.");
			//leaveGame();
			}
			pass(true);

			return new Move(Protocol.PASS);
		} else if (answer.toUpperCase().equals(Protocol.QUIT)) {
			System.out.println("Terminate game.");
			//leaveGame();
			return new Move(Protocol.QUIT);
		} else {	
			pass(false);
			try {
				return new Move(answer, boardSize, this.getColour());
			} catch (InvalidCoordinateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}
