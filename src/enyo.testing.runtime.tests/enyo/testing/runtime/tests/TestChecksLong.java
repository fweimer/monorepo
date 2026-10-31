package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.List;

public final class TestChecksLong {
    @Test
    public void testInvalidRange(Testing t) {
        t.expect(() -> Checks.inRange(1L, 0L),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "inRange: low end greater than high end");
        t.expect(() -> Checks.inRangeClosed(1L, 0L),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "inRangeClosed: low end greater than high end");
        t.expect(() -> Checks.notInRange(1L, 0L),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "notInRange: low end greater than high end");
        t.expect(() -> Checks.notInRangeClosed(1L, 0L),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "notInRangeClosed: low end greater than high end");
    }

    @Test
    public void test(Testing t) {
        t.examine(0L)
            .check(Checks.lessThan(1L))
            .check(Checks.lessThanOrEquals(0L))
            .check(Checks.lessThanOrEquals(1L))
            .check(Checks.greaterThanOrEquals(0L))
            .check(Checks.greaterThan(-1L))
            .check(Checks.inRange(0L, 1L))
            .check(Checks.inRangeClosed(0L, 0L))
            .check(Checks.inRangeClosed(0L, 1L))
            .check(Checks.notInRange(0L, 0L))
            .check(Checks.notInRange(1L, 2L))
            .check(Checks.notInRange(-1L, 0L))
            .check(Checks.notInRangeClosed(-2L, -1L))
            .check(Checks.notInRangeClosed(1L, 2L));
    }

    @Test
    public void testFailedLessThan(Testing t) {
        testFailed(t, "failedLessThan1", "lessThan", 1L, 1L);
        testFailed(t, "failedLessThan2", "lessThan", 1L, 0L);
    }

    public void failedLessThan1(Testing t) {
        t.examine(1L)
            .check(Checks.lessThan(1L));
    }

    public void failedLessThan2(Testing t) {
        t.examine(1L)
            .check(Checks.lessThan(0L));
    }

    @Test
    public void testFailedLessThanOrEquals(Testing t) {
        testFailed(t, "failedLessThanOrEquals", "lessThanOrEquals", 1L, 0L);
    }

    public void failedLessThanOrEquals(Testing t) {
        t.examine(1L)
            .check(Checks.lessThanOrEquals(0L));
    }

    @Test
    public void testFailedGreaterThan(Testing t) {
        testFailed(t, "failedGreaterThan1", "greaterThan", 1L, 1L);
        testFailed(t, "failedGreaterThan2", "greaterThan", 0L, 1L);
    }

    public void failedGreaterThan1(Testing t) {
        t.examine(1L)
            .check(Checks.greaterThan(1L));
    }

    public void failedGreaterThan2(Testing t) {
        t.examine(0L)
            .check(Checks.greaterThan(1L));
    }

    @Test
    public void testFailedGreaterThanOrEquals(Testing t) {
        testFailed(t, "failedGreaterThanOrEquals", "greaterThanOrEquals",
            0L, 1L);
    }

    public void failedGreaterThanOrEquals(Testing t) {
        t.examine(0L)
            .check(Checks.greaterThanOrEquals(1L));
    }

    @Test
    public void testFailedInRange(Testing t) {
        testFailed(t, "failedInRange1", "inRange low", 0L, 1L, 2L);
        testFailed(t, "failedInRange2", "inRange high", 2L, 1L, 2L);
        testFailed(t, "failedInRange3", "inRange high", 3L, 1L, 2L);
    }

    public void failedInRange1(Testing t) {
        t.examine(0L)
            .check(Checks.inRange(1L, 2L));
    }

    public void failedInRange2(Testing t) {
        t.examine(2L)
            .check(Checks.inRange(1L, 2L));
    }

    public void failedInRange3(Testing t) {
        t.examine(3L)
            .check(Checks.inRange(1L, 2L));
    }

    @Test
    public void testFailedInRangeClosed(Testing t) {
        testFailed(t, "failedInRangeClosed1", "inRangeClosed low", 0L, 1L, 2L);
        testFailed(t, "failedInRangeClosed2", "inRangeClosed high", 3L, 1L, 2L);
    }

    public void failedInRangeClosed1(Testing t) {
        t.examine(0L)
            .check(Checks.inRangeClosed(1L, 2L));
    }

    public void failedInRangeClosed2(Testing t) {
        t.examine(3L)
            .check(Checks.inRangeClosed(1L, 2L));
    }

    @Test
    public void testFailedNotInRange(Testing t) {
        testFailed(t, "failedNotInRange1", "notInRange", 1L, 1L, 2L);
    }

    public void failedNotInRange1(Testing t) {
        t.examine(1L)
            .check(Checks.notInRange(1L, 2L));
    }

    @Test
    public void testFailedNotInRangeClosed(Testing t) {
        testFailed(t, "failedNotInRangeClosed1", "notInRangeClosed",
            1L, 1L, 2L);
        testFailed(t, "failedNotInRangeClosed2", "notInRangeClosed",
            2L, 1L, 2L);
    }

    public void failedNotInRangeClosed1(Testing t) {
        t.examine(1L)
            .check(Checks.notInRangeClosed(1L, 2L));
    }

    public void failedNotInRangeClosed2(Testing t) {
        t.examine(2L)
            .check(Checks.notInRangeClosed(1L, 2L));
    }

    private void testFailed(Testing t, String methodName, String check,
        long examinedValue, Long... others) {
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
