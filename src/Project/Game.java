package Project;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import GUI.GoGUIIntegrator;
import Project.State;

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
			if (player.getState().equals(Project.State.BLACK)) {
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
