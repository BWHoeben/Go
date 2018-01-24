package classes;

import java.util.Set;

import GUI.GoGUIIntegrator;

public class Game {

	public int numberOfPlayers;

	//	private int current;
	private Player[] players;
	private Board board;

	public Game(Set<Player> playersSet, int boardsize) {
		this.board = new Board(boardsize, players.length);
		this.numberOfPlayers = playersSet.size(); 
		this.players = new Player[numberOfPlayers];
		int i = 0;
		for (Player player : playersSet) {
			players[i] = player;
			i++;
		}
	}
	
	public Game(Set<Player> playersSet, int boardsize, GoGUIIntegrator gogui) {
		this.board = new Board(boardsize, players.length);
		this.numberOfPlayers = playersSet.size(); 
		this.players = new Player[numberOfPlayers];
		int i = 0;
		for (Player player : playersSet) {
			players[i] = player;
			i++;
		}
	}

	public Board getBoard() {
		return board;
	}

}
