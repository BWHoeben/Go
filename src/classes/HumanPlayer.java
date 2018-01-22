package classes;

import java.util.Scanner;

import errors.NotAnIntException;

public class HumanPlayer extends Player {

	public HumanPlayer(String name, Colour colour) {
		super(name, colour);
	}

	@Override
	public int determineMove(Board board) {
		int boardSize = board.getDimension();
		int row = -1000;
		int col = -1000;
		Scanner scanner = new Scanner(System.in);

		System.out.println("Please enter row_column or PASS: ");			
		String answer = scanner.nextLine();
		if (answer.equals(Protocol.PASS)) {
			System.out.println("Passing turn.");
			if (getLastMoveWasPass()) {
				System.out.println("Last move was also a pass. Terminating game.");
				leaveGame();
			}
			pass(true);
			scanner.close();
			return -1000;
		} else {	
			pass(false);
			String[] split = answer.split(Protocol.DELIMITER2);
			if (split.length != 2) {
				try {
					scanner.close();
					throw new NotAnIntException("Wrong syntax used! Use row_col");
				} catch (NotAnIntException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					row = Integer.parseInt(split[0]);
					col = Integer.parseInt(split[1]);
				} catch (NumberFormatException e) {
					try {
						scanner.close();
						throw new NotAnIntException("Provided input should be a number!");
					} catch (NotAnIntException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			scanner.close();
			return calculateIndex(col, row, boardSize);
		}
	}

	public int calculateIndex(int col, int row, int dimensionOfBoard) {
		return (row * dimensionOfBoard) + col;
	}
}
