package enyo.util;

import java.util.HashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

/**
 * Base interface for types which implement open enum types.
 *
 * <p>To use this facility, define an interface type that extends
 * {@link OfInt} and has an enum member, customarily called {@code E}.
 * Then define a static final member (called {@code T}) using
 * {@link #intType(MethodHandles.Lookup,Class,Class)} and related
 * methods.  Enum
 * instances can then be obtained using {@link OfInt.Type#ofInt(int)}
 * or {@link OfInt.Type#of(String)}.
 *
 * <h2 id="example">Example</h2>
 *
 * This code fragment creates an open enumeration type for ELF type
 * constants.  These constants are 16 bits wide, so the validating
 * expression checks for this range.
 *
 * {@snippet lang="java" :
 * public interface ElfType extends OpenEnum.OfInt {
 *     enum E implements ElfType, OpenEnum.OfInt.Direct {
 *         ET_NONE,
 *         ET_REL,
 *         ET_EXEC,
 *         ET_DYN,
 *         ET_CORE,
 *     }
 *     static final OpenEnum.OfInt.Type<ElfType> T =
 *         OpenEnum.intType(MethodHandles.lookup(), ElfType.class, E.class,
 *             i -> i >= 0 && i <= 65535);
 * }
 * }
 */
public interface OpenEnum {
    /**
     * Returns the underlying value of this enumeration constant.
     * @return the value of this enumeration constant, as a
     * {@link Long long}.
     * @see OfInt#value()
     */
    long longValue();

    /**
     * Returns the name of this enumeration constant.
     * @return the name if available, otherwise null
     */
    String name();

    /**
     * Returns the name of value of this enumeration constant.
     * @return the {@linkplain #name() name} if not null, otherwise
     *   the value formatted as a signed decimal number
     */
    String toString();

    /**
     * Open enum type that supports values in the {@link Integer int} range.
     * @see #intType(MethodHandles.Lookup,Class,Class)
     * @see #intType(MethodHandles.Lookup,Class,Class,IntConsumer)
     * @see #predicatedIntType(MethodHandles.Lookup,Class,Class,IntPredicate)
     */
    interface OfInt extends OpenEnum {
        /**
         * Returns the value of this enum constant.
         * @return this constant's value
         */
        int value();

        /**
         * Returns the value of this enum constant as {@link Long long}.
         * The default implementation simply calls {@code value()}.
         * @return this constant's value
         * @see #value()
         */
        default long longValue() {
            return value();
        }

        /**
         * Mix-in interface to implement direct {@link OfInt} mapping for
         * {@linkplain Enum enums}.
         */
        interface Direct extends OpenEnum.OfInt {
            /**
             * Returns the enum constant ordinal.
             * Ordinals always start with zero and are contiguous.
             * This method is implemented by the @code{enum} types.
             * @return the enumeration ordinal value
             * @see Enum#ordinal()
             */
            int ordinal();

            /**
             * Returns the value of this enum constant.
             * This default implementation calls #ordinal().
             * @return this constant's value
             */
            default int value() {
                return ordinal();
            }

            /**
             * Returns the value of this enum constant as a
             * {@link Long long}.
             * This default implementation calls #ordinal().
             * @return this constant's value
             */
            default long longValue() {
                return ordinal();
            }
        }

        /**
         * Metatype class for describing properties of {@code int}-backed
         * open enum types.
         *
         * <p>Instances of this class should be created with one of the
         * {@code intType()} methods in {@link OpenEnum} and assigned
         * to a public static final member {@code T} in the corresponding
         * interface  type.
         *
         * @param <T> the type of open enumeration objects produced
         * @see #intType(MethodHandles.Lookup,Class,Class)
         * @see #intType(MethodHandles.Lookup,Class,Class,IntConsumer)
         * @see #predicatedIntType(MethodHandles.Lookup,Class,Class,IntPredicate)
         */
        class Type<T extends OpenEnum.OfInt> {
            private final Enum<?>[] constants;
            private final int[] values;
            private final Map<String, T> names;
            private final MethodHandle constructor;
            private final IntConsumer validator;
            private final Class<? extends T> baseType;
            private final Class<? extends T> enumType;
            private final Class<? extends T> openType;

            private Type(MethodHandles.Lookup lookup,
                Class<T> interfaceType, Class<? extends Enum<?>> enumType,
                         IntConsumer validator) {
                if (lookup.lookupClass() != interfaceType)
                    throw new IllegalArgumentException(String.format(
                            "Type for %s instantiated from %s",
                            interfaceType.getName(),
                            lookup.lookupClass().getName()));
                if (!interfaceType.isInterface())
                    throw new IllegalArgumentException(
                        "Not an interface: " + enumType);
                if (!enumType.isEnum())
                    throw new IllegalArgumentException(
                        "Not an enum: " + enumType);
                if (!OfInt.class.isAssignableFrom(interfaceType))
                    throw new IllegalArgumentException(String.format(
                            "Interface %s does not extend %s",
                            interfaceType.getName(),
                            OfInt.class.getName()));
                if (!interfaceType.isAssignableFrom(enumType))
                    throw new IllegalArgumentException(String.format(
                            "Cannot assign %s to %s",
                            enumType.getName(), interfaceType.getName()));

                this.constants = enumType.getEnumConstants();
                Arrays.sort(constants, (a, b) ->
                    Long.compare(((OfInt) a).value(),
                        ((OfInt) b).longValue()));
                this.values = new int[constants.length];
                var names = new HashMap<String, T>();
                for (int i = 0; i < constants.length; ++i) {
                    var e0 = constants[i];
                    var e = (OpenEnum.OfInt) e0;
                    var value = e.value();
                    values[i] = value;
                    names.put(e0.name(), interfaceType.cast(e0));
                }
                this.names = Map.copyOf(names);

                // Check for duplicate values.
                if (values.length > 0) {
                    int previous = values[0];
                    for (int i = 1; i < values.length; ++i) {
                        int current = values[i];
                        if (current == previous)
                            throw new IllegalArgumentException(String.format(
                                    "Enum constants %s and %s have same value %d",
                                    constants[i - 1].name(),
                                    constants[i].name(),
                                    current));
                        previous = current;
                    }
                }

                this.constructor = OpenEnumImpl.constructor(
                    lookup, interfaceType, int.class, Integer.class);
                this.validator = validator;

                this.baseType = interfaceType;
                {
                    @SuppressWarnings("unchecked")
                    var cls = (Class<? extends T>) enumType;
                    this.enumType = cls;
                }
                {
                    @SuppressWarnings("unchecked")
                    var cls = (Class<? extends T>)
                        constructor.type().returnType();
                    this.openType = cls;
                }
            }

            /**
             * Returns the base interface type specified when creating
             * this metatype object.
             * @return an interface type
             * @see #enumType()
             * @see #openType()
             */
            public Class<? extends T> baseType() {
                return baseType;
            }

            /**
             * Returns the type of the proper enum constants.
             * @return the class of a enum type
             * @see #baseType()
             */
            public Class<? extends T> enumType() {
                return enumType;
            }

            /**
             * Returns the type used for objects that do not correspond
             * to proper enum constants.
             * <p>Note that is may be a hidden class.
             * Use {@link baseType()} to obtain symbolic references.
             * @return the class of the enum instances that do not
             *   have proper constants in the {@link #enumType()} enum type
             * @see #baseType()
             */
            public Class<? extends T> openType() {
                return openType;
            }

            /**
             * Returns an enumeration constant for a specific integer
             * value.
             *
             * <p>If there is a matching proper enum constant in the
             * enum type specified when this metatype object was
             * instantiated, this enum constant will be returned.
             *
             * @param value the value of the enumeration constant
             * @return an enumeration value {@code e}
             *   such that {@code e.value() == value}
             * @throws IllegalArgumentException
             *   if {@linkplain #predicatedIntType(MethodHandles.Lookup,Class,Class,IntPredicate) validation}
             *    fails
             * @throws RuntimeException if
             *   {@linkplain #intType(MethodHandles.Lookup,Class,Class,IntConsumer) consumer-based validation}
             *    fails
             */
            public T ofInt(int value) {
                int idx = Arrays.binarySearch(values, value);
                if (idx >= 0) {
                    @SuppressWarnings("unchecked")
                    var t = (T) constants[idx];
                    return t;
                } else {
                    if (validator != null)
                        validator.accept(value);
                    Object obj;
                    try {
                        obj = constructor.invoke(value);
                    } catch (RuntimeException | Error e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new InternalError(e);
                    }
                    @SuppressWarnings("unchecked")
                    var t = (T) obj;
                    return t;
                }
            }

            /**
             * Returns an enumeration constant for a specific string.
             *
             * <p>If the string is the name of a constant for the enum
             * type that was specified when this metatype object was
             * instantiated, this constant is returned.  Otherwise,
             * the argument is parsed as a signed decimal integer and
             * converted using {@link #ofInt(int)} (which may produce
             * produce a proper enum constant as well).
             * @param value an enum constant name, or an integer formatted
             *   as a string
             * @return a matching enumeration value
             * @throws NullPointerException if {@code value} is null
             * @throws IllegalArgumentException if the argument {@code value}
             *   does not name a proper enum constant and cannot be parsed
             *   as an integer
             * @throws RuntimeException if validation in {@link #ofInt(int)}
             *   fails for the specified integer value
             */
            public T of(String value) {
                T t = names.get(value);
                if (t != null)
                    return t;
                return ofInt(Integer.parseInt(value));
            }

            /**
             * Returns the name for an enumeration value, if available.
             * <p>The returned string is equal to {@code get(int).name()}.
             * @param value the enumeration constant value
             * @return the enum constant name, or {@code null} if the
             *   value is unnamed
             * @throws RuntimeException if validation fails
             * @see #toString(int)
             */
            public String name(int value) {
                if (validator != null)
                    validator.accept(value);
                int idx = Arrays.binarySearch(values, value);
                if (idx < 0)
                    return null;
                return constants[idx].name();
            }

            /**
             * Returns the name for an enumeration, or the number formatted
             * as a signed decimal integer.
             * <p>The returned string is equal to
             * {@code ofInt(value).toString()}.
             * Unlike {@link #name()}, the return value is never null.
             * @param value the enumeration constant value
             * @return the enum constant name, or a formatted decimal integer
             *  if there is no name for {@code value}
             * @throws RuntimeException if validation fails
             * @see #name(int)
             */
            public String toString(int value) {
                String name = name(value);
                if (name != null)
                    return name;
                else
                    return Integer.toString(value);
            }

            /**
             * Compares the {@linkplain #value() integer values} of the
             * enum values.
             * @param a the first enum value to compare
             * @param b the second enum value to compare
             * @return -1 if {@code a} is less than {@code b},
             *   0 if they are equal, 1 otherwise
             */
            public int compare(T a, T b) {
                return Integer.compare(a.value(), b.value());
            }

            /**
             * Returns a comparator for objects of this enumeration type.
             *
             * @return a comparator that orders enumeration values by their
             *   {@linkplain #value() value}
             */
            public Comparator<T> comparator() {
                @SuppressWarnings("unchecked")
                var c = (Comparator<T>) COMPARATOR;
                return c;
            }

            private static final Comparator<OfInt> COMPARATOR = (a, b) ->
                Integer.compare(a.value(), b.value());
        }
    }

    /**
     * Creates a metatype object for an {@link Integer int}-based
     * open enumeration type.
     *
     * <p>All {@code int} values are considered valid for this type,
     * and can be created using {@link OfInt.Type#ofInt(int)}.
     *
     * @param <T> the type of the interface
     * @param lookup the lookup context of the caller
     * @param interfaceType the interface type that enumeration instances
     *   implement
     * @param enumType the associated {@link Enum enum} type
     * @return a new metatype object for the enumeration type
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code enumType} is not an
     *   {@link Enum enum}
     * @throws IllegalArgumentException if {@code enumType}
     *   does not extend {@code interfaceType}
     * @throws IllegalArgumentException if the {@link OfInt#value()}
     *   method on enum constants does not return distinct values
     * @see #intType(MethodHandles.Lookup,Class,Class,IntConsumer)
     * @see #predicatedIntType(MethodHandles.Lookup,Class,Class,IntPredicate)
     */
    static <T extends OpenEnum.OfInt> OfInt.Type<T>
        intType(MethodHandles.Lookup lookup, Class<T> interfaceType,
            Class<? extends Enum<?>> enumType) {
        return new OfInt.Type<>(lookup, interfaceType, enumType, null);
    }

    /**
     * Creates a metatype object for an {@link Integer int}-based
     * open enumeration type.
     *
     * <p>Validation may reject some integer constant values as
     * invalid for this open enumeration type.
     *
     * @param <T> the type of the interface
     * @param lookup the lookup context of the caller
     * @param interfaceType the interface type that enumeration instances
     *   implement
     * @param enumType the associated {@link Enum enum} type
     * @param validator validator for {@link Integer int} values;
     *   expected to throw a {@linkplain RuntimeException run-time exception}
     *   if validation fails
     * @return a new metatype object for the enumeration type
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if type validation fails
     *   (see {@link intType(MethodHandles.Lookup,Class,Class)})
     * @see #intType(MethodHandles.Lookup,Class,Class)
     * @see OfInt
     * @see OfInt.Type#ofInt(int)
     */
    static <T extends OpenEnum.OfInt> OfInt.Type<T>
        intType(MethodHandles.Lookup lookup, Class<T> interfaceType,
            Class<? extends Enum<?>> enumType, IntConsumer validator) {
        Objects.requireNonNull(validator, "validator");
        return new OfInt.Type<>(lookup, interfaceType, enumType, validator);
    }

    /**
     * Creates a metatype object for an {@link Integer int}-based
     * open enumeration type.
     *
     * <p>Validation may reject some integer constant values as
     * invalid for this open enumeration type.
     *
     * @param <T> the type of the interface
     * @param lookup the lookup context of the caller
     * @param interfaceType the interface type that enumeration instances
     *   implement
     * @param enumType the associated {@link Enum enum} type
     * @param validator predicate checking that the value should
     *  be admitted for this enumeration type;
     *  {@link IllegalArgumentException} is thrown if the predicate returns
     *  false
     * @return a new metatype object for the enumeration type
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if type validation fails
     *   (see {@link intType(MethodHandles.Lookup,Class,Class)})
     * @see #intType(MethodHandles.Lookup,Class,Class)
     * @see OfInt
     * @see OfInt.Type#ofInt(int)
     */
    static <T extends OpenEnum.OfInt> OfInt.Type<T>
        predicatedIntType(MethodHandles.Lookup lookup, Class<T> interfaceType,
            Class<? extends Enum<?>> enumType, IntPredicate validator) {
        Objects.requireNonNull(validator, "validator");
        return new OfInt.Type<>(lookup, interfaceType, enumType,
            i -> {
                if (!validator.test(i))
                    throw new IllegalArgumentException(
                        "Invalid enum value: " + i);
        });
    }
}
