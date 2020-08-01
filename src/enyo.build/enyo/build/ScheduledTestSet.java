package enyo.build;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * A set-like class for keeping track of tests that have begun execution.
 */
final class ScheduledTestSet {
    /**
     * Creates a set of scheduled tests of the indicated size.
     * Only {@link ScheduledTest#id()} values less than that size
     * are supported.
     * @param size the total number of tests
     */
    ScheduledTestSet(int size) {
        present = new byte[size];
    }

    /**
     * Adds the scheduled test to this set.
     * @param test the test to add to the set
     * @return true if the test has been added freshly, false if it was
     *   already present
     */
    boolean add(ScheduledTest test) {
        int index = test.id();
        return byteArrayVarHandle.compareAndSet(
            present, index, (byte) 0, (byte) 1);
    }

    /**
     * Checks if a test has already been added to the set.
     * This is intended just for optimization purposes.  In order
     * to prevent repeated actions on newly added tests, the return value of
     * {@link #add(ScheduledTest)} must be used.
     * @param test the test to check against this set
     * @return true if the test is in the set, false if not
     */
    boolean contains(ScheduledTest test) {
        int index = test.id();
        return present[index] != 0;
    }

    // 0 if not in this set, 1 if present.
    private final byte[] present;

    private final static VarHandle byteArrayVarHandle =
        MethodHandles.arrayElementVarHandle(byte[].class);
}
