package enyo.build;

import enyo.io.function.IOFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interfacing with the test-link directory on the build daemon side.
 */
final class TestLinkController {
    /**
     * Sets up the {@code test-link} directory, so that tests can be
     * run in a separate VM via {@link RunTests}.
     */
    TestLinkController(DirectoryLayout dirs, List<String> modules)
        throws IOException {
        this.dirs = dirs;

        ArrayList<ScheduledTest> allTests = allTests(modules);

        TestLink testLink = new TestLink(dirs);
        TestLinkLock lock = testLink.prepareAndLock();
        try {
            testLink.writeParameters(
                new TestLink.Parameters(modules, allTests.size()));
            testLink.writeTests("main", allTests.stream());
        } finally {
            lock.release();
        }
    }

    private final DirectoryLayout dirs;
    private ConcurrentLinkedQueue<String> errors =
        new ConcurrentLinkedQueue<>();

    private ArrayList<ScheduledTest> allTests(List<String> modules) {
        AtomicInteger idSource = new AtomicInteger();
        return modules.stream()
            .flatMap(IOFunction.of(mod -> FindTests.tests(dirs, mod)))
            .filter(method -> method.hasTestAnnotation)
            .flatMap(method -> {
                    if (method.error != null) {
                        errors.add(String.format("%s %s %s: %s%n",
                                method.classDetails.module,
                                method.classDetails.clazz.className(),
                                method.method.name(),
                                method.error));
                        return Stream.of();
                    } else
                        return Stream.of(new ScheduledTest(
                                idSource.getAndIncrement(),
                                method.locator()));
                })
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
