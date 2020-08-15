package enyo.testing.runtime.tests;

import enyo.testing.Check;
import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

public class TestCheckFailure {
    @Test
    public static void testEmpty(Testing t) {
        t.examine(new Check.Failure(""))
            .map(Check.Failure::check, e -> e.is(""))
            .map(Check.Failure::objects,
                e -> e.test(array -> array.length == 0));
    }

    @Test
    public static void testWithCheck(Testing t) {
        String name = "name";
        t.examine(new Check.Failure(name))
            .map(Check.Failure::check, e -> e.isSame(name))
            .map(Check.Failure::objects,
                e -> e.test(array -> array.length == 0));
    }

    @Test
    public static void testWithObjects(Testing t) {
        String name = "name";
        var obj = new Object() { };
        t.examine(new Check.Failure(name, obj))
            .map(Check.Failure::check, e -> e.isSame(name ))
            .map(f -> f.objects().length, e -> e.is(1))
            .map(f -> f.objects()[0], e -> e.isSame(obj));
    }

    @Test
    public static void testToString(Testing t) {
        String name = "name";
        t.examine(new Check.Failure(name))
            .map(Object::toString)
            .isSame(name);
    }

    @Test
    public static void testNullCheck(Testing t) {
        t.expect(() -> new Check.Failure(null), NullPointerException.class);
        t.expect(() -> new Check.Failure(null, ""), NullPointerException.class);
    }

    @Test
    public static void testNullObjects(Testing t) {
        t.expect(() -> new Check.Failure("", (Object[]) null),
            NullPointerException.class);
        t.expect(() -> new Check.Failure("1", (Object[]) null),
            NullPointerException.class);
    }

    @Test
    public static void testOrder() {
        Order<Check.Failure> order = new Order<>();
        order.group()
            .add(new Check.Failure(""))
            .add(new Check.Failure("", new Object[0]))
            .close();
        order.group()
            .add(new Check.Failure("1"))
            .add(new Check.Failure("1", new Object[0]))
            .close();
        order.group()
            .add(new Check.Failure("", 1))
            .add(new Check.Failure("", new Object[] { 1 }))
            .close();
        order.group()
            .add(new Check.Failure("", (Object) null))
            .add(new Check.Failure("", new Object[1]))
            .close();
        order.group()
            .add(new Check.Failure("", "object"))
            .add(new Check.Failure("", new String("object")))
            .close();
        order.check();
    }
}
