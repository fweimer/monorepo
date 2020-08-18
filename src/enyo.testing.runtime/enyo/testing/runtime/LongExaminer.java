package enyo.testing.runtime;

import enyo.testing.Check;
import enyo.testing.Examiner;
import enyo.testing.TestFailure;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

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
        this.context = Objects.requireNonNull(context);
        return this;
    }

    @Override
    public Examiner.OfLong barrier() {
        barrierBase();
        return this;
    }

    @Override
    public Examiner.OfLong map(LongUnaryOperator map) {
        if (barrier)
            return this;
        if (map == null) {
            failure("map (not null)", map);
            return forceFailed(new LongExaminer(t, this, 0));
        } else {
            long newValue ;
            try {
                newValue = map.applyAsLong(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                // Do not catch errors here, assuming that they should
                // be fatal.
                t.recordException(this, e);
                return forceFailed(new LongExaminer(t, this, 0));
            }
            return transfer(new LongExaminer(t, this, newValue));
        }
    }

    @Override
    public  Examiner.OfLong map(LongUnaryOperator map,
        Consumer<Examiner.OfLong> consumer) {
        if (barrier)
            return this;
        if (map == null || consumer == null)
            failureWithObjects("map (not null)", map, consumer);
        else {
            long newValue ;
            try {
                newValue = map.applyAsLong(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                // Do not catch errors here, assuming that they should
                // be fatal.
                t.recordException(this, e);
                return this;
            }
            var newExaminer = transfer(new LongExaminer(t, this, newValue));
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
    public <T> Examiner<T> mapToObj(LongFunction<? extends T> map) {
        if (barrier)
            return forceFailed(new ObjectExaminer<>(t, this, null));
        if (map == null) {
            failure("mapToObj (not null)", map);
            return forceFailed(new ObjectExaminer<>(t, this, null));
        } else {
            T newValue;
            try {
                newValue = map.apply(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                // Do not catch errors here, assuming that they should
                // be fatal.
                t.recordException(this, e);
                return forceFailed(new ObjectExaminer<>(t, this, null));
            }
            return transfer(new ObjectExaminer<>(t, this, newValue));
        }
    }

    @Override
    public <T> Examiner.OfLong mapToObj(LongFunction<? extends T> map,
        Consumer<Examiner<T>> consumer) {
        if (barrier)
            return this;
        if (map == null || consumer == null)
            failureWithObjects("mapToObj (not null)", map, consumer);
        else {
            T newValue ;
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
    public Examiner.OfLong inspect(LongConsumer consumer) {
        if (barrier)
            return this;
        if (consumer == null)
            failureWithObjects("inspect (not null)", consumer);
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
    public Examiner.OfLong check(Check.OfLong check) {
        if (barrier)
            return this;
        if (check == null)
            failure("check (not null)");
        else {
            Check.Failure result;
            try {
                result = check.test(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                t.recordException(this, e);
                return this;
            }
            if (result != null)
                failureWithObjects(result.check(), result.objects());
        }
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
