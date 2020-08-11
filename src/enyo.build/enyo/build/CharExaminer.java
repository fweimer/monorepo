package enyo.build;

import enyo.testing.Examiner;

import java.util.Objects;

class CharExaminer extends ExaminerBase implements Examiner.OfChar {
    final char value;

    CharExaminer(TestingImpl t, char value) {
        super(t);
        this.value = value;
    }

    @Override
    public Examiner.OfChar context(String context) {
        context = Objects.requireNonNull(context);
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
            failure("==", value, other);
        return this;
    }

    @Override
    public Examiner.OfChar isnt(char other) {
        if (barrier)
            return this;
        if (value == other)
            failure("!=", value, other);
        return this;
    }

    @Override
    public Examiner<Character> boxed() {
        return transfer(new ObjectExaminer<>(t, value));
    }
}
