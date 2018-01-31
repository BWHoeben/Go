package Test;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import GUI.GoGUIIntegrator;
import classes.ActualBoard;
import classes.Colour;
import classes.Move;
import errors.InvalidMoveException;

public class Tests {

	private ActualBoard board;
	private GoGUIIntegrator gogui;
	private int dimension = 7;
	
	@Before
	public void setUp() {
		gogui = new GoGUIIntegrator(false, false, dimension);
		gogui.startGUI();
		this.board = new ActualBoard(dimension, 2, gogui);
	}
	
	public int getCol(int index) {
		return index % dimension;
	}
	
	public int getRow(int index) {
		return index/dimension;
	}
	
	public void processGui(int index, Colour colour) {
		Move move = new Move(index, dimension, colour);
		gogui.addStone(getCol(index), getRow(index), colour);
		try {
			board.setIntersection(move);
		} catch (InvalidMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getScore(Colour colour) {
		return this.board.getScore().get(colour);
	}
	
	//@Test
	public void testScoring() {
		processGui(38, Colour.BLACK);
		processGui(39, Colour.BLACK);
		processGui(40, Colour.BLACK);
		processGui(41, Colour.BLACK);
		processGui(42, Colour.BLACK);
		processGui(43, Colour.BLACK);
		processGui(44, Colour.BLACK);
		processGui(45, Colour.BLACK);
		processGui(46, Colour.BLACK);
		processGui(47, Colour.BLACK);
		processGui(48, Colour.BLACK);
		processGui(49, Colour.BLACK);
		processGui(30, Colour.BLACK);
	
		assertEquals(getScore(Colour.BLACK), 19 * 19);
		
		processGui(11, Colour.BLACK);
		
		assertEquals(getScore(Colour.BLACK), 19 * 19);
		
		processGui(0, Colour.WHITE);
	
		assertEquals(getScore(Colour.BLACK), (19 * 19) - 22);
		
		processGui(100, Colour.WHITE);

		assertEquals(getScore(Colour.BLACK), 14);
		
		processGui(19, Colour.WHITE);
		processGui(20, Colour.WHITE);
		processGui(21, Colour.WHITE);
		processGui(22, Colour.WHITE);
		processGui(23, Colour.WHITE);
		processGui(24, Colour.WHITE);
		processGui(25, Colour.WHITE);
		processGui(26, Colour.WHITE);
		processGui(27, Colour.WHITE);
		processGui(28, Colour.WHITE);
		processGui(29, Colour.WHITE);
		processGui(10, Colour.WHITE);

		assertEquals(getScore(Colour.WHITE), 23);
		
		processGui(57, Colour.WHITE);
		processGui(58, Colour.WHITE);
		processGui(59, Colour.WHITE);
		processGui(60, Colour.WHITE);
		processGui(61, Colour.WHITE);
		processGui(62, Colour.WHITE);
		processGui(63, Colour.WHITE);
		processGui(64, Colour.WHITE);
		processGui(65, Colour.WHITE);
		processGui(66, Colour.WHITE);
		processGui(67, Colour.WHITE);
		processGui(68, Colour.WHITE);
		processGui(69, Colour.WHITE);
		processGui(50, Colour.WHITE);
		processGui(31, Colour.WHITE);
		processGui(12, Colour.WHITE);
		
		assertEquals(getScore(Colour.WHITE), 19 * 19);
	}
	
	@Test
	public void Test2() {
		processGui(7, Colour.BLACK);
		
		processGui(32, Colour.WHITE);
		processGui(33, Colour.BLACK);
		processGui(39, Colour.BLACK);
		
		try {
			Thread.sleep(20*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
