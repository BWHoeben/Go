package classes;

import java.util.Scanner;

import errors.InvalidCoordinateException;

public class HumanPlayer extends Player {

	private Scanner scanner;

	public HumanPlayer(String name, Colour colour, Scanner scannerArg) {
		super(name, colour);
		this.scanner = scannerArg;
	}

	public Move determineMove(ActualBoard board) {
		int boardSize = board.getDimension();
		while (true) {
			System.out.println("Please enter row_column, PASS, QUIT or EXIT: ");			
			String answer = scanner.nextLine();
			if (answer.toUpperCase().equals(Protocol.PASS)) {
				System.out.println("Passing turn.");
				if (getLastMoveWasPass()) {
					System.out.println("Last move was also a pass.");
				}
				pass(true);
				return new Move(Protocol.PASS);
			} else if (answer.toUpperCase().equals(Protocol.QUIT)) {
				System.out.println("Terminating game.");
				return new Move(Protocol.QUIT);
			} else if (answer.toUpperCase().equals(Protocol.EXIT)) {	
				System.out.println("Disconnecting from server");
				return new Move(Protocol.EXIT);
			} else if (isValidAnswer(answer, board)) {	
				pass(false);
				try {
					System.out.println("Valid move. Index: " + 
							new Move(answer, board.getDimension(), this.getColour()).getIndex());
				} catch (InvalidCoordinateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					return new Move(answer, boardSize, this.getColour());
				} catch (InvalidCoordinateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			} else {
				System.out.println("Invalid move, try again.");
			}
		}
	}

	public boolean isValidAnswer(String answer, ActualBoard board) {
		// Is the format correct?
		String[] array = answer.split(Protocol.DELIMITER2);
		try {
			Integer.parseInt(array[0]);
			Integer.parseInt(array[0]);
		} catch (NumberFormatException e) {
			return false;
		}

		// Is it a valid move?
		try {
			return board.isValidMove(new Move(answer, board.getDimension(), this.getColour()));
		} catch (InvalidCoordinateException e) {
			return false;
		}
	}
}
