package enyo.testing.runtime;

import enyo.testing.Examiner;

import java.util.Objects;

class CharExaminer extends ExaminerBase implements Examiner.OfChar {
    final char value;

    CharExaminer(Testing t, ExaminerBase base, char value) {
        super(t, base);
        this.value = value;
    }

    @Override
    Examined asExamined() {
        return new Examined(context, value);
    }

    @Override
    public Examiner.OfChar context(String context) {
        this.context = Objects.requireNonNull(context);
        return this;
    }

    @Override
    public Examiner.OfChar barrier() {
        barrierBase();
        return this;
    }

    @Override
    public Examiner.OfChar is(char other) {
        if (barrier)
            return this;
        if (value != other)
            failure("==", other);
        return this;
    }

    @Override
    public Examiner.OfChar isnt(char other) {
        if (barrier)
            return this;
        if (value == other)
            failure("!=", other);
        return this;
    }

    @Override
    public Examiner<Character> boxed() {
        // Boxing does not add information.  Do not chain to this
        // examiner, but the original examiner.
        return transfer(new ObjectExaminer<>(t, base, value));
    }
}
