package enyo.lang;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A combination of a method name with its type.
 *
 * The named method type is not associated with a specific class.
 *
 * <p>At the virtual machine level, two methods are distinct if their
 * name and method types are distinct.  Equality and hash code of this
 * class reflects that.
 *
 * @see Method
 * @see MethodType
 */
public record NamedMethodType(String name, MethodType type) {
    /**
     * Creates a new combination of a method name and its type.
     * @param name the name of the method
     * @param type the type of the method
     * @throws IllegalArgumentException if {@code name} is not a valid
     *   method name
     * @throws NullPointerException if any argument is null
     */
    public NamedMethodType {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        if (name.isEmpty())
            throw new IllegalArgumentException("Empty method name");
    }

    /**
     * Creates a named method type from a reflected method.
     * The name, return type, and the parameter list of the method are used.
     * @param method the reflected method from which name and parameter types
     *   are extracted
     * @throws NullPointerException if the argument is null
     * @see Method#getName()
     * @see Method#getReturnType()
     * @see Method#getParameterTypes()
     */
    public NamedMethodType(Method method) {
        this(method.getName(),
            MethodType.methodType(
                method.getReturnType(),
                method.getParameterTypes()));
    }

    /**
     * Returns a method handle for this named method in the specified class.
     * The target method is a virtual method.
     * @param lookup used to find the method
     * @param cls the class in which the method is looked up
     * @return a method handle
     * @throws NullPointerException if any argument is null
     * @throws NoSuchMethodException if the lookup fails
     * @throws IllegalAccessException if the lookup fails
     * @see MethodHandles.Lookup#findVirtual(Class,String,MethodType)
     */
    public MethodHandle findVirtual(MethodHandles.Lookup lookup,
        Class<?> cls) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(cls, name(), type());
    }

    /**
     * Returns a method handle for this named method in the specified class.
     * The target method is a static method.
     * @param lookup used to find the method
     * @param cls the class in which the method is looked up
     * @return a method handle
     * @throws NullPointerException if any argument is null
     * @throws NoSuchMethodException if the lookup fails
     * @throws IllegalAccessException if the lookup fails
     * @see MethodHandles.Lookup#findStatic(Class,String,MethodType)
     */
    public MethodHandle findStatic(MethodHandles.Lookup lookup,
        Class<?> cls) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findStatic(cls, name(), type());
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(type().returnType().getName());
        sb.append(' ');
        sb.append(name());
        sb.append('(');
        boolean first = true;
        for (var cls : type().parameterList()) {
            if (first)
                first = false;
            else
                sb.append(',');
            sb.append(cls.getName());
        }
        sb.append(')');
        return sb.toString();
    }
}
