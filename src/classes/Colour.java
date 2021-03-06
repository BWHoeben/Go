package classes;

import errors.InvalidColourException;

public enum Colour {
	EMPTY, BLACK, WHITE, ORANGE, PINK;

	public Colour first() {
		return BLACK;
	}
	
	public char toChar() {
		switch (this) {
			case BLACK : return 'B';
			case WHITE : return 'W';
			case ORANGE : return 'O';
			case PINK : return 'P';
			default : return 'E';
		}
	}
	
	public static Colour useChar(char c) {
		switch (c) {
			case 'B' : return BLACK;
			case 'W' : return WHITE;
			case 'O' : return ORANGE;
			case 'P' : return PINK;
			default : return EMPTY;
		}
	}

	public Colour next(int numberOfPlayers) {
		if (numberOfPlayers == 2) {
			switch (this) {
				case BLACK : return WHITE;
				case WHITE : return BLACK;
				default : return null;
			}
		} else if (numberOfPlayers == 3) {
			switch (this) {
				case BLACK : return WHITE;
				case WHITE : return ORANGE;
				case ORANGE : return BLACK;
				default : return null;
			}
		} else if (numberOfPlayers == 4) {
			switch (this) {
				case BLACK : return WHITE;
				case WHITE : return ORANGE;
				case ORANGE : return PINK;
				case PINK : return BLACK;
				default : return null;
			}	
		}
		return null;
	}

	public String toString() {
		switch (this) {
			case EMPTY : return "EMPTY";
			case BLACK : return "BLACK";
			case WHITE : return "WHITE";
			case ORANGE : return "ORANGE";
			case PINK : return "PINK";
			default : return "Invalid colour! :(";
		}
	}

	public static Colour getColour(String colourString) throws InvalidColourException {
		Colour colour = Colour.BLACK;
		while (!colour.toString().equals(colourString)) {
			colour = colour.next(4);
			if (colour.equals(colour.first())) {
				throw new InvalidColourException("Invalid/Unkown colour!");
			}
		}
		return colour;
	}
}
