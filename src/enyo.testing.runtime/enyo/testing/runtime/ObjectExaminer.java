package enyo.testing.runtime;

import enyo.testing.Examiner;
import enyo.testing.TestFailure;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
        if (barrier)
            return forceFailed(new ObjectExaminer<U>(t, this, null));
        if (map == null) {
            failure("map (not null)", map);
            return forceFailed(new ObjectExaminer<U>(t, this, null));
        } else {
            U newValue ;
            try {
                newValue = map.apply(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
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
    public <U> Examiner<T> map(Function<? super T, ? extends U> map,
                               Consumer<Examiner<U>> consumer) {
        if (barrier)
            return this;
        if (map == null || consumer == null)
            failureWithObjects("map (not null)", map, consumer);
        else {
            U newValue ;
            try {
                newValue = map.apply(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                // Do not catch errors here, assuming that they should
                // be fatal.
                t.recordException(this, e);
                return this;
            }
            var newExaminer = transfer(new ObjectExaminer<>(t, this, newValue));
            try {
                consumer.accept(newExaminer);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                // Record the exception in the context of the new
                // examiner, providing details about the examined
                // value.
                t.recordException(newExaminer, e);
            }
        }
        return this;
    }

    @Override
    public <U extends T> Examiner<T> map(Class<U> cls,
        Consumer<Examiner<U>> consumer) {
        if (barrier)
            return this;
        if (cls == null || consumer == null)
            failureWithObjects("map (not null)", cls, consumer);
        else {
            U newValue;
            try {
                newValue = cls.cast(value);
            } catch (ClassCastException e) {
                t.recordException(this, e);
                return this;
            }
            // Since we performed a cast, the value is unchanged, so
            // base this examiner on this examiner's base, unless this
            // examiner provides some context.
            ExaminerBase newBase;
            if (context == null)
                newBase = base;
            else
                newBase = this;
            var newExaminer = transfer(
                new ObjectExaminer<>(t, newBase, newValue));
            try {
                consumer.accept(newExaminer);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                // Use the new examiner for possibly expanded context.
                t.recordException(newExaminer, e);
            }
        }
        return this;
    }

    @Override
    public Examiner<T> inspect(Consumer<? super T> consumer) {
        if (barrier)
            return this;
        if (consumer == null)
            failureWithObjects("map (not null)", consumer);
        else {
            try {
                consumer.accept(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                t.recordException(this, e);
            }
        }
        return this;
    }

    @Override
    public <U extends T> Examiner<T> inspect(
        Class<U> cls, Consumer<? super U> consumer) {
        if (barrier)
            return this;
        if (consumer == null)
            failureWithObjects("map (not null)", consumer);
        else {
            U newValue;
            try {
                newValue = cls.cast(value);
            } catch (ClassCastException e) {
                t.recordException(this, e);
                return this;
            }
            try {
                consumer.accept(newValue);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                t.recordException(this, e);
            }
        }
        return this;
    }

    @Override
    public Examiner<T> test(Predicate<? super T> predicate) {
        if (barrier)
            return this;
        if (predicate == null)
            failure("test predicate (not null)");
        else {
            boolean result;
            try {
                result = predicate.test(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                t.recordException(this, e);
                return this;
            }
            if (!result)
                failure("test predicate");
        }
        return this;
    }

    @Override
    public Examiner<T> test(String check, Predicate<? super T> predicate) {
        if (barrier)
            return this;
        if (check == null) {
            failure("test predicate description (not null)");
            check = "test";
            if (predicate == null) {
                failure("test predicate (not null)");
                return this;
            }
        }
        if (predicate == null)
            failure("test predicate (not null)", check);
        else {
            boolean result;
            try {
                result = predicate.test(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                t.recordException(this, e);
                return this;
            }
            if (!result)
                failure(check);
        }
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
