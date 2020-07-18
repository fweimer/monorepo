package enyo.testing;

/**
 * An exception indicating that a test has already failed.
 *
 * Such exceptions are thrown to implement test barriers which
 * prevent the execution of subsequent test code.
 *
 * @see Testing#fullBarrier()
 */
public class TestAlreadyFailed extends TestFailure {
    /**
     * Creates a new test failure indicator.
     */
    public TestAlreadyFailed() {
        super("Test has already failed");
    }

    private static final long serialVersionUID = 0x53654c91c8d76c0bL;
}
