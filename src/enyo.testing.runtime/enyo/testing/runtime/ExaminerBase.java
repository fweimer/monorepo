package enyo.testing.runtime;

abstract class ExaminerBase {
    final Testing t;
    final ExaminerBase base;
    String context;
    boolean barrier;
    boolean barrierRequested;
    boolean failed;

    ExaminerBase(Testing t, ExaminerBase base) {
        this.t = t;
        this.base = base;
    }

    void barrierBase() {
        barrierRequested = true;
        barrier = barrier || failed;
    }

    /**
     * Produces an examined value from this examiner.
     */
    abstract Examined asExamined();

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

    void failure(String check) {
        failed = true;
        if (barrierRequested)
            barrier = true;
        t.recordFailure(this, check);
    }

    void failure(String check, Object other) {
        failed = true;
        if (barrierRequested)
            barrier = true;
        t.recordFailure(this, check, other);
    }
}
