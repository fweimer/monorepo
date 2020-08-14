package enyo.testing.runtime;

import enyo.testing.Examiner;

import java.util.Objects;

class IntExaminer extends ExaminerBase implements Examiner.OfInt {
    final int value;

    IntExaminer(Testing t, ExaminerBase base, int value) {
        super(t, base);
        this.value = value;
    }

    @Override
    Examined asExamined() {
        return new Examined(context, value);
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
            failure("==", other);
        return this;
    }

    @Override
    public Examiner.OfInt isnt(int other) {
        if (barrier)
            return this;
        if (value == other)
            failure("!=", other);
        return this;
    }

    @Override
    public Examiner<Integer> boxed() {
        // Boxing does not add information.  Do not chain to this
        // examiner, but the original examiner.
        return transfer(new ObjectExaminer<>(t, base, value));
    }
}
