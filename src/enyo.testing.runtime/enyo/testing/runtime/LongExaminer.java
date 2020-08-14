package enyo.testing.runtime;

import enyo.testing.Examiner;

import java.util.Objects;

class LongExaminer extends ExaminerBase implements Examiner.OfLong {
    final long value;

    LongExaminer(Testing t, ExaminerBase base, long value) {
        super(t, base);
        this.value = value;
    }

    @Override
    Examined asExamined() {
        return new Examined(context, value);
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
            failure("==", other);
        return this;
    }

    @Override
    public Examiner.OfLong isnt(long other) {
        if (barrier)
            return this;
        if (value == other)
            failure("!=", other);
        return this;
    }

    @Override
    public Examiner<Long> boxed() {
        // Boxing does not add information.  Do not chain to this
        // examiner, but the original examiner.
        return transfer(new ObjectExaminer<>(t, base, value));
    }
}
