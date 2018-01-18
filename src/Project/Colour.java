package Project;

public enum Colour {
	EMPTY, BLACK, WHITE, ORANGE, PINK;
	
	public Colour first() {
		return BLACK;
	}
	
	public Colour next() {
		switch(this) {
		case BLACK : return WHITE;
		case WHITE : return ORANGE;
		case ORANGE : return PINK;
		case PINK : return BLACK;
		default : return null;
		}
	}
	
	public String toString() {
		switch(this) {
		case EMPTY : return "EMPTY";
		case BLACK : return "BLACK";
		case WHITE : return "WHITE";
		case ORANGE : return "ORANGE";
		case PINK : return "";
		default : return "Invalid colour! :(";
		}
	}
}
