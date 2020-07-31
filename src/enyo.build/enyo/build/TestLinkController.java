package enyo.build;

import java.io.IOException;
import java.util.List;

/**
 * Methods for interfacing with the test-link directory on the build
 * daemon side.
 */
final class TestLinkController {
    /**
     * Sets up the {@code test-link} directory, so that tests can be
     * run in a separate VM via {@link RunTests}.
     */
    static void prepare(DirectoryLayout dirs, List<String> modules)
        throws IOException {
        TestLink testLink = new TestLink(dirs);
        TestLinkLock lock = testLink.prepareAndLock();
        try {
        } finally {
            lock.release();
        }
    }

    private TestLinkController() { } // No instances.
}
