package enyo.build;

import enyo.testing.Examiner;

import java.util.Objects;

class ObjectExaminer<T> extends ExaminerBase implements Examiner<T> {
    final T value;

    ObjectExaminer(TestingImpl t, T value) {
        super(t);
        this.value = value;
    }

    @Override
    public Examiner<T> context(String context) {
        context = Objects.requireNonNull(context);
        return this;
    }

    @Override
    public Examiner<T> barrier() {
        barrierBase();
        return this;
    }

    @Override
    public Examiner<T> is(T other) {
        if (barrier)
            return this;
        if (value == null)
            failure("not null", value);
        if (other == null)
            failure("not null", other);
        if (!Objects.equals(value, other))
            failure("equals", value, other);
        return this;
    }

    @Override
    public Examiner<T> nullableIs(T other) {
        if (barrier)
            return this;
        if (!Objects.equals(value, other))
            failure("equals", value, other);
        return this;
    }

    @Override
    public Examiner<T> isnt(T other) {
        if (barrier)
            return this;
        if (value == null)
            failure("not null", value);
        if (other == null)
            failure("not null", other);
        if (Objects.equals(value, other))
            failure("not equals", value, other);
        return this;
    }

    @Override
    public Examiner<T> isSame(T other) {
        if (barrier)
            return this;
        if (value == null)
            failure("not null", value);
        if (other == null)
            failure("not null", other);
        if (value != null && value != other)
            failure("==", value, other);
        return this;
    }

    @Override
    public Examiner<T> isNotSame(T other) {
        if (barrier)
            return this;
        if (value == null)
            failure("not null", value);
        if (other == null)
            failure("not null", other);
        if (value != null && value == other)
            failure("!=", value, other);
        return this;
    }

    @Override
    public Examiner<T> isNull() {
        if (barrier)
            return this;
        if (value != null)
            failure("null", value);
        return this;
    }

    @Override
    public Examiner<T> isNotNull() {
        if (barrier)
            return this;
        if (value == null)
            failure("not null", value);
        return this;
    }

    @Override
    public Examiner<T> isExactClass(Class<? extends T> cls) {
        if (barrier)
            return this;
        if (value == null)
            failure("not null", value);
        if (cls == null)
            failure("not null", cls);
        if (value != null && cls != null && value.getClass() != cls)
            failure("exact class", value, cls);
        return this;
    }
}
