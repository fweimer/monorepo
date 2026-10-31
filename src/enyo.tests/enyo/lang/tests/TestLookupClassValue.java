package enyo.lang.tests;

import enyo.lang.LookupClassValue;
import enyo.lang.UncheckedIllegalAccessException;

import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.ThrowingCallable;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;

public final class TestLookupClassValue {
    @Test
    public static void testAccess(Testing t) {
        var counter = t.counter(1);
        var cv = LookupClassValue.of(
            (_, cls) -> {
                counter.increment();
                return new Value(cls);
            });
        // Check that illegal access does not result in a supplier call.
        t.expect(() -> cv.get(publicLookup(), Value.class),
            UncheckedIllegalAccessException.class);
        t.examine(counter.value()).is(0);

        // Subsequent access installs the class value.
        t.examine(cv.get(lookup(), Value.class))
            .map(v -> v.cls)
            .isSameObject(Value.class);
        counter.checkTarget();

        // But further access fails.
        t.expect(() -> cv.get(publicLookup(), Value.class),
            UncheckedIllegalAccessException.class);
        counter.checkTarget();
    }

    // Empirically, this produces >= 110 supplier calls together with
    // the class count 100, so the concurrent creation path is exercised.
    // Verified below with:
    // var cnt = new java.util.concurrent.atomic.AtomicInteger();
    // var cv = LookupClassValue.of(
    //        (_, cls) -> {cnt.incrementAndGet(); return new Value(cls);});
    private static final int THREADS = 5;

    @Test
    @Resources(threads=THREADS)
    public static void testConcurrent(Testing t) throws Exception {
        var classes = unnamedClasses(100);
        var cv = LookupClassValue.of((_, cls) -> new Value(cls));
	var values = t.parallelValues(IntStream.range(0, THREADS)
	    .mapToObj(_ -> (ThrowingCallable<List<Value>>) () ->
		classes.stream()
		.map(cls -> cv.get(lookup(), cls))
		.toList())
	    .toList());
        for (var l : values) {
            t.examine(l.stream()
                .map(Value::name)
                .toList())
                .is(IntStream.range(0, classes.size())
                    .mapToObj(i -> PACKAGE + '.' + "Unnamed" + i)
                    .toList());
        }
        t.examine(values.stream()
            .flatMap(List::stream)
            .distinct()
            .count())
            .is(classes.size());
    }

    private static final String PACKAGE =
        TestLookupClassValue.class.getPackage().getName();

    // Generate a list of unique classes, to exercises concurrent get.
    private static List<Class<?>> unnamedClasses(int count) {
        var lookup = lookup();
        return List.copyOf(
            IntStream.range(0, count)
            .mapToObj(i -> {
                    byte[] blob = ClassFile.of().build(
                        ClassDesc.of(PACKAGE, "Unnamed" + i), _ -> {});
                    try {
                        return lookup.defineHiddenClass(blob, true)
                            .lookupClass();
                    } catch (IllegalAccessException e) {
                        throw new UncheckedIllegalAccessException(e);
                    }
                })
            .toList());
    }

    // Deliberately has object-based identity.
    private static class Value {
        final Class<?> cls;

        Value(Class<?> cls) {
            this.cls = cls;
        }

        final String name() {
            // Strip the suffix used by the hidden class.
            String base = cls.getName();
            int slash = base.indexOf('/');
            if (slash <= 0)
                throw new AssertionError(base);
            return base.substring(0, slash);
        }
    }
}
