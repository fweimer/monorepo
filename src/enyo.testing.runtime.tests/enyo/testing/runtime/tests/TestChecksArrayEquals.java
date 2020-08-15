package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.Arrays;
import java.util.List;

public final class TestChecksArrayEquals {
    @Test
    public void testNull(Testing t) {
        t.expect(() -> Checks.arrayEquals((Object[]) null),
            NullPointerException.class);
    }

    @Test
    public void testEmptyEmpty(Testing t) {
        t.examine(new String[0])
            .check(Checks.arrayEquals(new String[0]));
    }

    @Test
    public void testNullNull(Testing t) {
        t.examine(new String[1])
            .check(Checks.arrayEquals(new String[1]));
    }

    @Test
    public void testStringString(Testing t) {
        t.examine(new String[] { "string" })
            .check(Checks.arrayEquals(new String("string")));
    }

    @Test
    public void testExaminedNull(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "examinedNull");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("arrayEquals (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNull)
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
    }

    public void examinedNull(Testing t) {
        t.examine((Object[]) null)
            .check(Checks.arrayEquals());
    }

    @Test
    public void testNullEmpty(Testing t) {
        checkOneFailure(t, "nullEmpty", new String[1], new Object[0]);
    }

    public void nullEmpty(Testing t) {
        t.examine(new String[1])
            .check(Checks.arrayEquals());
    }

    @Test
    public void testEmptyNull(Testing t) {
        checkOneFailure(t, "emptyNull", new String[0], new Object[1]);
    }

    public void emptyNull(Testing t) {
        t.examine(new String[0])
            .check(Checks.arrayEquals((String) null));
    }

    @Test
    public void testStringEmpty(Testing t) {
        checkOneFailure(t, "stringEmpty", new String[] { "string" },
            new Object[0]);
    }

    public void stringEmpty(Testing t) {
        t.examine(new String[] { "string" })
            .check(Checks.arrayEquals());
    }

    @Test
    public void testEmptyString(Testing t) {
        checkOneFailure(t, "emptyString", new String[0],
            new Object[] { "string" });
    }

    public void emptyString(Testing t) {
        t.examine(new String[0])
            .check(Checks.arrayEquals("string"));
    }

    @Test
    public void testStringNull(Testing t) {
        checkOneFailure(t, "stringNull", new String[] { "string" },
            new Object[1]);
    }

    public void stringNull(Testing t) {
        t.examine(new String[] { "string" })
            .check(Checks.arrayEquals((String) null));
    }

    @Test
    public void testNullString(Testing t) {
        checkOneFailure(t, "nullString", new String[1],
            new Object[] { "string" });
    }

    public void nullString(Testing t) {
        t.examine(new String[1])
            .check(Checks.arrayEquals("string"));
    }

    @Test
    public void testStringUnequal1(Testing t) {
        checkOneFailure(t, "stringUnequal1", new String[] { "string1" },
            new Object[] { "string2" });
    }

    public void stringUnequal1(Testing t) {
        t.examine(new String[] { "string1" })
            .check(Checks.arrayEquals("string2"));
    }

    @Test
    public void testStringUnequal2(Testing t) {
        checkOneFailure(t, "stringUnequal2", new String[] { "string1" },
            new Object[] { "string1", "string2" });
    }

    public void stringUnequal2(Testing t) {
        t.examine(new String[] { "string1" })
            .check(Checks.arrayEquals("string1", "string2"));
    }

    @Test
    public void testStringUnequal3(Testing t) {
        checkOneFailure(t, "stringUnequal3",
            new String[] { "string1", "string2" },
            new Object[] { "string1" });
    }

    public void stringUnequal3(Testing t) {
        t.examine(new String[] { "string1", "string2" })
            .check(Checks.arrayEquals("string1"));
    }

    private void checkOneFailure(Testing t, String methodName,
        Object[] examinedArray, Object[] otherArray) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), methodName);
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("arrayEquals"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> (Object[]) call.context.get(0).value(),
                    e1 -> e1
                    .map(array -> array.length,
                        e2 -> e2.is(examinedArray.length))
                    .test(array -> Arrays.equals(examinedArray, array)))
                .map(call -> call.otherObjects,
                    e1 -> e1
                    .map(array -> array.length,
                        e2 -> e2.is(otherArray.length))
                    .test(array -> Arrays.equals(otherArray, array))));
    }
}
