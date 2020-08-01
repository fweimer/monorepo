package enyo.build;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Interfacing with the test-link directory on the test VM side
 */
final class TestLinkConsumer {
    TestLinkConsumer(TestLink testLink) {
        this.testLink = testLink;
    }

    void iterateTestsList(String listName, Consumer<TestMethodLocator> tester)
        throws IOException {
        try (DataInputStream in = testLink.openTestsList(listName)) {
            while (true) {
                Optional<TestMethodLocator> test = TestLink.readOneTest(in);
                if (!test.isPresent())
                    break;
                tester.accept(test.get());
            }
        }
    }

    private final TestLink testLink;
}
