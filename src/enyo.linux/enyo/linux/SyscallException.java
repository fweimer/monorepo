package enyo.linux;

import enyo.lang.UncheckedIllegalAccessException;

import java.lang.foreign.MemorySegment;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * The exception captures the system call number and arguments.
 */
public final class SyscallException extends RuntimeException {
    /**
     * Returns true if the raw system call result indicates an error.
     * @param result the system call result to check
     * @return true if the number is in the in-band {@code errno} range,
     *   false if not
     */
    public static boolean isError(long result) {
        return result < 0 && result >= -4095;
    }

    @Override
    public String getMessage() {
        var sb = new StringBuilder();
        if (errno != 0)
            sb.append(toString(Errno.class, errnoToStringCache, errno));
        if (syscall != 0) {
            if (!sb.isEmpty())
                sb.append(' ');
            sb.append(toString(
                SyscallConstants.class, syscallToStringCache, syscall));
        }
        if (!arguments.isEmpty()) {
            if (!sb.isEmpty())
                sb.append(' ');
            sb.append(arguments.toString());
        }
        if (!buffers.isEmpty()) {
            if (!sb.isEmpty())
                sb.append(' ');
            sb.append(buffers.toString());
        }
        return sb.toString();
    }

    SyscallException(int syscall, List<Long> arguments,
        List<MemorySegment> buffers, long syscallResult) {
        this.syscall = syscall;
        this.errno = -(int) syscallResult;
        this.arguments = List.copyOf(arguments);
        this.buffers = List.copyOf(buffers);
    }

    private static final AtomicReference<Map<Integer, String>>
        errnoToStringCache = new AtomicReference<>();

    private static final AtomicReference<Map<Integer, String>>
        syscallToStringCache = new AtomicReference<>();

    // Resolve the value to the lexicographically first name, based on
    // the non-private static final int fields in the specified class.
    private static String toString(Class<?> cls,
        AtomicReference<Map<Integer, String>> cache, int value) {
        var map = cache.getAcquire();
        if (map == null) {
            map = Arrays.stream(cls.getDeclaredFields())
            .filter(f -> Modifier.isStatic(f.getModifiers())
                         && Modifier.isFinal(f.getModifiers())
                         && !Modifier.isPrivate(f.getModifiers())
                         && f.getType() == int.class)
                .sorted(Comparator.comparing(Field::getName))
            .collect(Collectors.toUnmodifiableMap(
                f -> {
                    try {
                        return (Integer) f.get(cls);
                    } catch (IllegalAccessException e) {
                        throw new UncheckedIllegalAccessException(e);
                    }
                },
                f -> f.getName(),
                // Resolve the EDEADLK/EDEADLOCK conflict in favor of
                // EDEADLK.
                (first, later) -> first));
            var witness = cache.compareAndExchange(null, map);
            if (witness != null)
                map = witness;
        }
        String name = map.get(value);
        if (name == null)
            name = Integer.toString(value);
        return name;
    }

    /**
     * The triggering syscall, or 0 if unknown.
     */
    private final int syscall;

    /**
     * The Linux error code.
     */
    private final int errno;

    /**
     * The captured system call arguments (excluding addresses).
     */
    @SuppressWarnings("serial")
    private final List<Long> arguments;

    private final transient List<MemorySegment> buffers;

    private static final long serialVersionUID = 0x16f396e2c952ea5L;
}
