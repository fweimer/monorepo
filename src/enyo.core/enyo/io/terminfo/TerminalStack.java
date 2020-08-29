package enyo.io.terminfo;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An operand stack for executing a terminal script.
 */
public final class TerminalStack {
    /**
     * Creates a new terminal stack.
     * The stack is initially empty.
     */
    public TerminalStack() {
    }

    /**
     * Pushes the integer value onto the stack.
     * @param value the integer to push
     */
    public void pushInt(int value) {
        if (intStack != null) {
            if (intStack.length <= depth)
                intStack = Arrays.copyOf(intStack, depth + 1);
        } else
            intStack = new int[depth + 1];
        intStack[depth++] = value;
    }

    /**
     * Pushes the string onto the stack.
     * @param value the byte string to push
     */
    public void pushString(byte[] value) {
        Objects.requireNonNull(value);
        if (stringStack != null) {
            if (stringStack.length <= depth)
                stringStack = Arrays.copyOf(stringStack, depth + 1);
        } else
            stringStack = new byte[depth + 1][];
        stringStack[depth++] = value.clone();
    }

    /**
     * Pops an integer value off the stack.
     * @return the previously-pushed integer value
     * @throws IllegalStateException if the stack is empty, or of a string
     *   is at the top of the stack
     */
    public int popInt() {
        checkNotEmpty();
        int idx = depth - 1;
        if (stringStack != null && idx < stringStack.length
            && stringStack[idx] != null)
            throw new IllegalStateException("Top of stack is string");
        int result = intStack[idx];
        --depth;
        return result;
    }

    /**
     * Pops a string off the stack.
     * @return the previously-pushed byte string
     * @throws IllegalStateException if the stack is empty, or of an integer
     *   is at the top of the stack
     */
    public byte[] popString() {
        checkNotEmpty();
        int idx = depth - 1;
        byte[] result = null;
        if (stringStack != null && idx < stringStack.length)
            result = stringStack[idx];
        if (result == null)
            throw new IllegalStateException("Top of stack is integer");
        stringStack[idx] = null;
        --depth;
        // Already cloned by pushString(), cannot be used again.
        return result;
    }

    /**
     * Returns the contents of the stack.
     * The first element of the stream (if present) is the top of the stack.
     * @return a stream of {@link Integer} {@code byte[]} objects
     */
    public Stream<Object> stream() {
        return IntStream.range(0, depth)
            .mapToObj(i0 -> {
                    int i = depth - i0 - 1;
                    if (stringStack != null && i < stringStack.length) {
                        byte[] str = stringStack[i];
                        if (str != null)
                            return str;
                    }
                    return intStack[i];
                });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder('[');
        sb.append('[');
        for (int i = depth - 1; i >= 0; --i) {
            if (sb.length() > 1)
                sb.append(',');
            if (stringStack != null && i < stringStack.length) {
                byte[] str = stringStack[i];
                if (str != null) {
                    sb.append('"');
                    for (byte b : str) {
                        if (b < ' ' || b > '~') {
                            int b0 = Byte.toUnsignedInt(b);
                            sb.append('\\');
                            sb.append((char) ('0' + (b0 >> 6)));
                            sb.append((char) ('0' + ((b0 >> 3) & 7)));
                            sb.append((char) ('0' + (b0 & 7)));
                        } else {
                            if (b == '\\' || b == '"')
                                sb.append('\\');
                            sb.append((char) b);
                        }
                    }
                    sb.append('"');
                    continue;
                }
            }
            sb.append(intStack[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    private int depth;
    private int[] intStack;

    // If an element is non-null, the corresponding intStack element
    // is ignored.
    private byte[][] stringStack;

    private void checkNotEmpty() {
        if (depth == 0)
            throw new IllegalStateException("Stack is empty");
    }
}
