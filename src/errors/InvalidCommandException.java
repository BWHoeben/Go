package errors;

public class InvalidCommandException extends Exception  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InvalidCommandException(String message) {
		super(message);
	}

}
