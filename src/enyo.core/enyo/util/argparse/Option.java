package enyo.util.argparse;

import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Description of an argument parser option.
 *
 * @param component the record field corresponding to this option
 * @param index the index in the record component array
 * @param names the computed names of this option; see
 *   {@linkplain ArgParser##naming the default naming conventions}
 *   see {@link Name}
 * @param positional if false, this is a {@code -}/{@code --} explicit
 *   option; if true, this is a positional argument;
 *   see {@link Positional}
 * @param help the help string; can be null
 * @param defaultValue the default value string; can be null
 * @param metaVar the meta variable string; can be null
 * @param required if true, this option must be present
 * @param converter the function used to convert from strings to option values;
 *   this is null for boolean fields
 * @param defaultObject the converted default value, based on the
 *   {@link Default} annotation
 */
public record Option(
    RecordComponent component,
    int index,
    List<String> names,
    boolean positional,
    String help,
    String defaultValue,
    String metaVar,
    boolean required,
    Converter converter,
    Object defaultObject
) {
    /**
     * Creates a new option object.
     * @throws NullPointerException if {@code component}, {@code names}
     *  are null
     * @throws ConfigurationException if the specified field information
     *   is inconsistent
     */
    public Option {
        Objects.requireNonNull(names, "component");
        Objects.requireNonNull(names, "names");

        if (names.isEmpty())
            throw new ConfigurationException(String.format(
                    "Field %s of %s has no option names",
                    component.getName(), component.getDeclaringRecord()));
        if ("".equals(help))
            throw new ConfigurationException(String.format(
                    "Field %s of %s has empty help string",
                    component.getName(), component.getDeclaringRecord()));

        if (positional) {
            if (component.getType() == boolean.class)
                throw new ConfigurationException(String.format(
                        "Positional argument field %s of %s has type boolean",
                        component.getName(), component.getDeclaringRecord()));
            if (names.size() > 1)
                throw new ConfigurationException(String.format(
                        "Positional argument field %s of %s for has %d names",
                        component.getName(), component.getDeclaringRecord(),
                        names.size()));
            if (names.get(0).isEmpty())
                throw new ConfigurationException(String.format(
                        "Positional argument field %s of %s has empty name",
                        component.getName(), component.getDeclaringRecord(),
                        names.get(0)));
            if (names.get(0).startsWith("-"))
                throw new ConfigurationException(String.format(
                        "Positional argument field %s of %s has name starting with '-': %s",
                        component.getName(), component.getDeclaringRecord(),
                        names.get(0)));
            if (metaVar != null)
                throw new ConfigurationException(String.format(
                        "Positional argument field %s of %s has meta-variable name: %s",
                        component.getName(), component.getDeclaringRecord(),
                        metaVar));

        } else // !positional
            for (String name : names) {
                if (name.isEmpty())
                    throw new ConfigurationException(String.format(
                        "Option field %s of %s has has empty name",
                        component.getName(), component.getDeclaringRecord()));
                if (!name.startsWith("-"))
                    throw new ConfigurationException(String.format(
                        "Option field %s of %s has name not starting with '-': %s",
                        component.getName(), component.getDeclaringRecord(),
                        name));
            }
    }

    /**
     * Creates an option object based on a record component.
     *
     * Analyzes the field type and its annotations and performs
     * consistency checks.
     *
     * @param components the components of the record object
     * @param index the index in {@code components} for which the
     *   option object should be created
     * @param positional true if a previous object (with a lower index)
     *   had a {@link Positional} annotation
     * @return a new option object
     * @throws NullPointerException if {@code components} is null
     * @throws ConfigurationException if the specified field information
     *   is inconsistent
     */

    public static Option of(RecordComponent[] components, int index,
                            boolean positional) {
        var c = components[index];
        positional = getPositional(c, positional);
        var converter = getConverter(c);
        return new Option(
            c, index,
            getNames(c, positional),
            positional,
            getString(c, Help.class, Help::value),
            getString(c, Default.class, Default::value),
            getString(c, MetaVar.class, MetaVar::value),
            isRequired(c),
            converter,
            getDefaultObject(c, converter));
    }

    /**
     * The first specified name of an option.
     * @return the primary name, prefixed with {@code "-"} for
     *   single-letter options and {@code "--"} for long options
     */
    public String primaryName() {
        return names.get(0);
    }

    /**
     * A descriptive name for this option.
     * @return {@code "argument"} or {@code "option"}
     *   followed by the {@linkplain #primaryName() primary name}
     */
    public String descriptor() {
        if (positional)
            return "argument " + primaryName();
        else
            return "option " + primaryName();
    }

    /**
     * Returns whether this option needs an additional argument value.
     * @return true if this option is of type boolean, false otherwise
     */
    public boolean needsValue() {
        return component.getType() != boolean.class;
    }

    private static List<String> getNames(
        RecordComponent c, boolean positional) {
        var names = c.getDeclaredAnnotationsByType(Name.class);
        if (names.length > 0)
            return Arrays.stream(names)
                .map(Name::value)
                .toList();
        // If there is no @Name annotation, compute a default name.
        // See ArgParser for the default naming rules.
        String name = c.getName();
        if (name.length() > 1 && name.charAt(name.length() - 1) == '_')
            // Remove trailing '_'.
            name = name.substring(0, name.length() - 1);
        if (name.length() > 1 && name.indexOf('_') >= 0) {
            name = name.replace('_', '-');
            if (!positional)
                name = "--" + name;
        } else if (!Character.isUpperCase(name.charAt(0))) {
            var sb = new StringBuilder(2 + name.length());
            if (!positional)
                sb.append("--");
            boolean lastUpper = false;
            for (int i = 0; i < name.length(); ++i) {
                char ch = name.charAt(i);
                if (!lastUpper && Character.isUpperCase(ch))
                    sb.append('-');
                sb.append(Character.toLowerCase(ch));
            }
            name = sb.toString();
        }
        return List.of(name);
    }

    private static boolean getPositional(
        RecordComponent c, boolean positional) {
        return positional || c.getDeclaredAnnotation(Positional.class) != null;
    }

    private static <T extends Annotation> String getString(
        RecordComponent c, Class<T> ann, Function<T, String> getter) {
        var v = c.getDeclaredAnnotation(ann);
        if (v == null)
            return null;
        return getter.apply(v);
    }

    private static boolean isRequired(RecordComponent c) {
        if (c.getAnnotation(Required.class) != null)
            return true;
        return c.getType().isPrimitive() && c.getType() != boolean.class;
    }

    private static Converter getConverter(RecordComponent c) {
        // Update the ArgParser doctext in case more types are added here.
        switch (c.getType().getName()) {
        case "boolean":
            return null;
        case "int":
        case "java.lang.Integer":
            return Integer::decode;
        case "long":
        case "java.lang.Long":
            return Long::decode;
        case "float":
        case "java.lang.Float":
            return Float::parseFloat;
        case "double":
        case "java.lang.Double":
            return Double::parseDouble;
        case "java.lang.String":
            return String::toString;
        }
        throw new ConfigurationException(String.format(
                "Field %s of %s has unsupported type %s",
                c.getName(), c.getDeclaringRecord(), c.getGenericType()));
    }

    private static Object getDefaultObject(
        RecordComponent c, Converter converter) {
        Default def = c.getAnnotation(Default.class);
        if (def == null) {
            // Special case for boolean fields: they default to false.
            if (c.getType() == boolean.class)
                return false;
            else
                return null;
        }
        try {
            return converter.apply(def.value());
        } catch (RuntimeException e) {
            throw new ConfigurationException(String.format(
                    "Field %s of %s has invalid default value: %s",
                    c.getName(), c.getDeclaringRecord(), def.value()), e);
        }
    }
}
