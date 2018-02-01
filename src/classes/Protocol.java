package classes;

public class Protocol {
	public static final String VERSIONNUMBER = "6";
	public static final String VERSION = "VERSION";
	public static final String NAME = "NAME";
	public static final String EXTENSIONS = "EXTENSIONS";
	public static final String MOVE = "MOVE";
	public static final String TURN = "TURN";
	public static final String FIRST = "FIRST";
	public static final String PASS = "PASS";
	public static final String SETTINGS = "SETTINGS";
	public static final String QUIT = "QUIT";
	public static final String REQUESTGAME = "REQUESTGAME";
	public static final String RANDOM = "RANDOM";
	
	public static final String ENCODING = "UTF-8";
	public static final int TIMEOUTSECONDS = 90;
	public static final short DEFAULT_PORT = 5647;
	public static final String DELIMITER1 = "$";
	public static final String DELIMITER2 = "_";
	public static final String COMMAND_END = "\n";
	
	public static final String START = "START";
	
	public static final String ENDGAME = "ENDGAME";
	public static final String FINISHED = "FINISHED";
	public static final String ABORTED = "ABORTED";
	public static final String TIMEOUT = "TIMEOUT";
	
	public static final String YES = "YES";
	public static final String NO = "NO";
	
	public static final String ERROR = "ERROR";
	public static final String UNKNOWN = "UNKNOWNCOMMAND";
	public static final String INVALID = "INVALIDMOVE";
	public static final String NAMETAKEN = "NAMETAKEN";
	public static final String INCOMPATIBLEPROTOCOL = "INCOMPATIBLEPROTOCOL";
	public static final String OTHER = "OTHER";
	public static final String EXIT = "EXIT";
	
	public Protocol() {
		
	}
}
