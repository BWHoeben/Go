package errors;

public class ScoresDoNotMatchException extends Exception  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ScoresDoNotMatchException(String message) {
		super(message);
	}

}
