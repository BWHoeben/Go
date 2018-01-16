package Project;

public enum State {
    EMPTY, BLACK, WHITE;
	
	public State next() {
		if (this == WHITE) {
			return BLACK;
		} else if (this == BLACK) {
			return WHITE;
		} else {
			return null;
		}
	}
}
