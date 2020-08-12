package enyo.testing.runtime;

import enyo.testing.Examiner;

import java.util.Objects;

class IntExaminer extends ExaminerBase implements Examiner.OfInt {
    final int value;

    IntExaminer(Testing t, int value) {
        super(t);
        this.value = value;
    }

    @Override
    public Examiner.OfInt context(String context) {
        context = Objects.requireNonNull(context);
        return this;
    }

    @Override
    public Examiner.OfInt barrier() {
        barrierBase();
        return this;
    }

    @Override
    public Examiner.OfInt is(int other) {
        if (barrier)
            return this;
        if (value != other)
            failure("==", value, other);
        return this;
    }

    @Override
    public Examiner.OfInt isnt(int other) {
        if (barrier)
            return this;
        if (value == other)
            failure("!=", value, other);
        return this;
    }

    @Override
    public Examiner<Integer> boxed() {
        return transfer(new ObjectExaminer<>(t, value));
    }
}
