package enyo.testing.runtime;

import java.util.Objects;

/**
 * Associates an examined value with its context.
 *
 * Examined values can be created using any of these methods:
 * <ul>
 * <li>{@link enyo.testing.Testing#examine(Object)}
 * <li>{@link enyo.testing.Testing#examine(int)}
 * <li>{@link enyo.testing.Testing#examine(long)}
 * <li>{@link enyo.testing.Testing#examine(double)}
 * <li>{@link enyo.testing.Testing#examine(char)}
 * </ul>
 * The context is set using these methods:
 * <ul>
 * <li>{@link enyo.testing.Examiner#context(String)}
 * <li>{@link enyo.testing.Examiner.OfInt#context(String)}
 * <li>{@link enyo.testing.Examiner.OfLong#context(String)}
 * <li>{@link enyo.testing.Examiner.OfDouble#context(String)}
 * <li>{@link enyo.testing.Examiner.OfChar#context(String)}
 * </ul>
 */
public final class Examined {
    /**
     * Creates a new examined value
     * @param context the context string (which should be short); can be null
     * @param value the examined object; can be null
     */
    public Examined(String context, Object value) {
        this.context = context;
        this.value = value;
    }

    /**
     * Returns the context of the examination.
     * @return the context string (can be null)
     */
    public String context() {
        return context;
    }

    /**
     * Returns the examined value.
     * @return an object (can be null)
     */
    public Object value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != Examined.class)
            return false;
        Examined other = (Examined) o;
        return Objects.equals(context, other.context)
            && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(context) * 17 + Objects.hashCode(value);
    }

    @Override
    public String toString() {
        String ctx;
        if (context == null)
            ctx = "";
        else
            ctx = context;

        if (value == null)
            return '[' + ctx + "]: null";

        try {
            return  '[' + ctx + "]: " + value.toString();
        } catch (Throwable e) {
            return '[' + ctx + "]: <error: " + e.getClass().getName() + '>';
        }
    }

    private final String context;
    private final Object value;
}
