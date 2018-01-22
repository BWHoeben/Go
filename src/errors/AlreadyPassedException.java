package errors;

public class AlreadyPassedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AlreadyPassedException(String message) {
		super(message);
	}
}
