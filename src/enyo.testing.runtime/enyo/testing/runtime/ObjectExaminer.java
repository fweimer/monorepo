package enyo.testing.runtime;

import enyo.testing.Examiner;

import java.util.Objects;
import java.util.function.Function;

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
    public <U> Examiner<U> map(Function<? super T, ? extends U> map) {
        if (map == null) {
            failure("map (not null)", map);
            return forceFailed(new ObjectExaminer<U>(t, this, null));
        } else {
            U newValue ;
            try {
                newValue = map.apply(value);
            } catch (Exception e) {
                // Do not catch errors here, assuming that they should
                // be fatal.
                t.recordException(this, e);
                return forceFailed(new ObjectExaminer<U>(t, this, null));
            }
            return transfer(new ObjectExaminer<>(t, this, newValue));
        }
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
        if (value == null || other == null)
            failure("== (not null)", other);
        else if (value != other)
            failure("==", other);
        return this;
    }

    @Override
    public Examiner<T> isNotSame(T other) {
        if (barrier)
            return this;
        if (value == null || other == null)
            failure("!= (not null)", other);
        else if (value == other)
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
        if (value == null || cls == null)
            failure("exact class (not null)", cls);
        else if (value.getClass() != cls)
            failure("exact class", cls);
        return this;
    }
}
