package enyo.build;

import enyo.testing.Resources;

/**
 * The computed resources of a test case.
 *
 * These resources are derived from the {@link Resources} annotation
 * on the test method itself and its enclosing class.
 */
final class TestResources {
    public TestResources(int threads, int memory) {
        if (threads <= 0)
            throw new IllegalArgumentException("threads: " + threads);
        if (memory <= 0)
            throw new IllegalArgumentException("memory: " + memory);
        this.threads =  threads;
        this.memory = memory;
    }

    int threads() {
        return threads;
    }

    int memory() {
        return memory;
    }

    private final int threads;
    private final int memory;

    /**
     * Default values of the {@link Resources} annotation.
     */
    static final TestResources DEFAULT =
        new TestResources(Resources.THREADS, Resources.MEMORY);
}
