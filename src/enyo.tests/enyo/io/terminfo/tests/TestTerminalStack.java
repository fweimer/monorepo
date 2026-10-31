package enyo.io.terminfo.tests;

import enyo.io.terminfo.TerminalStack;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TestTerminalStack {
    @Test
    public static void testEmpty(Testing t) {
        TerminalStack stack = new TerminalStack();
        t.examine(stack.stream().collect(Collectors.toList())).is(List.of());
        t.examine(stack.toString()).is("[]");
        t.expect(() -> stack.popInt(),
            IllegalStateException.class)
	    .map(Exception::getMessage).is("Stack is empty");
        t.expect(() -> stack.popString(),
            IllegalStateException.class)
            .map(Exception::getMessage).is("Stack is empty");
    }

    @Test
    public static void testPushInt(Testing t) {
        TerminalStack stack = new TerminalStack();
        stack.pushInt(1);
        t.examine(stack.stream().collect(Collectors.toList())).is(List.of(1));
        t.examine(stack.toString()).is("[1]");
        stack.pushInt(2);
        t.examine(stack.stream().collect(Collectors.toList()))
            .is(List.of(2, 1));
        t.examine(stack.toString()).is("[2,1]");
        stack.pushInt(3);
        t.examine(stack.stream().collect(Collectors.toList()))
            .is(List.of(3, 2, 1));
        t.examine(stack.toString()).is("[3,2,1]");
    }

    @Test
    public static void testPushString(Testing t) {
        TerminalStack stack = new TerminalStack();
        t.expect(() -> stack.pushString(null), NullPointerException.class);
        stack.pushString("s1".getBytes());
        t.examine(stack.stream()
            .map(obj -> new String((byte[]) obj))
            .collect(Collectors.toList()))
            .is(List.of("s1"));
        t.examine(stack.toString()).is("[\"s1\"]");
        stack.pushString("s2".getBytes());
        t.examine(stack.stream()
            .map(obj -> new String((byte[]) obj))
            .collect(Collectors.toList()))
            .is(List.of("s2", "s1"));
        t.examine(stack.toString()).is("[\"s2\",\"s1\"]");
        stack.pushString(new byte[0]);
        t.examine(stack.stream()
            .map(obj -> new String((byte[]) obj))
            .collect(Collectors.toList()))
            .is(List.of("", "s2", "s1"));
        t.examine(stack.toString()).is("[\"\",\"s2\",\"s1\"]");
    }

    @Test
    public static void testRepush(Testing t) {
        TerminalStack stack = new TerminalStack();
        stack.pushInt(-1);
        stack.pushString(new byte[] { -1 });
        stack.pushString(new byte[] { -128 });
        stack.pushString(new byte[] { 127 });
        stack.pushString(new byte[0]);
        t.assertByteArrayEquals(new byte[0], stack.popString());
        stack.pushInt(123);
        t.examine(stack.stream()
            .map(TestTerminalStack::convertString)
            .collect(Collectors.toList()))
            .is(List.of(123, "\177", "\200", "\377", -1));
        t.examine(stack.toString())
            .is("[123,\"\\177\",\"\\200\",\"\\377\",-1]");
    }

    @Test
    public static void testRunScript0(Testing t) {
        runScript(t, Stream.of());
    }

    @Test
    public static void testRunScript1(Testing t) {
        Action[] values = Action.values();
        Arrays.stream(values)
            .forEach(a1 -> runScript(t, Stream.of(a1)));
    }

    @Test
    public static void testRunScript2(Testing t) {
        Action[] values = Action.values();
        Arrays.stream(values)
            .forEach(a1 -> Arrays.stream(values)
                .forEach(a2 -> runScript(t, Stream.of(a1, a2))));
    }

    @Test
    public static void testRunScript3(Testing t) {
        Action[] values = Action.values();
        Arrays.stream(values)
            .forEach(a1 -> Arrays.stream(values)
                .forEach(a2 -> Arrays.stream(values)
                    .forEach(a3 -> runScript(t, Stream.of(a1, a2, a3)))));
    }

    @Test
    public static void testRunScript4(Testing t) {
        Action[] values = Action.values();
        Arrays.stream(values)
            .forEach(a1 -> Arrays.stream(values)
                .forEach(a2 -> Arrays.stream(values)
                    .forEach(a3 -> Arrays.stream(values)
                        .forEach(a4 ->
                            runScript(t, Stream.of(a1, a2, a3, a4))))));
    }

    @Test
    public static void testRunScript5(Testing t) {
        Action[] values = Action.values();
        Arrays.stream(values)
            .forEach(a1 -> Arrays.stream(values)
                .forEach(a2 -> Arrays.stream(values)
                    .forEach(a3 -> Arrays.stream(values)
                        .forEach(a4 -> Arrays.stream(values)
                            .forEach(a5 ->
                                runScript(t, Stream.of(
                                        a1, a2, a3, a4, a5)))))));
    }

    enum Action {
        PUSH_INT,
        PUSH_STRING,
        PUSH_NULL,
        POP_INT,
        POP_STRING,
        ;
    }

    private static void runScript(Testing t, Stream<Action> actions) {
        TerminalStack stack = new TerminalStack();
        ArrayList<Object> reference = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        actions.forEach(action -> {
                switch (action) {
                case PUSH_INT:
                    int val = counter.getAndIncrement();
                    stack.pushInt(val);
                    reference.add(val);
                    break;
                case PUSH_STRING:
                    String str = "s" + counter.getAndIncrement();
                    stack.pushString(str.getBytes(
                            StandardCharsets.ISO_8859_1));
                    reference.add(str);
                    break;
                case PUSH_NULL:
                    t.expect(() -> stack.pushString(null),
                        NullPointerException.class);
                    break;
                case POP_INT:
                    if (reference.isEmpty())
                        t.expect(() -> stack.popInt(),
                            IllegalStateException.class)
			    .map(Exception::getMessage)
			    .is("Stack is empty");
                    else {
                        int idx = reference.size() - 1;
                        Object obj = reference.get(idx);
                        if (obj.getClass() == Integer.class) {
                            t.examine(stack.popInt()).is((int) obj);
                            reference.remove(idx);
                        } else
                            t.expect(() -> stack.popInt(),
                                IllegalStateException.class)
				.map(Exception::getMessage)
                                .is("Top of stack is string");
                    }
                    break;
                case POP_STRING:
                    if (reference.isEmpty())
                        t.expect(() -> stack.popString(),
                            IllegalStateException.class)
			    .map(Exception::getMessage)
                            .is("Stack is empty");
                    else {
                        int idx = reference.size() - 1;
                        Object obj = reference.get(idx);
                        if (obj.getClass() == String.class) {
                            t.examine(convertString(stack.popString())).is(obj);
                            reference.remove(idx);
                        } else
                            t.expect(() -> stack.popString(),
                                IllegalStateException.class)
				.map(Exception::getMessage)
                                .is("Top of stack is integer");
                    }
                    break;
                }

                ArrayList<Object> actual = stack.stream()
                    .map(TestTerminalStack::convertString)
                    .collect(Collectors.toCollection(ArrayList::new));
                Collections.reverse(actual);
                t.examine(actual).is(reference);
                t.examine(actual.toString()).is(reference.toString());
            });
    }

    private static Object convertString(Object obj) {
        if (obj.getClass() == byte[].class)
            obj = new String((byte[]) obj,
                StandardCharsets.ISO_8859_1);
        return obj;
    }

    private static Object renderString(Object obj) {
        if (obj.getClass() == String.class)
            obj = '"' + obj.toString() + '"';
        return obj;
    }
}
