package enyo.build;

abstract class ExaminerBase {
    final TestingImpl t;
    String context;
    boolean barrier;
    boolean barrierRequested;
    boolean failed;

    ExaminerBase(TestingImpl t) {
        this.t = t;
    }

    void barrierBase() {
        barrierRequested = true;
        barrier = barrier || failed;
    }

    /**
     * Applies non-value state from this examiner to the other examiner.
     */
    <T extends ExaminerBase> T transfer(T other) {
        other.context = context;
        other.barrier = barrier;
        other.barrierRequested = barrierRequested;
        other.failed = failed;
        return other;
    }

    void failure(String check, Object value) {
        failed = true;
        if (barrierRequested)
            barrier = true;
        t.recordFailure(context, check, value);
    }

    void failure(String check, Object left, Object right) {
        failed = true;
        if (barrierRequested)
            barrier = true;
        t.recordFailure(context, check, left, right);
    }
}
