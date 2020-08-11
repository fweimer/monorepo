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
