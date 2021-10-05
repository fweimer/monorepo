package enyo.io.terminfo.tests;

import enyo.io.function.IOConsumer;
import enyo.io.terminfo.ScriptAnalysis;
import enyo.io.terminfo.StringCapability;
import enyo.io.terminfo.TermFile;
import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.TestFailure;
import enyo.testing.Testing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TestScriptAnalysis {
    @Test
    public static void testEmpty(Testing t) {
        ScriptAnalysis a = analyze("");
        t.examine(a.parameterTypes()).is(Map.of());
        checkRegular(t, a, 0);
    }

    @Test
    public static void testLiteral1(Testing t) {
        ScriptAnalysis a = analyze("ab%%cd");
        t.examine(a.parameterTypes()).is(Map.of());
        checkRegular(t, a, 0);
    }

    @Test
    public static void testLiteral2(Testing t) {
        ScriptAnalysis a = analyze("terminal script");
        t.examine(a.parameterTypes()).is(Map.of());
        checkRegular(t, a, 0);
    }

    @Test
    public static void testIncrement1(Testing t) {
        ScriptAnalysis a = analyze("%i");
        t.examine(a.parameterTypes()).is(Map.of());
        checkRegular(t, a, 0);
    }

    @Test
    public static void testIncrement2(Testing t) {
        ScriptAnalysis a = analyze("%i%p1%c");
        t.examine(a.parameterTypes()).is(intTypes("1"));
        checkRegular(t, a, 1);
    }

    @Test
    public static void testIncrement3(Testing t) {
        ScriptAnalysis a = analyze("%i%p2%c");
        t.examine(a.parameterTypes()).is(intTypes("12"));
        checkRegular(t, a, 1);
    }

    @Test
    public static void testIncrement4(Testing t) {
        ScriptAnalysis a = analyze(".%i%p1%s");
        t.examine(a.parameterTypes()).is(Map.of());
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertFalse(a.legacyStackMode());
        t.examine(a.errors()).is(List.of(
                "Parameter 1 used as integer (offset 1)"
                + " and string (offset 6)"));
    }

    @Test
    public static void testIncrement5(Testing t) {
        ScriptAnalysis a = analyze("..%i%p2%s");
        t.examine(a.parameterTypes()).is(Map.of());
        t.examine(a.parameterTypes()).is(Map.of());
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertFalse(a.legacyStackMode());
        t.examine(a.errors()).is(List.of(
                "Parameter 2 used as integer (offset 2)"
                + " and string (offset 7)"));
    }

    @Test
    public static void testIncrement6(Testing t) {
        ScriptAnalysis a = analyze("%i%p3%s");
        t.examine(a.parameterTypes()).is(types("12", "3"));
        checkRegular(t, a, 1);
    }

    @Test
    public static void testConstant(Testing t) {
        ScriptAnalysis a = analyze("%'X'%{17}%&%c");
        t.examine(a.parameterTypes()).is(Map.of());
        checkRegular(t, a, 2);
    }

    @Test
    public static void testFormatInt(Testing t) {
        Consumer<String> intExpected = str -> {
            ScriptAnalysis a = analyze("%p1" + str);
            t.examine(a.parameterTypes()).is(intTypes("1"));
            checkRegular(t, a, 1);
        };

        for (String flags : List.of(
                "", ":", ":-2", "02", "3", "4.5", ":+6"))
            "doxX".chars().forEach(
                ch -> intExpected.accept('%' + flags + (char) ch));
        "oxX".chars().forEach(
            ch -> intExpected.accept("%#7" + (char) ch));
    }

    @Test
    public static void testFormatString(Testing t) {
        Consumer<String> intExpected = str -> {
            ScriptAnalysis a = analyze("%p1" + str);
            t.examine(a.parameterTypes()).is(stringTypes("1"));
            checkRegular(t, a, 1);
        };

        for (String flags : List.of(
                "", ":", ":-2", "02", "3", "4.5", ":+6"))
            intExpected.accept('%' + flags + 's');
    }

    @Test
    public static void testSetVariable(Testing t) {
        ScriptAnalysis a = analyze("%p3%Pa");
        t.examine(a.parameterTypes()).is(intTypes("3"));
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of('a'));
        t.examine(a.readVariables()).is(Set.of());
        t.examine(a.errors()).is(List.of());
        t.assertFalse(a.legacyStackMode());
    }

    @Test
    public static void testReadVariable(Testing t) {
        ScriptAnalysis a = analyze("%gr%c");
        t.examine(a.parameterTypes()).is(Map.of());
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of('r'));
        t.examine(a.errors()).is(List.of());
        t.assertFalse(a.legacyStackMode());
    }

    @Test
    public static void testGap(Testing t) {
        ScriptAnalysis a = analyze("%p1%c%p3%c");
        t.examine(a.parameterTypes()).is(intTypes("13"));
        t.examine(a.maxStackDepth()).is(1);
        checkRegular(t, a, 1);

        a = analyze("%p1 %l %c %p3 %l %c");
        t.examine(a.parameterTypes()).is(stringTypes("13"));
        checkRegular(t, a, 1);

        a = analyze("%p1%p3%p4%A");
        t.examine(a.parameterTypes()).is(intTypes("34"));
        t.examine(a.maxStackDepth()).is(3);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertFalse(a.legacyStackMode());
        t.examine(a.errors()).is(List.of("Stack depth at end of script: 2"));
    }

    @Test
    public static void testString1(Testing t) {
        ScriptAnalysis a = analyze("%p1%s");
        t.examine(a.parameterTypes()).is(stringTypes("1"));
        checkRegular(t, a, 1);
    }

    @Test
    public static void testString2(Testing t) {
        ScriptAnalysis a = analyze("%p1%l%p3%s%p4%d%d");
        t.examine(a.parameterTypes()).is(types("4", "13"));
        checkRegular(t, a, 2);
    }

    @Test
    public static void testConditionWelformed(Testing t) {
        ScriptAnalysis a = analyze("%?%p1%t1%e2%;");
        t.examine(a.parameterTypes()).is(intTypes("1"));
        checkRegular(t, a, 1);

        a = analyze("%p1%p2%?%p3%t1%e2%;%c%l%c");
        t.examine(a.parameterTypes()).is(types("23", "1"));
        checkRegular(t, a, 3);

        // Restores the original stack layout after changing it.
        a = analyze("%p2 %? %p1 %t %c %p2 %; %c");
        t.examine(a.parameterTypes()).is(intTypes("12"));
        checkRegular(t, a, 2);

        // A numeric parameter can be combined with a string constant.
        // First with previous type information.
        a = analyze("%p2 %c %? %p1 %t %{0} %e %p2 %; %c");
        t.examine(a.parameterTypes()).is(intTypes("12"));
        checkRegular(t, a, 1);

        // Now without previous type information.
        a = analyze("%? %p1 %t %{0} %e %p2 %; %c");
        t.examine(a.parameterTypes()).is(intTypes("12"));
        checkRegular(t, a, 1);
    }

    @Test
    public static void testConditionDynamicSelection(Testing t) {
        ScriptAnalysis a = analyze("%? %p1 %t %p2 %e %p3 %; %l");
        t.examine(a.parameterTypes()).is(intTypes("1"));
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertFalse(a.legacyStackMode());
        t.examine(a.errors()).is(List.of(
                "Stack mismatch between %t (offset 7) and %e (offset 21):"
                + " [2] != [3]"));

        a = analyze("%? %p1 %t --- %e %p3 %; %l");
        t.examine(a.parameterTypes()).is(intTypes("1"));
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertFalse(a.legacyStackMode());
        t.examine(a.errors()).is(List.of(
                "Stack mismatch between %t (offset 7) and %e (offset 21):"
                + " [] != [3]"));

        a = analyze("%? %p1 %t %p2 %e --- %; %l");
        t.examine(a.parameterTypes()).is(intTypes("1"));
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertFalse(a.legacyStackMode());
        t.examine(a.errors()).is(List.of(
                "Stack mismatch between %t (offset 7) and %e (offset 21):"
                + " [2] != []"));
    }

    @Test
    public static void testConditionDynamicNoElse(Testing t) {
        ScriptAnalysis a = analyze("%? %p1 %t %{0} %; %l");
        t.examine(a.parameterTypes()).is(intTypes("1"));
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertFalse(a.legacyStackMode());
        t.examine(a.errors()).is(List.of(
                "Stack mismatch between %t (offset 7) and %; (offset 15):"
                + " [] != [I]"));
    }

    @Test
    public static void testParameterConflict(Testing t) {
        ScriptAnalysis a = analyze("%p1%c %p2%s %p1%l %p2%d");
        t.examine(a.parameterTypes()).is(Map.of());
        t.examine(a.maxStackDepth()); // Due to %l.is(2);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertFalse(a.legacyStackMode());
        t.examine(a.errors()).is(List.of(
                "Parameter 1 used as integer (offset 3)"
                + " and string (offset 15)",
                "Parameter 2 used as string (offset 9)"
                + " and integer (offset 21)"));
    }

    @Test
    public static void testLegacyStackMode1(Testing t) {
        ScriptAnalysis a = analyze("%d");
        t.examine(a.parameterTypes()).is(intTypes("1"));
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertTrue(a.legacyStackMode());
        t.examine(a.errors()).is(List.of());
    }

    @Test
    public static void testLegacyStackMode2(Testing t) {
        ScriptAnalysis a = analyze("%s");
        t.examine(a.parameterTypes()).is(stringTypes("1"));
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertTrue(a.legacyStackMode());
        t.examine(a.errors()).is(List.of());
    }

    @Test
    public static void testLegacyStackMode3(Testing t) {
        ScriptAnalysis a = analyze("%+%c");
        t.examine(a.parameterTypes()).is(intTypes("12"));
        t.examine(a.maxStackDepth()).is(2);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertTrue(a.legacyStackMode());
        t.examine(a.errors()).is(List.of());
    }

    @Test
    public static void testLegacyStackMode4(Testing t) {
        ScriptAnalysis a = analyze("%{0}%+%c");
        t.examine(a.parameterTypes()).is(intTypes("1"));
        t.examine(a.maxStackDepth()).is(2);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertTrue(a.legacyStackMode());
        t.examine(a.errors()).is(List.of());
    }

    @Test
    public static void testLegacyStackMode5(Testing t) {
        ScriptAnalysis a = analyze("%{0}%+%c%s");
        t.examine(a.parameterTypes()).is(types("1", "2"));
        t.examine(a.maxStackDepth()).is(3);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertTrue(a.legacyStackMode());
        t.examine(a.errors()).is(List.of());
    }

    @Test
    public static void testLegacyStackMode6(Testing t) {
        ScriptAnalysis a = analyze("%d%s");
        t.examine(a.parameterTypes()).is(types("1", "2"));
        t.examine(a.maxStackDepth()).is(2);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertTrue(a.legacyStackMode());
        t.examine(a.errors()).is(List.of());
    }

    @Test
    public static void testLegacyStackMode7(Testing t) {
        ScriptAnalysis a = analyze("%s%{0}%{1}%d%d%d");
        t.examine(a.parameterTypes()).is(types("2", "1"));
        t.examine(a.maxStackDepth()).is(3);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.assertTrue(a.legacyStackMode());
        t.examine(a.errors()).is(List.of());
    }

    @Test
    public static void testIntegerAsString(Testing t) {
        ScriptAnalysis a = analyze("%{0}%s");
        t.examine(a.errors())
            .is(List.of("Attempt to use string as integer: []"));
        t.examine(a.parameterTypes()).is(Map.of());
        t.examine(a.maxStackDepth()).is(1);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.examine(a.legacyStackMode()).is(false);
    }

    @Test
    public static void testFiles(Testing t) throws IOException {
        TestTermFile.terminfoFiles(t).forEach(IOConsumer.of(path -> {
                    byte[] blob = Files.readAllBytes(path);
                    TermFile tf = TermFile.of(blob);
                    for (StringCapability cap :
                             TestScriptVisitor.INTERESTING_CAPS) {
                        byte[] cap1 = tf.get(cap);
                        if (cap1 != null && !brokenScript(cap1)) {
                            ScriptAnalysis a =
                                new ScriptAnalysis(cap1);
                            if (!a.errors().isEmpty())
                                throw new TestFailure(String.format(
                                        "%s %s: %s \"%s\"",
                                        path, cap, a.errors(),
                                        escape(cap1)));
                            t.examine(path)
                                .map(ignore -> cap)
                                .map(ignore -> escape(cap1))
                                .map(ignore -> a.parameterTypes())
                                .mapToInt(map ->
                                    map.keySet().stream()
                                    .mapToInt(i -> i)
                                    .max()
                                    .orElse(0))
                                .check(Checks.lessThanOrEquals(
                                        cap.parameters()));
                        }
                    }
                }));
    }

    private static ScriptAnalysis analyze(String script) {
        byte[] bytes = script.getBytes(StandardCharsets.ISO_8859_1);
        return new ScriptAnalysis(bytes);
    }

    private static void checkRegular(Testing t,
        ScriptAnalysis a, int stackDepth) {
        t.examine(a.maxStackDepth()).is(stackDepth);
        t.examine(a.modifiedVariables()).is(Set.of());
        t.examine(a.readVariables()).is(Set.of());
        t.examine(a.errors()).is(List.of());
        t.assertFalse(a.legacyStackMode());
    }

    private static Map<Integer, ScriptAnalysis.ParameterType> types(
        String intParams, String stringParams) {
        HashMap<Integer, ScriptAnalysis.ParameterType> result =
            new HashMap<>();
        intParams.chars()
            .forEach(ch -> result.put(
                    ch - '0', ScriptAnalysis.ParameterType.INTEGER));
        stringParams.chars()
            .forEach(ch -> result.merge(
                    ch - '0', ScriptAnalysis.ParameterType.STRING,
                    (a, b) -> { throw new TestFailure("Duplicate"); }));
        return result;
    }

    private static Map<Integer, ScriptAnalysis.ParameterType> types(
        String params, ScriptAnalysis.ParameterType type) {
        return params.chars()
            .mapToObj(ch -> ch - '0')
            .collect(Collectors.toMap(
                    Function.identity(),
                    ch -> type));
    }

    private static Map<Integer, ScriptAnalysis.ParameterType> intTypes(
        String params) {
        return types(params, ScriptAnalysis.ParameterType.INTEGER);
    }

    private static Map<Integer, ScriptAnalysis.ParameterType> stringTypes(
        String params) {
        return types(params, ScriptAnalysis.ParameterType.STRING);
    }

    // Used to avoid testing scripts
    static final Set<String> BROKEN_SCRIPTS = Set.of(
        // Non-empty stack at script end.
        "\033[%?%p2%t4;%;%?%p4%t5;%;%?%p6%t1;%;"
        + "%?%p1%p2%|%p3%!" // Too many values in if clause.
        + "%t7;%;%?%p7%t8;%;m\016", // aaa-24-rv: set_attributes

        "\033|%'0'%p1%+%c2%p2\031", // ndr9500-25-nl: pkey_local
        "\033|%'0'%p1%+%c1%p2\031", // ndr9500-25-nl: pkey_xmit

        "\033[%p1%i%p1%d;%p2%dH", // ti916-132: cursor_address
        "\033]2;%p1",

        // Stack mismatch in if condition.
        "\033[%?%p1%{8}%<%t3%p1%d%e%?%p1%{16}%<%t%p1%'R'%+"
        + "%e38:2::%p1%{65536}%/%d:%p1%{256}%/%{255}%&%d:%p1%{255}%&%d%;%;m",
        "\033[%?%p1%{8}%<%t4%p1%d%e%?%p1%{16}%<%t%p1%{92}%+"
        + "%e48:2::%p1%{65536}%/%d:%p1%{256}%/%{255}%&%d:%p1%{255}%&%d%;%;m",

        // Wrong parameter count.
        "\033w@%p1%'0'%+%c%p2%' '%+%c%p3%' '%+%c$<20>", // ncr260wy350pp: mrcup
        "\033w@%p1%'0'%+%c%p2%' '%+%c%p3%' '%+%c$<30>", // ncr260wy60pp: mrcup
        "\033w@%p1%'0'%+%c%p2%' '%+%c%p3%' '%+%c$<5>", // ncr260vppp: mrcup
        "\033w@%p1%'0'%+%c%p2%' '%+%c%p3%' '%+%c$<10>", // ncr260wy32wpp: mrcup

        // Multiple brokeness.
        "\033!%+%p1%' '%+%p2%' '" // icl6404: change_scroll_region
                                                     );
    static boolean brokenScript(byte[] script) {
        return BROKEN_SCRIPTS.contains(
            new String(script, StandardCharsets.ISO_8859_1));
    }

    static String escape(byte[] script) {
        StringBuilder sb = new StringBuilder(script.length + 5);
        for (byte b : script) {
            if (b < ' ' || b > '~')
                sb.append(String.format("\\%03o", Byte.toUnsignedInt(b)));
            else if (b == '\\' || b == '"') {
                sb.append('\\');
                sb.append((char) b);
            } else
                sb.append((char) b);
        }
        return sb.toString();
    }
}
