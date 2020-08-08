package enyo.build;

import enyo.io.ObjectSequences;
import enyo.io.function.IOConsumer;
import enyo.testing.Resources;
import enyo.testing.introspect.ScheduledTest;
import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.introspect.TestResources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Low-level I/O for the {@code test-link} directory.
 */
final class TestLink implements Closeable {
    /**
     * Creates a new test link for the controller (build daemon) side.
     * The link object is locked, and the subdirectory has been scrubbed
     * (it is empty except for lock files).
     * The link should be closed using {@link #close()}.
     * @return the test link object
     */
    static TestLink forController(DirectoryLayout dirs) throws IOException {
        {
            Path path = dirs.testLink();
            if (!Files.isDirectory(path))
                Files.createDirectory(path);
        }

        return new TestLink(dirs, true);
    }

    /**
     * Creates a new test link for the consumer (test runner) side.
     * The link should be closed using {@link #close()}.
     * @return the test link object
     */
    static TestLink forConsumer(DirectoryLayout dirs) throws IOException {
        return new TestLink(Objects.requireNonNull(dirs), false);
    }

    /**
     * Releases the lock on the {@code test-link} subdirectory.
     */
    @Override
    public void close() throws IOException {
        try {
            lock.release();
        } finally {
            lockChannel.close();
        }
    }

    /**
     * The directory layout in which this test link resides.
     */
    DirectoryLayout directoryLayout() {
        return dirs;
    }

    /**
     * Returns the shared array used for test link communication.
     */
    SharedArray sharedArray() {
        return sharedArray;
    }

    /**
     * Writes a stream of test method locators under the name.
     * @see #openTestsList(String)
     * @see #readOneTest(DataInput)
     */
    void writeTests(String listName, Stream<ScheduledTest> tests)
        throws IOException {
        try (OutputStream out = newOutputStream(listName + ".testlist");
            DataOutputStream dout = new DataOutputStream(out)) {
            ObjectSequences.writeSequence(dout, tests,
                TestLink::writeScheduledTest);
        }
    }

    /**
     * Writes a stream of test method locators for the specified queue.
     * @see #writeTests(String,Stream)
     */
    void writeTests(QueueKind kind, Stream<ScheduledTest> tests)
        throws IOException {
        writeTests(kind.queueName(), tests);
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
     * Opens a tests list file.
     * The file must have been written using
     * {@link #writeTests(QueueKind,Stream)}.
     * @see #readOneTest(DataInput)
     */
    DataInputStream openTestsList(QueueKind kind) throws IOException {
        return openTestsList(kind.queueName());
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

    /**
     * Parameters shared across the test-link divide.
     */
    final static class Parameters implements Serializable {
        Parameters(List<String> testModules, int testCount) {
            this.testModules = List.copyOf(testModules);
            this.testCount = testCount;
        }

        List<String> testModules() {
            return testModules;
        }

        /**
         * The maximum {@linkplain ScheduledTest#id() test ID} plus one.
         */
        int testCount() {
            return testCount;
        }

        private final List<String> testModules;
        private final int testCount;

        // This class is Serializable because the convenience of
        // automated serialization is not a problem, given that a test
        // VM only needs one such object.
        private static final long serialVersionUID = 0x80202bc42496efa6L;
    }

    void writeParameters(Parameters parameters) throws IOException {
        Objects.requireNonNull(parameters);
        try (OutputStream out0 = newOutputStream("parameters");
             ObjectOutputStream out = new ObjectOutputStream(out0)) {
            out.writeObject(parameters);
        }
    }

    Parameters readParameters() throws IOException {
        try (InputStream in0 = newInputStream("parameters");
             ObjectInputStream in = new ObjectInputStream(in0)) {
            return (Parameters) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    private final DirectoryLayout dirs;
    private final SharedArray sharedArray;
    private final FileChannel lockChannel;
    private final FileLock lock;

    private TestLink(DirectoryLayout dirs, boolean cleanup) throws IOException {
        this.dirs = dirs;
        Path pathLock = dirs.testLink().resolve("lock");
        lockChannel = FileChannel.open(pathLock, StandardOpenOption.CREATE,
            StandardOpenOption.WRITE);
        boolean good = false;
        try {
            lock = lockChannel.tryLock(0, 0, false);
            if (lock == null)
                throw new IOException("Cannot lock " + dirs.testLink());

            if (cleanup)
                cleanup(dirs, pathLock);

            sharedArray = new SharedArray(dirs.testLink().resolve("shared"));

            good = true;
        } finally {
            if (!good)
                lockChannel.close();
        }
    }

    /**
     * Creates a buffered output stream for a file in the test-link
     * directory.
     */
    private OutputStream newOutputStream(String fileName) throws IOException {
        return new BufferedOutputStream(Files.newOutputStream(
                dirs.testLink().resolve(fileName),
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE));
    }

    /**
     * Creates a buffered input stream for a file in the test-link
     * directory.
     */
    private InputStream newInputStream(String fileName) throws IOException {
        return new BufferedInputStream(Files.newInputStream(
                dirs.testLink().resolve(fileName)));
    }

    /**
     * Remove stale files in the test-link subdirectory.
     */
    private static void cleanup(DirectoryLayout dirs, Path lock)
        throws IOException {
        Files.list(dirs.testLink())
            // Do not delete the lock file.
            .filter(p -> !p.equals(lock))
            .forEach(IOConsumer.of(Files::delete));
    }

    private static void writeTestMethodLocator(DataOutput out,
        TestMethodLocator test) throws IOException {
        ObjectSequences.writeAsOptional(out, test.moduleName(),
            ObjectSequences::writeString);
        ObjectSequences.writeString(out, test.className());
        ObjectSequences.writeString(out, test.methodName());
        out.writeBoolean(test.hasTestingArgument());
    }

    private static TestMethodLocator readTestMethodLocator(DataInput in)
        throws IOException {
        Optional<String> moduleName =
            ObjectSequences.readObject(in, ObjectSequences::readString);
        String className = ObjectSequences.readString(in);
        String methodName = ObjectSequences.readString(in);
        boolean hasTestingArgument = in.readBoolean();
        return new TestMethodLocator(moduleName, className, methodName,
            hasTestingArgument);
    }

    private static void writeScheduledTest(DataOutput out,
        ScheduledTest test) throws IOException {
        out.writeInt(test.id());
        writeTestMethodLocator(out, test.locator());
        writeTestResources(out, test.resources());
    }

    private static ScheduledTest readScheduledTest(DataInput in)
        throws IOException {
        int id = in.readInt();
        TestMethodLocator locator = readTestMethodLocator(in);
        TestResources resources = readTestResources(in);
        return new ScheduledTest(id, locator, resources);
    }

    private static void writeTestResources(DataOutput out,
        TestResources resources) throws IOException {
        out.writeInt(resources.threads());
        out.writeInt(resources.memory());
    }

    private static TestResources readTestResources(DataInput in)
        throws IOException {
        int threads = in.readInt();
        int memory = in.readInt();
        if (threads == TestResources.DEFAULT.threads()
            && memory == TestResources.DEFAULT.memory())
            return TestResources.DEFAULT;
        else
            return new TestResources(threads, memory);
    }
}
