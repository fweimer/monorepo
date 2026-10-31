package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.List;

public final class TestChecksInt {
    @Test
    public void testInvalidRange(Testing t) {
        t.expect(() -> Checks.inRange(1, 0),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "inRange: low end greater than high end");
        t.expect(() -> Checks.inRangeClosed(1, 0),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "inRangeClosed: low end greater than high end");
        t.expect(() -> Checks.notInRange(1, 0),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "notInRange: low end greater than high end");
        t.expect(() -> Checks.notInRangeClosed(1, 0),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "notInRangeClosed: low end greater than high end");
    }

    @Test
    public void test(Testing t) {
        t.examine(0)
            .check(Checks.lessThan(1))
            .check(Checks.lessThanOrEquals(0))
            .check(Checks.lessThanOrEquals(1))
            .check(Checks.greaterThanOrEquals(0))
            .check(Checks.greaterThan(-1))
            .check(Checks.inRange(0, 1))
            .check(Checks.inRangeClosed(0, 0))
            .check(Checks.inRangeClosed(0, 1))
            .check(Checks.notInRange(0, 0))
            .check(Checks.notInRange(1, 2))
            .check(Checks.notInRange(-1, 0))
            .check(Checks.notInRangeClosed(-2, -1))
            .check(Checks.notInRangeClosed(1, 2));
    }

    @Test
    public void testFailedLessThan(Testing t) {
        testFailed(t, "failedLessThan1", "lessThan", 1, 1);
        testFailed(t, "failedLessThan2", "lessThan", 1, 0);
    }

    public void failedLessThan1(Testing t) {
        t.examine(1)
            .check(Checks.lessThan(1));
    }

    public void failedLessThan2(Testing t) {
        t.examine(1)
            .check(Checks.lessThan(0));
    }

    @Test
    public void testFailedLessThanOrEquals(Testing t) {
        testFailed(t, "failedLessThanOrEquals", "lessThanOrEquals", 1, 0);
    }

    public void failedLessThanOrEquals(Testing t) {
        t.examine(1)
            .check(Checks.lessThanOrEquals(0));
    }

    @Test
    public void testFailedGreaterThan(Testing t) {
        testFailed(t, "failedGreaterThan1", "greaterThan", 1, 1);
        testFailed(t, "failedGreaterThan2", "greaterThan", 0, 1);
    }

    public void failedGreaterThan1(Testing t) {
        t.examine(1)
            .check(Checks.greaterThan(1));
    }

    public void failedGreaterThan2(Testing t) {
        t.examine(0)
            .check(Checks.greaterThan(1));
    }

    @Test
    public void testFailedGreaterThanOrEquals(Testing t) {
        testFailed(t, "failedGreaterThanOrEquals", "greaterThanOrEquals",
            0, 1);
    }

    public void failedGreaterThanOrEquals(Testing t) {
        t.examine(0)
            .check(Checks.greaterThanOrEquals(1));
    }

    @Test
    public void testFailedInRange(Testing t) {
        testFailed(t, "failedInRange1", "inRange low", 0, 1, 2);
        testFailed(t, "failedInRange2", "inRange high", 2, 1, 2);
        testFailed(t, "failedInRange3", "inRange high", 3, 1, 2);
    }

    public void failedInRange1(Testing t) {
        t.examine(0)
            .check(Checks.inRange(1, 2));
    }

    public void failedInRange2(Testing t) {
        t.examine(2)
            .check(Checks.inRange(1, 2));
    }

    public void failedInRange3(Testing t) {
        t.examine(3)
            .check(Checks.inRange(1, 2));
    }

    @Test
    public void testFailedInRangeClosed(Testing t) {
        testFailed(t, "failedInRangeClosed1", "inRangeClosed low", 0, 1, 2);
        testFailed(t, "failedInRangeClosed2", "inRangeClosed high", 3, 1, 2);
    }

    public void failedInRangeClosed1(Testing t) {
        t.examine(0)
            .check(Checks.inRangeClosed(1, 2));
    }

    public void failedInRangeClosed2(Testing t) {
        t.examine(3)
            .check(Checks.inRangeClosed(1, 2));
    }

    @Test
    public void testFailedNotInRange(Testing t) {
        testFailed(t, "failedNotInRange1", "notInRange", 1, 1, 2);
    }

    public void failedNotInRange1(Testing t) {
        t.examine(1)
            .check(Checks.notInRange(1, 2));
    }

    @Test
    public void testFailedNotInRangeClosed(Testing t) {
        testFailed(t, "failedNotInRangeClosed1", "notInRangeClosed", 1, 1, 2);
        testFailed(t, "failedNotInRangeClosed2", "notInRangeClosed", 2, 1, 2);
    }

    public void failedNotInRangeClosed1(Testing t) {
        t.examine(1)
            .check(Checks.notInRangeClosed(1, 2));
    }

    public void failedNotInRangeClosed2(Testing t) {
        t.examine(2)
            .check(Checks.notInRangeClosed(1, 2));
    }

    private void testFailed(Testing t, String methodName, String check,
        int examinedValue, Integer... others) {
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
