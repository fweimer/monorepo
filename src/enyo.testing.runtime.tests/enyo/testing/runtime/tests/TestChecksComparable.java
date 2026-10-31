package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.List;

public final class TestChecksComparable {
    @Test
    public void testNull(Testing t) {
        t.expect(() -> Checks.lessThan(null),
            NullPointerException.class);
        t.expect(() -> Checks.lessThanOrEquals(null),
            NullPointerException.class);
        t.expect(() -> Checks.greaterThan(null),
            NullPointerException.class);
        t.expect(() -> Checks.greaterThanOrEquals(null),
            NullPointerException.class);
        t.expect(() -> Checks.inRange(null, null),
            NullPointerException.class);
        t.expect(() -> Checks.inRange("", null),
            NullPointerException.class);
        t.expect(() -> Checks.inRange(null, ""),
            NullPointerException.class);
        t.expect(() -> Checks.inRangeClosed(null, null),
            NullPointerException.class);
        t.expect(() -> Checks.inRangeClosed("", null),
            NullPointerException.class);
        t.expect(() -> Checks.inRangeClosed(null, ""),
            NullPointerException.class);
        t.expect(() -> Checks.notInRange(null, null),
            NullPointerException.class);
        t.expect(() -> Checks.notInRange("", null),
            NullPointerException.class);
        t.expect(() -> Checks.notInRange(null, ""),
            NullPointerException.class);
        t.expect(() -> Checks.notInRangeClosed(null, null),
            NullPointerException.class);
        t.expect(() -> Checks.notInRangeClosed("", null),
            NullPointerException.class);
        t.expect(() -> Checks.notInRangeClosed(null, ""),
            NullPointerException.class);
    }

    @Test
    public void testInvalidRange(Testing t) {
        t.expect(() -> Checks.inRange("low", "high"),
            IllegalArgumentException.class)
	    .map(Exception::getMessage).is(
                "inRange: low end greater than high end");
        t.expect(() -> Checks.inRangeClosed("low", "high"),
            IllegalArgumentException.class)
	    .map(Exception::getMessage).is(
                "inRangeClosed: low end greater than high end");
        t.expect(() -> Checks.notInRange("low", "high"),
            IllegalArgumentException.class)
	    .map(Exception::getMessage).is(
                "notInRange: low end greater than high end");
        t.expect(() -> Checks.notInRangeClosed("low", "high"),
            IllegalArgumentException.class)
	    .map(Exception::getMessage).is(
                "notInRangeClosed: low end greater than high end");
    }

    @Test
    public void test(Testing t) {
        t.examine("")
            .check(Checks.lessThan("\0"))
            .check(Checks.lessThanOrEquals("\0"))
            .check(Checks.greaterThanOrEquals(""))
            .check(Checks.inRange("", "\0"))
            .check(Checks.inRangeClosed("", ""))
            .check(Checks.notInRange("", ""))
            .check(Checks.notInRange("\0", "\0"))
            .check(Checks.notInRangeClosed("\0", "\0"));
        t.examine("\0")
            .check(Checks.lessThanOrEquals("\0"))
            .check(Checks.greaterThan(""))
            .check(Checks.greaterThanOrEquals("\0"))
            .check(Checks.inRange("", "\0\0"))
            .check(Checks.inRangeClosed("", "\0"))
            .check(Checks.inRange("\0", "\0\0"))
            .check(Checks.inRangeClosed("\0", "\0"))
            .check(Checks.notInRange("", "\0"))
            .check(Checks.notInRange("\0", "\0"))
            .check(Checks.notInRangeClosed("", ""))
            .check(Checks.notInRangeClosed("\0\0", "\0\0"));
    }

    @Test
    public void testNullLessThan(Testing t) {
        testNull(t, "nullLessThan", "lessThan", "string");
    }

    public void nullLessThan(Testing t) {
        t.examine((String) null)
            .check(Checks.lessThan("string"));
    }

    @Test
    public void testNullLessThanOrEquals(Testing t) {
        testNull(t, "nullLessThanOrEquals", "lessThanOrEquals", "string");
    }

    public void nullLessThanOrEquals(Testing t) {
        t.examine((String) null)
            .check(Checks.lessThanOrEquals("string"));
    }

    @Test
    public void testNullGreaterThan(Testing t) {
        testNull(t, "nullGreaterThan", "greaterThan", "string");
    }

    public void nullGreaterThan(Testing t) {
        t.examine((String) null)
            .check(Checks.greaterThan("string"));
    }

    @Test
    public void testNullGreaterThanOrEquals(Testing t) {
        testNull(t, "nullGreaterThanOrEquals", "greaterThanOrEquals", "string");
    }

    public void nullGreaterThanOrEquals(Testing t) {
        t.examine((String) null)
            .check(Checks.greaterThanOrEquals("string"));
    }

    @Test
    public void testNullInRange(Testing t) {
        testNull(t, "nullInRange", "inRange", "from", "to");
    }

    public void nullInRange(Testing t) {
        t.examine((String) null)
            .check(Checks.inRange("from", "to"));
    }

    @Test
    public void testNullInRangeClosed(Testing t) {
        testNull(t, "nullInRangeClosed", "inRangeClosed", "from", "to");
    }

    public void nullInRangeClosed(Testing t) {
        t.examine((String) null)
            .check(Checks.inRangeClosed("from", "to"));
    }

    @Test
    public void testNullNotInRange(Testing t) {
        testNull(t, "nullNotInRange", "notInRange", "from", "to");
    }

    public void nullNotInRange(Testing t) {
        t.examine((String) null)
            .check(Checks.notInRange("from", "to"));
    }

    @Test
    public void testNullNotInRangeClosed(Testing t) {
        testNull(t, "nullNotInRangeClosed", "notInRangeClosed", "from", "to");
    }

    public void nullNotInRangeClosed(Testing t) {
        t.examine((String) null)
            .check(Checks.notInRangeClosed("from", "to"));
    }

    private void testNull(Testing t, String methodName, String check,
                          Object... others) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), methodName);
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        check + " (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNull)
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals(others))));
    }

    @Test
    public void testFailedLessThan(Testing t) {
        testFailed(t, "failedLessThan1", "lessThan", "1", "1");
        testFailed(t, "failedLessThan2", "lessThan", "1", "0");
    }

    public void failedLessThan1(Testing t) {
        t.examine("1")
            .check(Checks.lessThan("1"));
    }

    public void failedLessThan2(Testing t) {
        t.examine("1")
            .check(Checks.lessThan("0"));
    }

    @Test
    public void testFailedLessThanOrEquals(Testing t) {
        testFailed(t, "failedLessThanOrEquals", "lessThanOrEquals", "1", "0");
    }

    public void failedLessThanOrEquals(Testing t) {
        t.examine("1")
            .check(Checks.lessThanOrEquals("0"));
    }

    @Test
    public void testFailedGreaterThan(Testing t) {
        testFailed(t, "failedGreaterThan1", "greaterThan", "1", "1");
        testFailed(t, "failedGreaterThan2", "greaterThan", "0", "1");
    }

    public void failedGreaterThan1(Testing t) {
        t.examine("1")
            .check(Checks.greaterThan("1"));
    }

    public void failedGreaterThan2(Testing t) {
        t.examine("0")
            .check(Checks.greaterThan("1"));
    }

    @Test
    public void testFailedGreaterThanOrEquals(Testing t) {
        testFailed(t, "failedGreaterThanOrEquals", "greaterThanOrEquals",
            "0", "1");
    }

    public void failedGreaterThanOrEquals(Testing t) {
        t.examine("0")
            .check(Checks.greaterThanOrEquals("1"));
    }

    @Test
    public void testFailedInRange(Testing t) {
        testFailed(t, "failedInRange1", "inRange low", "0", "1", "2");
        testFailed(t, "failedInRange2", "inRange high", "2", "1", "2");
        testFailed(t, "failedInRange3", "inRange high", "3", "1", "2");
    }

    public void failedInRange1(Testing t) {
        t.examine("0")
            .check(Checks.inRange("1", "2"));
    }

    public void failedInRange2(Testing t) {
        t.examine("2")
            .check(Checks.inRange("1", "2"));
    }

    public void failedInRange3(Testing t) {
        t.examine("3")
            .check(Checks.inRange("1", "2"));
    }

    @Test
    public void testFailedInRangeClosed(Testing t) {
        testFailed(t, "failedInRangeClosed1", "inRangeClosed low",
            "0", "1", "2");
        testFailed(t, "failedInRangeClosed2", "inRangeClosed high",
            "3", "1", "2");
    }

    public void failedInRangeClosed1(Testing t) {
        t.examine("0")
            .check(Checks.inRangeClosed("1", "2"));
    }

    public void failedInRangeClosed2(Testing t) {
        t.examine("3")
            .check(Checks.inRangeClosed("1", "2"));
    }

    @Test
    public void testFailedNotInRange(Testing t) {
        testFailed(t, "failedNotInRange1", "notInRange",
            "1", "1", "2");
        testFailed(t, "failedNotInRange2", "notInRange",
            "10", "1", "2");
    }

    public void failedNotInRange1(Testing t) {
        t.examine("1")
            .check(Checks.notInRange("1", "2"));
    }

    public void failedNotInRange2(Testing t) {
        t.examine("10")
            .check(Checks.notInRange("1", "2"));
    }

    @Test
    public void testFailedNotInRangeClosed(Testing t) {
        testFailed(t, "failedNotInRangeClosed1", "notInRangeClosed",
            "1", "1", "2");
        testFailed(t, "failedNotInRangeClosed2", "notInRangeClosed",
            "2", "1", "2");
    }

    public void failedNotInRangeClosed1(Testing t) {
        t.examine("1")
            .check(Checks.notInRangeClosed("1", "2"));
    }

    public void failedNotInRangeClosed2(Testing t) {
        t.examine("2")
            .check(Checks.notInRangeClosed("1", "2"));
    }

    private void testFailed(Testing t, String methodName, String check,
        String examinedValue, String... others) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), methodName);
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(check))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(examinedValue))
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals((Object[]) others))));
    }
}
