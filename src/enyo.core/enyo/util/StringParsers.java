package enyo.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;

/**
 * Factories for string parsers.
 *
 * The string parsers created by this class are expected to be used in
 * meta-programming contexts.
 *
 * <p>The string parsers follow the model of {@link Double#parseDouble(String)}.
 * They expect a single {@link String} argument, and return an object of the
 * appropriate type.  If the argument string is null,
 * a {@link NullPointerException} is throwb.  If the argument cannot
 * be parsed, an exception of type {@link IllegalArgumentException}
 * (or a subclass) is raised.
 */
public final class StringParsers {
    /**
     * Returns a method handle that parsers a {@link String} into an object
     * of the specified type.
     *
     * For example, to parse a string into an integer, code like this
     * can be used:
     *
     * <pre>
     * MethodHandle parser = StringParsers.parser(int.class);
     * int value = (int) parser.invokeExact(string);
     * </pre>
     *
     * <p>The caller is expected to cache and reuse the method handle.
     * @param cls the expected result type for the parser
     * @return a method handle with the return type {@code cls}
     *   and a single argument, {@link String}
     * @throws IllegalArgumentException if the argument is not of a type
     *   that can be parsed
     * @throws NullPointerException if the argument is null
     */
    public static MethodHandle parser(Class<?> cls) {
        switch (cls.getName()) {
        case "java.lang.String":
            return staticMethod(StringParsers.class, "parseString", cls);

        // Primitive types.
        case "byte":
            return staticMethod(StringParsers.class, "parseByte", cls);
        case "short":
            return staticMethod(StringParsers.class, "parseShort", cls);
        case "int":
            return staticMethod(StringParsers.class, "parseInt", cls);
        case "long":
            return staticMethod(StringParsers.class, "parseLong", cls);
        case "float":
            return staticMethod(Float.class, "parseFloat", cls);
        case "double":
            return staticMethod(Double.class, "parseDouble", cls);
        case "boolean":
            return staticMethod(StringParsers.class, "parseBoolean", cls);
        case "char":
            return staticMethod(StringParsers.class, "parseCharacter", cls);

        // Primitive type wrappers.
        case "java.lang.Byte":
            return staticMethod(StringParsers.class, "parseByte",
                byte.class, cls);
        case "java.lang.Short":
            return staticMethod(StringParsers.class, "parseShort",
                short.class, cls);
        case "java.lang.Integer":
            return staticMethod(StringParsers.class, "parseInt",
                int.class, cls);
        case "java.lang.Long":
            return staticMethod(StringParsers.class, "parseLong",
                long.class, cls);
        case "java.lang.Float":
            return staticMethod(cls, "parseFloat", float.class, cls);
        case "java.lang.Double":
            return staticMethod(cls, "parseDouble", double.class, cls);
        case "java.lang.Boolean":
            return staticMethod(StringParsers.class, "parseBoolean",
                boolean.class, cls);
        case "java.lang.Character":
            return staticMethod(StringParsers.class, "parseCharacter",
                char.class, cls);

        // Other platform types.
        case "java.math.BigInteger":
        case "java.math.BigDecimal":
            return constructor(cls);
        }

        if (cls.isEnum())
            return forEnum(cls);

        throw new IllegalArgumentException(cls.toString());
    }

    /**
     * Returns the argument string.
     * This function is a dummy parser for strings.
     * @param s the string to return
     * @return the passed string
     * @throws NullPointerException if the argument is null
     */
    public static String parseString(String s) {
        return Objects.requireNonNull(s);
    }

    /**
     * Returns the character in a single-character string.
     * @param s the string to parse
     * @return the character
     * @throws IllegalArgumentException
     *   if the {@linkplain String#length() string length} is not 1
     * @throws NullPointerException if the string argument is null
     */
    public static char parseCharacter(String s) {
        if (s.length() != 1)
            throw new IllegalArgumentException(
                "Invalid string for character: " + s);
        return s.charAt(0);
    }

    /**
     * Parses a string into a {@code byte}.
     * @param s the string to parse
     * @return the parsed byte
     * @throws IllegalArgumentException if the string is not a valid number
     * @throws NullPointerException if the string argument is null
     * @see Byte#parseByte(String)
     */
    public static byte parseByte(String s) {
        return Byte.parseByte(Objects.requireNonNull(s));
    }

    /**
     * Parses a string into a {@code short}.
     * @param s the string to parse
     * @return the parsed {@code short} value
     * @throws IllegalArgumentException if the string is not a valid number
     * @throws NullPointerException if the string argument is null
     * @see Short#parseShort(String)
     */
    public static short parseShort(String s) {
        return Short.parseShort(Objects.requireNonNull(s));
    }

    /**
     * Parses a string into an {@code int}.
     * @param s the string to parse
     * @return the parsed integer
     * @throws IllegalArgumentException if the string is not a valid number
     * @throws NullPointerException if the string argument is null
     * @see Integer#parseInt(String)
     */
    public static int parseInt(String s) {
        return Integer.parseInt(Objects.requireNonNull(s));
    }

    /**
     * Parses a string into a {@code long}.
     * @param s the string to parse
     * @return the parsed {@code long} value
     * @throws IllegalArgumentException if the string is not a valid number
     * @throws NullPointerException if the string argument is null
     * @see Long#parseLong(String)
     */
    public static long parseLong(String s) {
        return Long.parseLong(Objects.requireNonNull(s));
    }

    /**
     * Parses a string into a {@code boolean}.
     * @param s the string to parse
     * @return the boolean value
     * @throws IllegalArgumentException if the string is neither
     *   {@code "true"} nor {@code "false"}
     * @throws NullPointerException if the string argument is null
     * @see Long#parseLong(String)
     */
    public static boolean parseBoolean(String s) {
        Objects.requireNonNull(s);
        if (s.equals("true"))
            return true;
        else if (s.equals("false"))
            return false;
        else
            throw new IllegalArgumentException("Invalid boolean: " + s);
    }

    /**
     * Procudes a method handle that constructs a new object from the
     * results of multiple fetcher calls and string parsing.
     *
     * The resulting method handle expects a single argument.
     * For each argument in {@code constructor}, {@code fetcher}
     * is applied to that single argument, and the result is converted
     * to the type expected by the respective {@code constructor}
     * argument using a string parser (obtained by {@link #parser(Class)}).
     * Finally, {@code constructor} is invoked with the arguments so
     * converted, and this returns the value of the resulting method handle.
     *
     * <p>Or, in pseudo-code: Denote the argument of the resulting
     * method handle by {@code arg}.  Then the resulting method handle
     * evaluates
     *
     * <pre>
     * constructor(p0(fetcher(arg)), p1(fetcher(arg)), …, pN(fetcher(arg)))
     * </pre>
     * where {@code p0}, …, {@code pN} are the appropriate parsers
     * obtained by {@link #parser(Class)}.
     *
     * @param constructor the method handle that is invoked to convert
     *   multiple parsed values into a single object
     * @param fetcher the method handle called to obtain additional values;
     *   its return type must be {@link String}
     * @return a new method type, expecting a single argument, of the
     *   the same type as the {@code fetcher}'s argument, and returning
     *   the return type of {@code constructor}
     * @throws IllegalArgumentException if {@code constructor} does not
     *   accept any arguments
     * @throws IllegalArgumentException if {@code fetcher} does not
     *   accept a single {@link String} argument
     * @throws IllegalArgumentException if any of the arguments of
     *   {@code constructor} are not recognized by {@link #parser(Class)}
     */
    public static MethodHandle applyParsers(MethodHandle constructor,
        MethodHandle fetcher) {
        if (constructor.type().parameterCount() == 0)
            throw new IllegalArgumentException(
                "Constructor lacks parameters: " + constructor);
        if (fetcher.type().parameterCount() != 1)
            throw new IllegalArgumentException(
                "Fetcher argument count not 1: " + fetcher);
        if (fetcher.type().returnType() != String.class)
            throw new IllegalArgumentException(
                "Fetcher does not return String: " + fetcher);

        MethodHandle[] argumentFilters;
        {
            List<Class<?>> parameterTypes = constructor.type().parameterList();
            argumentFilters = new MethodHandle[parameterTypes.size()];
            for (int i = 0; i < argumentFilters.length; ++i) {
                MethodHandle parser;
                try {
                    parser = parser(parameterTypes.get(i));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(String.format(
                            "Parsing argument %d of %s",
                            i, constructor), e);
                }
                argumentFilters[i] =
                    MethodHandles.filterReturnValue(fetcher, parser);
            }
        }


        MethodType resultType = MethodType.methodType(
            constructor.type().returnType(), fetcher.type().parameterType(0));
        return MethodHandles.permuteArguments(
            MethodHandles.filterArguments(constructor, 0, argumentFilters),
            resultType, new int[argumentFilters.length]);
    }

    private static final MethodHandles.Lookup PUBLIC =
        MethodHandles.publicLookup();

    private StringParsers() { } // No instances.

    private static MethodHandle staticMethod(Class<?> declaringClass,
        String methodName, Class<?> returnType) {
        try {
            return PUBLIC.findStatic(declaringClass, methodName,
                MethodType.methodType(returnType, String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InternalError(e);
        }
    }

    private static MethodHandle staticMethod(Class<?> declaringClass,
        String methodName, Class<?> returnType, Class<?> boxedType) {
        try {
            return PUBLIC.findStatic(declaringClass, methodName,
                MethodType.methodType(returnType, String.class))
                .asType(MethodType.methodType(boxedType, String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InternalError(e);
        }
    }

    private static MethodHandle constructor(Class<?> declaringClass) {
        try {
            return PUBLIC.findConstructor(declaringClass,
                MethodType.methodType(void.class, String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InternalError(e);
        }
    }

    private static MethodHandle forEnum(Class<?> declaringClass) {
        MethodType typ = MethodType.methodType(declaringClass, String.class);
        try {
            try {
                return PUBLIC.findStatic(declaringClass, "valueOf", typ);
            } catch (IllegalAccessException e) {
                // Fall back to the reflection-based implementation.
                return PUBLIC.findStatic(Enum.class, "valueOf",
                    MethodType.methodType(
                        Enum.class, Class.class, String.class))
                    .bindTo(declaringClass)
                    .asType(typ);
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InternalError(e);
        }
    }
}
