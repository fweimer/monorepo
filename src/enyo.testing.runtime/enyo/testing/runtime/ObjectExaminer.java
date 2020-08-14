package enyo.testing.runtime;

import enyo.testing.Examiner;

import java.util.Objects;

class ObjectExaminer<T> extends ExaminerBase implements Examiner<T> {
    final T value;

    ObjectExaminer(Testing t, ExaminerBase base, T value) {
        super(t, base);
        this.value = value;
    }

    @Override
    Examined asExamined() {
        return new Examined(context, value);
    }

    @Override
    public Examiner<T> context(String context) {
        this.context = Objects.requireNonNull(context);
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
        if (value == null || other == null)
            failure("equals (not null)", other);
        else if (!value.equals(other))
            failure("equals", other);
        return this;
    }

    @Override
    public Examiner<T> nullableIs(T other) {
        if (barrier)
            return this;
        if (!Objects.equals(value, other))
            failure("equals", other);
        return this;
    }

    @Override
    public Examiner<T> isnt(T other) {
        if (barrier)
            return this;
        if (value == null || other == null)
            failure("not equals (not null)", other);
        else if (Objects.equals(value, other))
            failure("not equals", other);
        return this;
    }

    @Override
    public Examiner<T> isSame(T other) {
        if (barrier)
            return this;
        if (value == null)
            failure("not null");
        if (other == null)
            failure("not null", other);
        if (value != null && value != other)
            failure("==", other);
        return this;
    }

    @Override
    public Examiner<T> isNotSame(T other) {
        if (barrier)
            return this;
        if (value == null)
            failure("not null");
        if (other == null)
            failure("not null", other);
        if (value != null && value == other)
            failure("!=", other);
        return this;
    }

    @Override
    public Examiner<T> isNull() {
        if (barrier)
            return this;
        if (value != null)
            failure("null");
        return this;
    }

    @Override
    public Examiner<T> isNotNull() {
        if (barrier)
            return this;
        if (value == null)
            failure("not null");
        return this;
    }

    @Override
    public Examiner<T> isExactClass(Class<? extends T> cls) {
        if (barrier)
            return this;
        if (value == null)
            failure("not null");
        if (cls == null)
            failure("not null", cls);
        if (value != null && cls != null && value.getClass() != cls)
            failure("exact class", cls);
        return this;
    }
}
