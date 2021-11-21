package enyo.testing.introspect;

import enyo.testing.Resources;

import java.io.Serializable;

/**
 * The computed resources of a test case.
 *
 * Usually, these resources are derived from the {@link Resources}
 * annotation on the test method itself and its enclosing class.
 *
 * <p>Test resource objects are immutable.
 */
public final class TestResources implements Serializable {
    /**
     * Creates a new combination of test resources.
     * @param threads the number of threads running parallel
     * @param memory the amount of memory needed by the test, in mibibytes
     * @throws IllegalArgumentException if any of the arguments are not positive
     */
    public TestResources(int threads, int memory) {
        if (threads <= 0)
            throw new IllegalArgumentException("threads: " + threads);
        if (memory <= 0)
            throw new IllegalArgumentException("memory: " + memory);
        this.threads =  threads;
        this.memory = memory;
    }

    /**
     * The number of threads executed in parallel by the test.
     * @return a thread count, a positive number
     * @see Resources#threads()
     */
    public int threads() {
        return threads;
    }

    /**
     * The amount of memory needed by the test at the same time.
     * @return the amount of memory in mibibyte, a positive number
     * @see Resources#memory()
     */
    public int memory() {
        return memory;
    }

    @Override
    public String toString() {
        return String.format("threads=%d memory=%d", threads, memory);
    }

    /**
     * Returns the resources available in the system.
     * @return a resources object
     */
    public static TestResources systemResources() {
        Runtime runtime = Runtime.getRuntime();
        long memoryMB = runtime.maxMemory() >> 20;
        int intMemoryMB = (int) memoryMB;
        if (intMemoryMB != memoryMB)
            intMemoryMB = Integer.MAX_VALUE;

        // Reserve some headroom.  This is rather arbitrary.  The
        // expectation is that most systems will be in the last
        // category, with a headroom of 225, and hopefully no
        // effective limit on test parallelism due to memory usage.
        if (intMemoryMB <= 10)
            intMemoryMB = 1;
        else if (intMemoryMB < 500)
            intMemoryMB -= intMemoryMB / 5;
        else if (intMemoryMB < 1000)
            intMemoryMB -= 100 + (intMemoryMB - 500) / 2;
        else
            intMemoryMB -= 225;

        return new TestResources(runtime.availableProcessors(), intMemoryMB);
    }

    /**
     * Default values of the {@link Resources} annotation.
     */
    public static final TestResources DEFAULT =
        new TestResources(Resources.THREADS, Resources.MEMORY);

    /**
     * Number of threads.
     * @see #threads()
     */
    private final int threads;

    /**
     * Amount of memory (in MiB).
     * @see #memory()
     */
    private final int memory;

    private static final long serialVersionUID = 0xc1cb86627f280b05L;
}
