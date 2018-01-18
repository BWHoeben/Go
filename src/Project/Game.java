package Project;

import java.util.Set;

import GUI.GoGUIIntegrator;

public class Game extends Thread {
	
	public int numberOfPlayers;
	
	private int current;
	private Player[] players;
	private Board board;
	
	public Game(Set<Player> playersSet, int boardsize, GoGUIIntegrator gogui) {
		this.board = new Board(boardsize);
		this.numberOfPlayers = playersSet.size(); 
		this.players = new Player[numberOfPlayers];
		int i = 0;
		for (Player player : playersSet) {
			players[i] = player;
			if (player.getState().equals(Project.Colour.BLACK)) {
				current = i;
			}
			i++;
		}
	}

	public void run() {
		play();
	}
	
	public void play() {
		int i = current;
		while (!this.board.GameOver()) {
			players[current].makeMove(board);
			current = i % numberOfPlayers;
			i++;
		}
	}
	
	public Board getBoard() {
		return board;
	}
	
}
