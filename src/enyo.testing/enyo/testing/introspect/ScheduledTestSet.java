package enyo.testing.introspect;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.stream.IntStream;

/**
 * A set-like class for keeping track of tests that have begun execution.
 *
 * These sets are thread-safe.  In particular, {@link #add(ScheduledTest)}
 * can be called from multiple threads.
 */
public final class ScheduledTestSet {
    /**
     * Creates a set of scheduled tests of the indicated size.
     * Only {@link ScheduledTest#id()} values less than that size
     * are supported.
     * @param size the total number of tests
     */
    public ScheduledTestSet(int size) {
        present = new byte[size];
    }

    /**
     * Adds the scheduled test to this set.
     * @param test the test to add to the set
     * @return true if the test has been added freshly, false if it was
     *   already present
     */
    public boolean add(ScheduledTest test) {
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
    public boolean contains(ScheduledTest test) {
        int index = test.id();
        return present[index] != 0;
    }

    /**
     * Returns a stream of the IDs of the tests in this set.
     * This method can be called concurrently with {@link #add(ScheduledTest)},
     * but it is unspecified whether later additions to the
     * set show up in the stream.
     * @return a stream of integers
     */
    public IntStream stream() {
        return IntStream.range(0, present.length)
            .filter(i -> present[i] != 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        int lastPresentIndex = -1;
        for (int i = 0; i < present.length; ++i) {
            if (present[i] != 0) {
                if (lastPresentIndex < 0)
                    lastPresentIndex = i;
            } else {
                if (lastPresentIndex >= 0) {
                    if (sb.length() > 1)
                        sb.append(',');
                    sb.append(lastPresentIndex);
                    if (lastPresentIndex + 1 != i) {
                        sb.append('-');
                        sb.append(i - 1);
                    }
                    lastPresentIndex = -1;
                }
            }
        }
        if (lastPresentIndex >= 0) {
            if (sb.length() > 1)
                sb.append(',');
            sb.append(lastPresentIndex);
            if (lastPresentIndex + 1 != present.length) {
                sb.append('-');
                sb.append(present.length - 1);
            }
        }
        sb.append('}');
        return sb.toString();
    }

    // 0 if not in this set, 1 if present.
    private final byte[] present;

    private final static VarHandle byteArrayVarHandle =
        MethodHandles.arrayElementVarHandle(byte[].class);
}
