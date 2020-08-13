package enyo.testing.runtime;

import enyo.testing.Examiner;

import java.util.Objects;

class LongExaminer extends ExaminerBase implements Examiner.OfLong {
    final long value;

    LongExaminer(Testing t, long value) {
        super(t);
        this.value = value;
    }

    @Override
    public Examiner.OfLong context(String context) {
        context = Objects.requireNonNull(context);
        return this;
    }

    @Override
    public Examiner.OfLong barrier() {
        barrierBase();
        return this;
    }

    @Override
    public Examiner.OfLong is(long other) {
        if (barrier)
            return this;
        if (value != other)
            failure("==", value, other);
        return this;
    }

    @Override
    public Examiner.OfLong isnt(long other) {
        if (barrier)
            return this;
        if (value == other)
            failure("!=", value, other);
        return this;
    }

    @Override
    public Examiner<Long> boxed() {
        return transfer(new ObjectExaminer<>(t, value));
    }
}
