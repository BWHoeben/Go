package Project;

import java.util.Scanner;

import Project.Errors.NotAnIntException;

public class HumanPlayer extends Player {

	public HumanPlayer(String name, Colour state) {
		super(name, state);
	}

	@Override
	public int determineMove(Board board) {
		int boardSize = board.getDimension();
		Scanner scanner = new Scanner(System.in);

		try {
		System.out.println("Please enter row: ");			
		int row = scanner.nextInt();
		System.out.println("Please enter column: ");			
		int col = scanner.nextInt();		
		scanner.close();
		return calculateIndex(col, row, boardSize);
		} catch (NumberFormatException e) {
			try {
				scanner.close();
				throw new NotAnIntException("Provided input should be a number!");
			} catch (NotAnIntException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return 0;
	}
	
	public int calculateIndex(int col, int row, int dimensionOfBoard) {
		return (row * dimensionOfBoard) + col;
	}
}
