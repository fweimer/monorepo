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
        if (other.base != this)
            // If the other examiner is based on this one, the chain
            // will provide the context.
            other.context = context;
        other.barrier = barrier;
        other.barrierRequested = barrierRequested;
        other.failed = failed;
        return other;
    }

    /**
     * This can be used to construct a failed examiner after a mapping
     * failure.
     */
    static <T extends ExaminerBase> T forceFailed(T examiner) {
        examiner.barrier = true;
        examiner.barrierRequested = true;
        examiner.failed = true;
        return examiner;
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

    void failureWithObjects(String check, Object... others) {
        failed = true;
        if (barrierRequested)
            barrier = true;
        t.recordExaminerFailure(this, check, others);
    }
}
