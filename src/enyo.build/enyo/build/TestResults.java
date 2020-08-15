package enyo.build;

import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.runtime.Examined;
import enyo.testing.runtime.ExceptionPhase;
import enyo.testing.runtime.Results;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TestResults implements Results {
    @Override
    public void unsupported(TestMethodLocator test, String context) {
        synchronized (System.err) {
            System.err.format("%s: warning: unsupported test: %s%n",
                test, context);
        }
    }

    @Override
    public void failure(TestMethodLocator test, List<Examined> context,
        String check, Object[] otherObjects,
        List<StackWalker.StackFrame> stackTrace) {
        synchronized (System.err) {
            System.err.format("%s: error: failed test: %s%n", test, check);
            stackTrace.stream()
                .forEach(frame -> System.err.format("\t%s%n", frame));
            printContext(System.err, context);
            printObjects(System.err, 1, otherObjects);
        }
    }

    @Override
    public void exception(TestMethodLocator test, List<Examined> context,
        ExceptionPhase phase, Throwable exception,
        Class<? extends Throwable> expected) {
        synchronized (System.err) {
            if (expected == null)
                System.err.format("%s: error: failed test%n", test);
            else
                System.err.format(
                    "%s: error: failed test expecting exception %s%n",
                    test, expected.getName());
            exception.printStackTrace(System.err);
            printContext(System.err, context);
        }
    }

    @Override
    public void completed(TestMethodLocator test, boolean success) {
    }

    private static void printContext(PrintStream stream,
        List<Examined> context) {
        if (context.isEmpty())
            return;

        // Single context string.
        if (context.size() == 1 && context.get(0).value() == null) {
            stream.format("note: in context: %s%n", context.get(0).context());
            return;
        }

        // Single value.  A null/null examined value is reported as a
        // null value.
        if (context.size() == 1) {
            Object value = context.get(0).value();
            stream.print("note: examined value: ");
            printObject(stream, 1, value);
            printObjectNested(stream, 1, value);
            return;
        }

        stream.println("note: test failure context:");
        for (int i = 0; i < context.size(); ++i) {
            Examined e = context.get(i);
            if (e.context() != null)
                stream.format("  context %d: %s%n", i, e.context());
            if (e.value() != null || e.context() == null) {
                stream.format("  value %d: ", i);
                printObject(stream, 2, e.value());
                printObjectNested(stream, 2, e.value());
            }
        }
    }


    private static void printObjects(PrintStream stream, int level,
        Object[] otherObjects) {
        if (otherObjects.length == 0)
            return;

        stream.println("note: objects related to the test failure:");
        for (Object object : otherObjects) {
            stream.print("  ");
            printObject(stream, 1, object);
            printObjectNested(stream, 2, object);
        }
    }

    private static void printObject(PrintStream stream,
        int level, Object object) {
        if (object == null)
            stream.println("null");
        else if (object.getClass() == String.class)
            printString(stream, (String) object);
        else if (SHORT_STRING_TYPES.contains(object.getClass()))
            stream.format("%s: %s%n", object.getClass().getName(), object);
        else {
            String typeTags = typeTags(object.getClass());
            stream.format("%s%s%n", typeTags, object.getClass().getName());
            if (!isNested(object)) {
                printIndent(stream, level + 1);
                stream.print(" toString: ");
                String toString;
                try {
                    toString = object.toString();
                } catch (Throwable e) {
                    stream.format("exception: %s%n", e.getClass().getName());
                    toString = null;
                }
                if (toString != null)
                    printString(stream, toString);
            }
        }
    }

    private static void printString(PrintStream stream, String string) {
        int limit = 200;
        boolean truncated;
        if (string.length() <= limit)
            truncated = false;
        else {
            truncated = true;
            string = string.substring(0, limit);
        }
        stream.print('"');
        string.chars()
            .forEach(ch -> {
                    if (ch < ' ' || ch > '~') {
                        if (ch > 255)
                            stream.format("\\u%04x", ch);
                        else
                            stream.format("\\%03o", ch);
                    } else {
                        if (ch == '\\' || ch == '"' || ch == '\'')
                            stream.print('\\');
                        stream.print((char) ch);
                    }
                });
        stream.print('"');
        if (truncated)
            stream.print("... (truncated)");
        stream.println();
    }

    private static boolean isNested(Object object) {
        return object instanceof Collection;
    }

    private static void printObjectNested(PrintStream stream, int level,
        Object object) {

        if (object instanceof Collection) {
            boolean numbered = object instanceof List;

            int i = 0;
            for (Object obj : (Collection<?>) object) {
                printIndent(stream, level);
                if (numbered)
                    stream.format("%d: ", i);
                printObject(stream, level, obj);

                ++i;
            }
        }
    }

    private static void printIndent(PrintStream stream, int level) {
        for (int i = 0; i < level; ++i)
            stream.print("  ");
    }

    // Computes a string describing key interfaces related to a type.
    // This helps to identify the purposes of internal classes.
    private static String typeTags(Class<?> cls) {

        StringBuilder sb = null;

        if (WELL_KNOWN_THROWABLE.contains(cls))
            // Non-derived throwable classes do not need markers
            // because their names are easily recognized.
            return "";
        if (Throwable.class.isAssignableFrom(cls))
            for (Class<?> exception : WELL_KNOWN_THROWABLE)
                if (exception.isAssignableFrom(cls)) {
                    String name = exception.getName();
                    name = name.substring(name.lastIndexOf('.') + 1);
                    sb = typeTags0(sb, name);
                    break;
                }

        boolean collection = false;
        if (List.class.isAssignableFrom(cls)) {
            sb = typeTags0(sb, "List");
            collection = true;
        }
        if (Set.class.isAssignableFrom(cls)) {
            sb = typeTags0(sb, "Set");
            collection = true;
        }
        if (Map.class.isAssignableFrom(cls)) {
            sb = typeTags0(sb, "Map");
            collection = true;
        }
        if (Enum.class.isAssignableFrom(cls)) {
            sb = typeTags0(sb, "Enum");
            collection = true;
        }
        if (!collection && Collection.class.isAssignableFrom(cls))
            sb = typeTags0(sb, "Collection");
        if (Stream.class.isAssignableFrom(cls))
            sb = typeTags0(sb, "Stream");
        if (sb != null) {
            sb.append("} ");
            return sb.toString();
        } else
            return "";
    }

    private static List<Class<?>> WELL_KNOWN_THROWABLE = List.of(
        Error.class,
        IOException.class,
        RuntimeException.class,
        // Order is important here: Those subsume previous classes.
        Exception.class,
        Throwable.class);


    private static StringBuilder typeTags0(StringBuilder sb, String type) {
        if (sb == null) {
            sb = new StringBuilder();
            sb.append('{');
        } else
            sb.append(' ');
        sb.append(type);
        return sb;
    }

    // Types that can be directly printed using toString().
    private static final Set<Class<?>> SHORT_STRING_TYPES = Set.of(
        Boolean.class,
        Byte.class,
        Class.class,
        Double.class,
        Float.class,
        Integer.class,
        Long.class,
        Module.class,
        Package.class,
        Process.class,
        Short.class);
}
