package enyo.testing;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe counter for checking invocation counts.
 */
public final class Counter {
    /**
     * Creates a new counter with the specified target value.
     * @param target the number of times {@link #increment()} can be called
     */
    public Counter(int target) {
        if (target < 0)
            throw new IllegalArgumentException("target: " + target);
        this.t = null;
        this.target = target;
    }

    /**
     * Creates a new counter with target value 1.
     * @see #Counter()
     */
    public Counter() {
        this(1);
    }

    Counter(Testing t, int target) {
        this.t = t;
        this.target = target;
    }

    /**
     * Returns the target value of this counter.
     * @return the total number {@link #increment()} <i>can</i> be called
     * @see #value()
     */
    public int target() {
        return target;
    }

    /**
     * Returns then current value of the counter.
     * @return the number of times {@link #increment()} has been called
     * @see #target()
     */
    public int value() {
        return counter.get();
    }

    /**
     * Increments the counter.
     * If the {@linkplain #value() value} is already at its
     * {@linkplain #target() target}, a test failure occurs.
     */
    public void increment() {
        while (true) {
            int value = counter.get();
            if (value == target) {
                targetReached();
                break;
            }
            if (counter.compareAndSet(value, value + 1))
                break;
        }
    }

    /**
     * Resets the value to 0.
     * Afterwards, it is af if {@link #increment} has not yet been called.
     */
    public void reset() {
        counter.set(0);
    }

    /**
     * Tests that the counter is at its target value.
     * If not, a test failure occurs.
     */
    public void checkTarget() {
        int current = counter.get();
        if (current != target)
            targetNotReached(current);
    }

    private final Testing t;
    private final int target;
    private final AtomicInteger counter = new AtomicInteger();

    private void targetReached() {
        if (t == null)
            throw new TestFailure("Counter reached target: " + target);
        else
            t.failedCheck("counter not at target", target);
    }

    private void targetNotReached(int current) {
        if (t == null)
            throw new TestFailure(String.format(
                    "Counter has not reached target %d: %d",
                    target, current));
        else
            t.failedCheck("counter at target", target, current);
    }
}
