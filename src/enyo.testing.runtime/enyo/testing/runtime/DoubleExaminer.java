package enyo.testing.runtime;

import enyo.testing.Examiner;

import java.util.Objects;

class DoubleExaminer extends ExaminerBase implements Examiner.OfDouble {
    final double value;

    DoubleExaminer(Testing t, ExaminerBase base, double value) {
        super(t, base);
        this.value = value;
    }

    @Override
    Examined asExamined() {
        return new Examined(context, value);
    }

    @Override
    public Examiner.OfDouble context(String context) {
        this.context = Objects.requireNonNull(context);
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
            failure("==", other);
        return this;
    }

    @Override
    public Examiner.OfDouble isnt(double other) {
        if (barrier)
            return this;
        if (value == other)
            failure("!=", other);
        return this;
    }

    @Override
    public Examiner<Double> boxed() {
        // Boxing does not add information.  Do not chain to this
        // examiner, but the original examiner.
        return transfer(new ObjectExaminer<>(t, base, value));
    }
}
