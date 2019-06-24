package exceptions;

public class ProbabilitySumException extends Exception {

	private static final long serialVersionUID = 1L;
	public ProbabilitySumException() {
        super("[Error] Missing probabbilities - The sum of the probabilities was not 1.");
    }

	public ProbabilitySumException(String message) {
        super(message);
    }

}
