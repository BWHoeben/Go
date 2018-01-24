package classes;

import errors.InvalidColourException;

public enum Colour {
	EMPTY, BLACK, WHITE, ORANGE, PINK;

	public Colour first() {
		return BLACK;
	}

	//public Colour next() {
	//	switch (this) {
	//		case BLACK : return WHITE;
	//		case WHITE : return ORANGE;
	//		case ORANGE : return PINK;
	//		case PINK : return BLACK;
	//		default : return null;
	//	}
	//}

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
