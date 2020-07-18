package enyo.testing;

/**
 * An exception indicating a test failure.
 */
public class TestFailure extends RuntimeException {
    /**
     * Creates a new test failure exception.
     * @param message the exception message
     */
    public TestFailure(String message) {
        super(message);
    }

    /**
     * Creates a new test failure exception.
     * @param message the exception message
     * @param cause the cause of this exception
     */
    public TestFailure(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 0xcc8579e0dd08dc1aL;
}
