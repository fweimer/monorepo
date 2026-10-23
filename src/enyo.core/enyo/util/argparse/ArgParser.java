package enyo.util.argparse;

import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.function.Function;

import enyo.lang.SystemExit;

/**
 * A parser for command lines that produces a record.
 *
 * The available options and command line arguments are defined in the
 * form of a {@linkplain Record record type}.  Both short ({@code -v})
 * and long ({@code --verbose}) option names are supported.
 *
 * <p>Record components correspond to options, and through annotations,
 * it is possible to configure {@linkplain Name option names},
 * {@linkplain Default default values}, and whether options are
 * {@linkplain Required required}.  The first record component with a
 * {@link Positional} annotation starts the positional arguments.
 * {@linkplain Help Help messages} and {@linkplain MetaVar meta-variables}
 * associates with options can be specified.
 *
 * <p>After constructing a parser object using
 * {@link ArgParser(MethodHandles.Lookup,Class)}, the same parser
 * object can be reused multiple times.  The {@link #parse(String[])}
 * method can be used to obtain the parse result as newly allocated record
 * object.
 *
 * <p>For convenience in main programs, the {@link #run(Class,String[])}
 * method can be used. It automatically reports errors to {@link System#err}
 * and help messages to {@link System#out}, and terminates the program
 * (via {@link SystemExit}) if the argument list cannot be parsed.
 * Otherwise, a new record is returned.
 *
 * <h2>Supported component types</h2>
 *
 * <ul>
 * <li>{@code boolean} (a fla without an optio nvalue, not supported
 *   for positional arguments)
 * <li>{@code int} (implicitly required), {@link Integer} (may be optional)
 * <li>{@code long} (implicitly required), {@link Long} (may be optional)
 * <li>{@code float} (implicitly required), {@link Float} (may be optional)
 * <li>{@code double} (implicitly required), {@link Double} (may be optional)
 * <li>{@link String}
 * </ul>
 *
 * <h2 id="naming">Default naming rules for option fields</h2>
 *
 * If no {@link Name} annotation is supplied, long option names are
 * derived from record names in the following way:
 *
 * <ul>
 * <li>If the field name ends with {@code '_'}, it is removed.
 * <li>If the remaining field name contains any {@code '_'}, those
 *  are replaced by {@code '-'}, and the field name is used as the option name.
 * <li> Otherwise, uppercase letters are converted to lower case, and
 *  a hyphen {@code -} is inserted before each sequence of uppercase letters.
 * </ul>
 *
 * <p>For example, a field {@code class_} is populated by an option
 * {@code --class}, and a field {@code installPath} has a default option
 * name {@code --install-path}.
 *
 * <p>Outside of help messages, field names do not matter for
 * positional arguments.  Short option names (single characters) are
 * not automatically derived.
 *
 * @param <T> the record type that the argument parser produces
 */
public final class ArgParser<T extends Record> {
    /**
     * Instantiates a new argument parser for a given record type.
     * @param lookup a lookup that allows instantiating records
     *   of type {@code cls}
     * @param cls the record type this argument argument parser produces
     * @throws ConfigurationException the record class has incorrect types
     *   or annotations
     */
    public ArgParser(MethodHandles.Lookup lookup, Class<T> cls) {
        var components = cls.getRecordComponents();
        constructor = recordConstructor(lookup, cls, components);
        options = computeOptions(components);
        firstPositionalIndex = computeFirstPositionalIndex();
        defaultInitIndices = computeDefaultInitIndices();
        shortOptions = optionsMap(true, s -> s.charAt(1));
        longOptions = optionsMap(false, s -> s.substring(2));
    }

    /**
     * Parses a command line.
     *
     * The caller is responsible for informing the user about command
     * line errors. See {@link #run(Class,String[])} for an
     * alternative that terminates the program on parse failure.
     *
     * <p>Callers are expected to handle {@link HelpException}s to
     * response to requests for help via the {@code --help} option,
     * and to handle {@link ArgumentListException}, reporting any
     * errors to the user.
     *
     * @param args the argument array
     * @return a new record object with the data parsed from the command line
     * @throws NullPointerException {@code args} is null
     * @throws ArgumentListException an error occurred during parsing
     *   of the command line
     * @throws HelpException the user requested help output
     * @see #run(Class,String[])
     */
    public T parse(String[] args) {
        Object[] fields = new Object[constructor.type().parameterCount()];
        var errors = new ArrayList<String>(0);
        initFields(fields);
        process(args, fields, errors);
        if (errors.isEmpty())
            checks(args, fields, errors);
        if (!errors.isEmpty())
            throw new ArgumentListException(this, List.of(args), errors);
        return construct(fields);
    }

    private void usage(Class<?> mainClass, PrintStream out) {
        int options = 0;
        int requiredOptions = 0;
        for (Option opt : options()) {
            if (opt.positional())
                continue;
            ++options;
            if (opt.required()) {
                ++requiredOptions;
            }
        }

        out.print("usage: ");

        var module = mainClass.getModule().getName();
        if (module != null) {
            out.print(module);
            out.print('/');
        }
        out.print(mainClass.getName());

        // Indicator for options.
        if (options > 0) {
            if (requiredOptions > 0)
                out.print(" options...");
            else
                out.print(" [options...]");
        }

        // Positional arguments.
        for (Option opt : options()) {
            if (!opt.positional())
                continue;
            out.print(' ');
            if (!opt.required())
                out.print('[');
            out.print(opt.primaryName());
            if (!opt.required())
                out.print(']');
        }

        out.println();
    }

    /**
     * Writes the help message to the specified stream.
     * @param out the stream to which the help message is written
     * @throws NullPointerException {@code out} is null
     */
    public void help(PrintStream out) {
        enum Where {
            Options, Arguments;
            static Where of(Option opt) {
                if (opt.positional())
                    return Arguments;
                else
                    return Options;
            }
        }
        Where where = null;

        for (Option opt : options) {
            {
                Where thisWhere = Where.of(opt);
                if (thisWhere != where) {
                    if (where != null)
                        out.println();
                    out.print(thisWhere.name());
                    out.println(':');
                    out.println();
                    where = thisWhere;
                }
            }

            out.print("  ");

            String metaVar = opt.metaVar();
            if (metaVar == null)
                metaVar = "ARG";

            boolean firstName = true;
            for (String name : opt.names()) {
                if (firstName)
                    firstName = false;
                else
                    out.print(", ");
                out.print(name);
                if (name.startsWith("--")) {
                    if (opt.needsValue()) {
                        out.print('=');
                        out.print(metaVar);
                    }
                } else if (name.startsWith("-")) {
                    if (opt.needsValue()) {
                        out.print(' ');
                        out.print(metaVar);
                    }
                }
                // No meta-variable for positional arguments.
            }

            out.println();
            out.print("    ");
            String help = opt.help();
            if (help == null)
                help = "(undocumented)";
            out.println(help);
        }
    }

    /**
     * Parses a command line and informs the user.
     *
     * This method writes diagnostics and help output and terminates
     * the program (by throwing {@link SystemExit}).  To avoid program
     * termination, use {@link #parse(String[])} instead.
     *
     * @param mainClass the class of the main program
     * @param args the argument array
     * @return a new record object with the data parsed from the command line
     * @throws NullPointerException {@code mainClass} or {@code args} is null
     * @throws SystemExit an error occurred, or the user requested help output
     */
    public T run(Class<?> mainClass, String[] args) {
        Objects.requireNonNull(mainClass, "mainClass");
        try {
            return parse(args);
        } catch (HelpException e) {
            usage(mainClass, System.out);
            System.out.println();
            help(System.out);
            throw new SystemExit(0);
        } catch (ArgumentListException e) {
            e.write(System.err);
            throw new SystemExit(1);
        }
    }

    /**
     * Returns the options that this argument parser can process.
     * @return an immutable list of options
     */
    public List<Option> options() {
        return options;
    }

    private MethodHandle recordConstructor(MethodHandles.Lookup lookup,
        Class<T> cls, RecordComponent[] components) {
        var ctype = MethodType.methodType(void.class,
            Arrays.stream(components)
            .map(RecordComponent::getType)
            .toArray(Class<?>[]::new));
        try {
            return lookup.findConstructor(cls, ctype);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InternalError(e);
        }
    }

    private List<Option> computeOptions(RecordComponent[] components) {
        var result = new ArrayList<Option>(components.length);
        boolean positional = false;
        Option optionalPositional = null;
        for (int i = 0; i < components.length; ++i) {
            var opt = Option.of(components, i, positional);
            result.add(opt);
            positional = opt.positional();
            if (positional) {
                if (opt.required()) {
                    if (optionalPositional != null)
                        throw new ConfigurationException(String.format("""
Required positional field %s in %s after optional field %s""",
                                opt.component().getName(),
                                opt.component().getDeclaringRecord(),
                                optionalPositional.component().getName()));
                } else
                    optionalPositional = opt;
            }
        }
        return List.copyOf(result);
    }

    private int computeFirstPositionalIndex() {
        for (Option opt : options)
            if (opt.positional())
                return opt.index();
        return options.size();
    }

    private int[] computeDefaultInitIndices() {
        var result = new int[options.size()];
        int resultPos = 0;
        for (Option opt : options) {
            Object def = opt.defaultObject();
            if (def != null)
                result[resultPos++] = opt.index();
        }
        return Arrays.copyOf(result, resultPos);
    }

    private static boolean isShortOption(String name) {
        return name.length() == 2 && name.charAt(0) == '-';
    }

    private <T> Map<T, Option> optionsMap(boolean shortOptions,
        Function<String, T> converter) {
        var result = new HashMap<T, Option>();
        for (var opt : options)
            if (!opt.positional())
                for (var name : opt.names())
                    if (isShortOption(name) == shortOptions) {
                        var old = result.put(converter.apply(name), opt);
                        if (old != null)
                            throw new ConfigurationException(String.format(
                                    "Fields %s and %s of %s have the same option name %s",
                                    old.component().getName(),
                                    opt.component().getName(),
                                    opt.component().getDeclaringRecord(),
                                    name));
                    }
        return Map.copyOf(result);
    }

    private void initFields(Object[] fields) {
        for (int idx : defaultInitIndices)
            fields[idx] = options.get(idx).defaultObject();
    }

    private void process(
        String[] args, Object[] fields, ArrayList<String> errors) {
        ArrayList<String> positional = new ArrayList<>(args.length);
        ARGS: for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.equals("--")) {
                // End of option list.
                for (++i; i < args.length; ++i)
                    positional.add(args[i]);
                break;
            } else if (arg.startsWith("--")) {
                // Long option.
                int equalSign = arg.indexOf('=', 2);
                boolean haveValue = equalSign >= 0;
                String optionName;
                if (haveValue)
                    optionName = arg.substring(2, equalSign);
                else
                    optionName = arg.substring(2);
                Option option = longOptions.get(optionName);
                if (option == null) {
                    if (optionName.equals("help"))
                        throw new HelpException();
                    errors.add("unknown option: --" + optionName);
                    continue ARGS;
                }
                boolean needsValue = option.needsValue();
                if (haveValue && !needsValue) {
                    errors.add("option --" + optionName
                        + " does not expect an argument");
                    continue ARGS;
                }
                if (needsValue) {
                    String valueString;
                    if (haveValue)
                        processOption(fields, arg.substring(equalSign + 1),
                            option, errors, "--", optionName);
                    else {
                        ++i;
                        processOption(
                            fields, args, i, option, errors, "--", optionName);
                    }
                } else { // !needsValue
                    fields[option.index()] = true;
                }
            } else if (arg.startsWith("-")) {
                // Short option.  These can be clustered (for flags).
                for (int j = 1; j < arg.length(); ++j) {
                    char ch = arg.charAt(j);
                    Option option = shortOptions.get(ch);
                    if (option == null) {
                        errors.add("unknown option: -" + ch);
                        // Do not report further errors; they could be
                        // due to agglutination of the option value.
                        continue ARGS;
                    }
                    boolean needsValue = option.needsValue();
                    if (option.needsValue()) {
                        if (j + 1 < arg.length()) {
                            processOption(fields, arg.substring(j + 1),
                                option, errors, "-", ch);
                            // Reminder of the option string has been consumed.
                            continue ARGS;
                        } else {
                            ++i;
                            processOption(fields, args, i, option, errors,
                                "-", ch);
                        }
                    } else // !needsValue
                        fields[option.index()] = true;
                }
            } else
                positional.add(arg);
        }
        if (errors.isEmpty())
            processPositional(fields, positional, errors);
    }

    private void processOption(
        Object[] fields, String[] args, int argIndex, Option option,
        ArrayList<String> errors, String namePrefix, Object name) {
        if (argIndex >= args.length) {
            errors.add(String.format("option %s%s needs a value",
                    namePrefix, name));
            return;
        }
        processOption(fields, args[argIndex], option,
            errors, namePrefix, name);
    }

    private void processOption(
        Object[] fields, String valueString, Option option,
        ArrayList<String> errors, String namePrefix, Object name) {
        Object value;
        try {
            value = option.converter().apply(valueString);
        } catch (IllegalArgumentException e) {
            // FIXME: Use full quoting?
            if (valueString.isEmpty())
                valueString = "\"\"";
            if (name == null)
                name = option.primaryName();
            errors.add(String.format(
                    "invalid value for option %s%s: %s",
                    namePrefix, name, valueString));
            return;
        }
        fields[option.index()] = value;
    }

    private void processPositional(
        Object[] fields, ArrayList<String> positional,
        ArrayList<String> errors) {
        int toProcess = Math.min(
            fields.length - firstPositionalIndex, positional.size());
        for (int i = 0; i < toProcess; ++i)
            processOption(fields, positional.get(i),
                options.get(firstPositionalIndex + i), errors, "", null);
        if (errors.isEmpty() && positional.size() > toProcess) {
            var remaining = positional.subList(toProcess, positional.size());
            var remainingString = remaining.stream()
                .map(s -> '"' + s + '"') // FIXME: real quoting
                .collect(Collectors.joining(", "));
            if (remaining.size() == 1)
                errors.add("unrecognized argument: " + remainingString);
            else
                errors.add("unrecognized positional arguments: "
                    + remainingString);
        }
    }

    private void checks(String[] args, Object[] fields,
        ArrayList<String> errors) {
        checkRequired(fields, errors);
    }

    private void checkRequired(
        Object[] fields, ArrayList<String> errors) {
        for (int i = 0; i < fields.length; ++i) {
            var opt = options.get(i);
            if (fields[i] == null && opt.required())
                errors.add(opt.descriptor() + " is missing");
        }
    }

    private T construct(Object[] fields) {
        try {
            @SuppressWarnings("unchecked")
            var result0 = (T) constructor.invokeWithArguments(fields);
            return result0;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    private final MethodHandle constructor;
    private final List<Option> options;
    private final int firstPositionalIndex; // options.size() if no positionals
    private final int[] defaultInitIndices;
    private final Map<Character, Option> shortOptions;
    private final Map<String, Option> longOptions;
}
