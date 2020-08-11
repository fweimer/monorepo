package enyo.build;

import enyo.testing.Examiner;

import java.util.Objects;

class DoubleExaminer extends ExaminerBase implements Examiner.OfDouble {
    final double value;

    DoubleExaminer(TestingImpl t, double value) {
        super(t);
        this.value = value;
    }

    @Override
    public Examiner.OfDouble context(String context) {
        context = Objects.requireNonNull(context);
        return this;
    }

    @Override
    public Examiner.OfDouble barrier() {
        if (barrier)
            return this;
        barrierBase();
        return this;
    }

    @Override
    public Examiner.OfDouble is(double other) {
        if (value != other)
            failure("==", value, other);
        return this;
    }

    @Override
    public Examiner.OfDouble isnt(double other) {
        if (barrier)
            return this;
        if (value == other)
            failure("!=", value, other);
        return this;
    }

    @Override
    public Examiner<Double> boxed() {
        return transfer(new ObjectExaminer<>(t, value));
    }
}
