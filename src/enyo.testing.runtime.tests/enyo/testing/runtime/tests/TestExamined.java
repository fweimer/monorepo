package enyo.testing.runtime.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.runtime.Examined;

public final class TestExamined {
    @Test
    public void test(Testing t) {
        {
            Examined e = new Examined(null, null);
            t.examine(e.context()).isNull();
            t.examine(e.value()).isNull();
            t.examine(e.toString()).is("[]: null");
        }

        {
            String context = "context string";
            Examined e = new Examined(context, null);
            t.examine(e.context()).isSame(context);
            t.examine(e.value()).isNull();
            t.examine(e.toString()).is("[context string]: null");
        }

        {
            Object value = TestExamined.class;
            Examined e = new Examined(null, value);
            t.examine(e.context()).isNull();
            t.examine(e.value()).isSame(value);
            t.examine(e.toString()).is("[]: " + value);
        }

        {
            String context = "context string";
            Object value = TestExamined.class;
            Examined e = new Examined(context, value);
            t.examine(e.context()).isSame(context);
            t.examine(e.value()).isSame(value);
            t.examine(e.toString()).is("[context string]: " + value);
        }
    }

    @Test
    public void testToString(Testing t) {
        {
            var nullToString = new Object() {
                    @Override
                    public String toString() {
                        return null;
                    }
                };

            Examined e = new Examined(null, nullToString);
            t.examine(e.context()).isNull();
            t.examine(e.value()).isSame(nullToString);
            t.examine(e.toString()).is("[]: null");

            String context = "context string";
            e = new Examined(context, nullToString);
            t.examine(e.context()).isSame(context);
            t.examine(e.value()).isSame(nullToString);
            t.examine(e.toString()).is("[context string]: null");
        }

        {
            var throwingToString = new Object() {
                    @Override
                    public String toString() {
                        throw new UnsupportedOperationException();
                    }
                };

            Examined e = new Examined(null, throwingToString);
            t.examine(e.context()).isNull();
            t.examine(e.value()).isSame(throwingToString);
            t.examine(e.toString()).is("[]: <error: "
                + UnsupportedOperationException.class.getName() + '>');

            String context = "context string";
            e = new Examined(context, throwingToString);
            t.examine(e.context()).isSame(context);
            t.examine(e.value()).isSame(throwingToString);
            t.examine(e.toString()).is("[context string]: <error: "
                + UnsupportedOperationException.class.getName() + '>');
        }
    }

    @Test
    public void testOrder() {
        Order<Examined> order = new Order<>();
        order.group()
            .add(new Examined(null, null))
            .add(new Examined(null, null))
            .close();
        order.group()
            .add(new Examined("context1", null))
            .add(new Examined("context1", null))
            .close();
        order.group()
            .add(new Examined(null, 1))
            .add(new Examined(null, 1))
            .close();
        order.group()
            .add(new Examined("context1", 1))
            .add(new Examined("context1", 1))
            .close();
        order.group()
            .add(new Examined("context2", null))
            .add(new Examined("context2", null))
            .close();
        order.group()
            .add(new Examined(null, 2))
            .add(new Examined(null, 2))
            .close();
        order.group()
            .add(new Examined("context2", 2))
            .add(new Examined("context2", 2))
            .close();
        order.check();
    }
}
