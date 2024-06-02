package enyo.util;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.LongFunction;

/**
 * Helper interfacefor creating enumerations that may cover the {@code long}
 * value range.
 *
 * <p>Actual {@code OpenEnum} classes are expected to be records, with
 * just one {@code value} field of type {@code long}.
 * {@snippet lang="java" :
 * public static record ElfType(long value) implements OpenEnum {
 *     // Restrictions on value ranges can be encorced in the
 *     // constructor.
 *     public ElfType {
 *         if (value < 0 || value > 65535)
 *             throw new IllegalArgumentException("value: " + value);
 *     }
 *
 *     // Creation of new instances should wrap the get(…) methods.
 *
 *     public static ElfType get(int value) {
 *         return OpenEnum.get(ElfType.class, value, ElfType::new);
 *     }
 *
 *     public static ElfType get(String name) {
 *         return OpenEnum.get(ElfType.class, name, ElfType::new);
 *     }
 *
 *     // Well-known constants can be declared as static final fields.
 *     public static final ElfType ET_NONE = new ElfType(0);
 *     public static final ElfType ET_REL = new ElfType(1);
 *     public static final ElfType ET_EXEC = new ElfType(2);
 *     public static final ElfType ET_DYN = new ElfType(3);
 *     public static final ElfType ET_CORE = new ElfType(4);
 *
 *     // The names of these fields need to be registered.
 *     static {
 *         OpenEnum.builder(ElfType.class)
 *             .add("ET_NONE", ET_NONE)
 *             .add("ET_REL", ET_REL)
 *             .add("ET_EXEC", ET_EXEC)
 *             .add("ET_DYN", ET_DYN)
 *             .add("ET_CORE", ET_CORE)
 *             .finish();
 *     }
 *
 *    // The toString() method should be implemented using name().
 *     @Override
 *     public String toString() {
 *         return name();
 *     }
 * }
*  }
 */
public interface OpenEnum {
    /**
     * Returns the underlying value of this enumeration constant.
     * @return the value
     */
    long value();

    /**
     * Returns the name of this enumeration constant.
     * @return the name if available, otherwise the constant value as
     *   a decimal number
     */
    default String name() {
        return valueToName(getClass(), value());
    }

    /**
     * Returns an {@code OpenEnum} instance for the specified value.
     * @param <T> the type of the instance
     * @param cls the reified type of the instance
     * @param value the value of the {@code OpenEnum} constant
     * @param constructor the constructor for building unnamed constants
     * @return an {@code OpenEnum} constant {@code e} such that
     *   {@code e.value() == value}
     * @throws NullPointerException if {@code cls} or {@code constructor}
     *   are null
     */
    static <T extends OpenEnum> T get(Class<T> cls, long value,
        LongFunction<T> constructor) {
        var info = OpenEnumInfo.get(cls);
        return get0(info, value, constructor);
    }

    private static <T extends OpenEnum> T get0(OpenEnumInfo info, long value,
        LongFunction<T> constructor) {
        var result = info.fromValue(value);
        if (result != null) {
            @SuppressWarnings({"unchecked"})
            T result1 = (T) result;
            return result1;
        } else
            return constructor.apply(value);
    }

    /**
     * Returns an {@code OpenEnum} instance for the specified name.
     * @param <T> the type of the instance
     * @param cls the reified type of the instance
     * @param name the name of the {@code OpenEnum} constant
     * @param constructor the constructor for building unnamed constants
     * @return an {@code OpenEnum} constant {@code e} such that
     *   {@code e.value() == value}
     * @throws IllegalArgumentException if {@code name} is not
     *   a valud name for {@code cls}, or if it is not a valid
     *   integer literal for {@code cls}
     * @throws NullPointerException if any arguments are null
     */
    static <T extends OpenEnum> T get(Class<T> cls, String name,
        LongFunction<T> constructor) {
        var info = OpenEnumInfo.get(cls);
        var result = info.fromName(name);
        if (result != null) {
            @SuppressWarnings({"unchecked"})
            T result1 = (T) result;
            return result1;
        } else {
            long value;
            try {
                if (name.startsWith("0x") || name.startsWith("0X"))
                    if (name.length() > 2 && name.charAt(2) != '+')
                        value = Long.parseUnsignedLong(
                            name, 2, name.length(), 16);
                    else
                        throw new IllegalArgumentException("name: " + name);
                else
                    value = Long.parseLong(name);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("name: " + name, e);
            }
            return get0(info, value, constructor);
        }
    }

    private static String valueToName(Class<? extends OpenEnum> cls,
        long value) {
        var info = OpenEnumInfo.get(cls);
        return info.valueToName(value);
    }

    /**
     * Creates a new builder for constructing the {@code OpenEnum} metadata.
     * @param <T> the type of the {@link OpenEnum} class being populated
     * @param cls the type of the {@link OpenEnum} class being populated
     * @return a new builder object
     * @see Builder#add(String,OpenEnum)
     */
    static <T extends OpenEnum> Builder<T> builder(Class<T> cls) {
        return new Builder<>(Objects.requireNonNull(cls));
    }

    /**
     * Helper class for constructing {@link OpenEnum} classes.
     *
     * <p>All named constants need be registered using
     * {@link #add(String,OpenEnum)}, and then {@link #finish()} needs
     * to be called.  This should happen inside a {@code static} block
     * of the {@link OpenEnum} class:
     * {@snippet lang="java" :
     * static {
     *     OpenEnum.builder(ElfType.class)
     *         .add("ET_NONE", ET_NONE)
     *         .add("ET_REL", ET_REL)
     *         .add("ET_EXEC", ET_EXEC)
     *         .add("ET_DYN", ET_DYN)
     *         .add("ET_CORE", ET_CORE)
     *         .finish();
     * }
     * }
     * @param <T> the {@link OpenEnum} class being populated
     */
    public class Builder<T extends OpenEnum> {
        private Builder(Class<T> cls) {
            this.cls = cls;
        }

        private Class<T> cls;
        private final HashMap<String, T> nameToEnum = new HashMap<>();
        private final HashMap<Long, String> valueToName = new HashMap<>();

        /**
         * Records a new name/enumeration constant combination.
         * @param name the name of the {@link OpenEnum} constant being
         *   registered
         * @param enumObject the enum constant
         * @throws IllegalStateException if the new combination
         *   is inconsistent with previous combinations, or if
         *   {@link OpenEnum} has already been registered
         * @return this object
         * @throws NullPointerException if any arguments are null
         */
        public Builder<T> add(String name, T enumObject) {
            if (cls == null)
                throw new IllegalStateException("Class already registered");
            var previous = nameToEnum.put(name, enumObject);
            if (previous != null)
                throw new IllegalStateException(String.format(
                        "Conflicting values for \"%s\": %d, %d",
                        name, previous.value(), enumObject.value()));
            var previousName = valueToName.put(enumObject.value(), name);
            if (previousName != null)
                throw new IllegalStateException(String.format(
                        "Conflicting names for %d: \"%s\", \"%s\"",
                        enumObject.value(), previousName, name));
            return this;
        }

        /**
         * Completes the registration of the {@link OpenEnum} class.
         * @throws IllegalStateException if the {@link OpenEnum} class has
         *   already been registered.
         */
        public void finish() {
            OpenEnumInfo.install(cls, nameToEnum, valueToName);
            cls = null;
        }
    }
}
