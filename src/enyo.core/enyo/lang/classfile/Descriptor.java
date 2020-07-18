package enyo.lang.classfile;

import java.util.Map;

/**
 * A string denoting the type of a class member.
 *
 * @see Field
 * @see Method
 * @see Member#descriptor()
 */
public final class Descriptor implements Comparable<Descriptor> {
    /**
     * Creates a new descriptor for the passed string.
     * @param descriptor the name contained in the descriptor
     * @return a descriptor with this string
     * @throws IllegalArgumentException if the descriptor is empty
     * @throws NullPointerException if {@code descriptor} is null
     */
    public static Descriptor of(String descriptor) {
        if (descriptor.length() == 0)
            throw new IllegalArgumentException();
        return new Descriptor(descriptor);
    }

    /**
     * Returns a descriptor for the passed type.
     * @param type the type for which the descriptor should be determined
     * @return a descriptor; equivalent to {@code of(descriptor(type))}
     * @throws NullPointerException if {@code type} is null
     * @see #of(String)
     * @see #descriptor(Class)
     */
    public static Descriptor of(Class<?> type) {
        return of(descriptor(type));
    }

    /**
     * Creates a new descriptor for the passed binary name.
     * @param name the binary name of the type; for example,
     *   {@code "java/lang/Object"} for {@link Object}
     * @return a descriptor with this string
     * @throws IllegalArgumentException if the name is empty
     * @throws NullPointerException if the name is null
     */
    public static Descriptor ofBinaryName(String name) {
        return new Descriptor(descriptorForBinaryName(name));
    }

    /**
     * Returns the descriptor string for a binary type name.
     * @param name the binary name of the type; for example,
     *   {@code "java/lang/Object"} for {@link Object}
     * @return the descriptor
     * @throws IllegalArgumentException if the name is empty
     * @throws NullPointerException if the name is null
     */
    public static String descriptorForBinaryName(String name) {
        if (name.length() == 0)
            throw new IllegalArgumentException();
        return 'L' + name + ';';
    }

    /**
     * Returns the descriptor for the passed type.
     * @param type the type for which the descriptor should be determined
     * @return a descriptor; equivalent to {@code of(descriptor(type))}
     * @throws NullPointerException if {@code type} is null
     */
    public static String descriptor(Class<?> type) {
        if (type.isPrimitive())
            return PRIMITIVE_TYPES.get(type);
        else if (type.isArray())
            // Handles both primitive and reference types.
            return type.getName().replace('.', '/');
        else
            return 'L' + type.getName().replace('.', '/') + ';';
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == Descriptor.class
            && name.equals(((Descriptor) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Descriptor other) {
        return name.compareTo(other.name);
    }

    private final String name;

    private static final Map<Class<?>, String> PRIMITIVE_TYPES = Map.of(
        Byte.TYPE, "B",
        Character.TYPE, "C",
        Double.TYPE, "D",
        Float.TYPE, "F",
        Integer.TYPE, "I",
        Long.TYPE, "J",
        Short.TYPE, "S",
        Boolean.TYPE, "Z",
        Void.TYPE, "V");

    private Descriptor(String name) {
        this.name = name;
    }
}
