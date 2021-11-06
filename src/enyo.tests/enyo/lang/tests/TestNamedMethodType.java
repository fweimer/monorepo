package enyo.lang.tests;

import enyo.lang.NamedMethodType;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;
import java.util.OptionalInt;

public final class TestNamedMethodType {
    @Test
    public void testNull(Testing t) {
        t.expect(() -> new NamedMethodType(null, null),
            NullPointerException.class);
        t.expect(
            () -> new NamedMethodType(null, MethodType.methodType(void.class)),
            NullPointerException.class);
        t.expect(
            () -> new NamedMethodType("null", null),
            NullPointerException.class);
        t.expect(() -> new NamedMethodType(null),
            NullPointerException.class);
    }

    @Test
    public void testMalformedName(Testing t) {
        t.expect(
            () -> new NamedMethodType("", MethodType.methodType(void.class)),
            IllegalArgumentException.class);
    }

    @Test
    public void testFromParts(Testing t) {
        var name = "testFromParts";
        var type = MethodType.methodType(void.class, Testing.class);
        t.examine(new NamedMethodType(name, type))
            .map(NamedMethodType::name, e -> e.isSame(name))
            .map(NamedMethodType::type, e -> e.isSame(type));
    }

    @Test
    public void testFromMethod(Testing t) throws NoSuchMethodException {
        t.examine(getClass().getMethod("testFromMethod", Testing.class))
            .map(NamedMethodType::new)
            .map(NamedMethodType::name, e -> e.is("testFromMethod"))
            .map(NamedMethodType::type, e -> e.is(
                    MethodType.methodType(void.class, Testing.class)));
    }

    @Test
    public void testToString(Testing t) {
        t.examine(new NamedMethodType("method1",
                MethodType.methodType(void.class)))
            .map(NamedMethodType::toString)
            .is("void method1()");
        t.examine(new NamedMethodType("method2",
                MethodType.methodType(int.class, double.class)))
            .map(NamedMethodType::toString)
            .is("int method2(double)");
        t.examine(new NamedMethodType("method3",
                MethodType.methodType(
                    String.class, Boolean.class, getClass())))
            .map(NamedMethodType::toString)
            .is("java.lang.String method3(java.lang.Boolean,"
                + "enyo.lang.tests.TestNamedMethodType)");
    }

    @Test
    public void testFindVirtual(Testing t) throws Throwable {
        class C1 {}
        class C2 {};
        var c1 = new C1();
        var c2 = new C2();
        var counter = t.counter(1);
        Function<C1, C2> f = (arg) -> {
            t.examine(arg).isSame(c1);
            counter.increment();
            return c2;
        };
        var named = new NamedMethodType("apply",
            MethodType.methodType(Object.class, Object.class));
        var lookup = MethodHandles.publicLookup();
        MethodHandle handle = named.findVirtual(lookup, Function.class);
        t.examine((C2) (Object) handle.invokeExact(f, (Object) c1))
            .isSame(c2);
        counter.checkTarget();
    }

    @Test
    public void testFindStatic(Testing t) throws Throwable {
        var named = new NamedMethodType("of",
            MethodType.methodType(OptionalInt.class, int.class));
        var lookup = MethodHandles.publicLookup();
        MethodHandle handle = named.findStatic(lookup, OptionalInt.class);
        t.examine((OptionalInt) handle.invokeExact(17))
            .is(OptionalInt.of(17));
    }
}
