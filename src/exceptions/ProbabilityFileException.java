package exceptions;

public class ProbabilityFileException extends Exception {

	private static final long serialVersionUID = 1L;
	public ProbabilityFileException() {
        super("[Error] The probabilities file was not parsed correctly or the file path doesn't exist.");
    }

	public ProbabilityFileException(String message) {
        super(message);
    }

}
