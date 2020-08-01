package enyo.build;

import enyo.io.ObjectSequences;
import enyo.io.function.IOConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Low-level I/O for the {@code test-link} directory.
 */
final class TestLink {
    /**
     * Obtains the {@code test-link} directory from the directory layout.
     */
    TestLink(DirectoryLayout dirs) {
        this.dirs = Objects.requireNonNull(dirs);
    }

    /**
     * Make sure that the {@code test-link} directory exists and lock it.
     */
    TestLinkLock prepareAndLock() throws IOException {
        Path testLink = dirs.testLink();
        if (!Files.isDirectory(testLink))
            Files.createDirectory(testLink);

        TestLinkLock lock = lock();
        boolean ok = false;
        try {
            cleanupTestLink(lock);
            ok = true;
        } finally {
            if (!ok)
                lock.release();
        }
        return lock;
    }

    /**
     * Locks the {@code test-link} directory.
     */
    TestLinkLock lock() throws IOException {
        return new TestLinkLock(dirs);
    }

    /**
     * Writes a stream of test method locators under the name.
     * @see #openTestsList(String)
     * @see #readOneTest(DataInput)
     */
    void writeTests(String listName, Stream<ScheduledTest> tests)
        throws IOException {
        Path target = dirs.testLink().resolve(listName + ".testlist");
        try (OutputStream out0 = Files.newOutputStream(target,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
            BufferedOutputStream out = new BufferedOutputStream(out0);
            DataOutputStream dout = new DataOutputStream(out)) {
            ObjectSequences.writeSequence(dout, tests,
                TestLink::writeScheduledTest);
        }
    }

    /**
     * Opens a tests list file.
     * The file must have been written using
     * {@link #writeTests(String,Stream)}.
     * @see #writeTests(String,Stream)
     * @see #readOneTest(DataInput)
     */
    DataInputStream openTestsList(String listName) throws IOException {
        Path path = dirs.testLink().resolve(listName + ".testlist");
        return new DataInputStream(new BufferedInputStream(
                Files.newInputStream(path)));
    }

    /**
     * Reads a single test from a stream.
     * The stream must have been written using
     * {@link #writeTests(String,Stream)}.
     * @return if present, the next test, otherwise an empty value,
     *   indicating that the end of the list of tests has been reached
     * @see #writeTests(String,Stream)
     */
    static Optional<ScheduledTest> readOneTest(DataInput in)
        throws IOException {
        return ObjectSequences.readObject(in, TestLink::readScheduledTest);
    }

    private final DirectoryLayout dirs;

    /**
     * Remove stale files in the test-link subdirectory.
     */
    private void cleanupTestLink(TestLinkLock lock) throws IOException {
        Files.list(dirs.testLink())
            // Do not delete the lock file.
            .filter(p -> !p.equals(lock.path()))
            .forEach(IOConsumer.of(Files::delete));
    }

    private static void writeTestMethodLocator(DataOutput out,
        TestMethodLocator test) throws IOException {
        ObjectSequences.writeAsOptional(out, test.moduleName(),
            (out1, string) -> out1.writeUTF(string));
        out.writeUTF(test.className());
        out.writeUTF(test.methodName());
        out.writeBoolean(test.hasTestingArgument());
    }

    private static TestMethodLocator readTestMethodLocator(DataInput in)
        throws IOException {
        Optional<String> moduleName =
            ObjectSequences.readObject(in, DataInput::readUTF);
        String className = in.readUTF();
        String methodName = in.readUTF();
        boolean hasTestingArgument = in.readBoolean();
        return new TestMethodLocator(moduleName, className, methodName,
            hasTestingArgument);
    }

    private static void writeScheduledTest(DataOutput out,
        ScheduledTest test) throws IOException {
        out.writeInt(test.id());
        writeTestMethodLocator(out, test.locator());
    }

    private static ScheduledTest readScheduledTest(DataInput in)
        throws IOException {
        int id = in.readInt();
        TestMethodLocator locator = readTestMethodLocator(in);
        return new ScheduledTest(id, locator);
    }
}
