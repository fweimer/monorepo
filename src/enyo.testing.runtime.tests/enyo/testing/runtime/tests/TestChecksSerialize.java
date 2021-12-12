package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public final class TestChecksSerialize {
    @Test
    public static void testNull(Testing t) {
        t.<Serializable>examine(null)
            .check(Checks.serialize());
    }

    @Test
    public static void testString(Testing t) {
        t.examine("X".repeat(5000))
            .check(Checks.serialize());
    }

    @Test
    public static void testList(Testing t) {
        t.examine((Serializable) List.of(1, 2, 3))
            .check(Checks.serialize());
    }

    @Test
    public static void testCustom(Testing t) {
        t.examine(new Custom(17))
            .check(Checks.serialize());
    }

    private record Custom(int value) implements Serializable { }

    @Test
    public void testNotSerializable(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "notSerializable");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("serialize (write)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.map(Object::getClass).is(NotSerializable.class))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                // NotSerializableException for java.util.Optional.
                .map(call -> call.otherObjects[0])
                .map(NotSerializableException.class)
                .map(Throwable::getMessage)
                .is(Optional.class.getName()));
    }

    // Expected test failure
    public static void notSerializable(Testing t) {
        t.examine(new NotSerializable())
            .check(Checks.serialize());
    }

    @SuppressWarnings("serial") // Optional is not serializable.
    private final static class NotSerializable implements Serializable {
        Optional<String> optional = Optional.of("");
    }
}
