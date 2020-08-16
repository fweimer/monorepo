package enyo.testing.runtime;

import enyo.testing.Examiner;
import enyo.testing.TestFailure;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

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
        this.context = Objects.requireNonNull(context);
        return this;
    }

    @Override
    public Examiner.OfInt barrier() {
        barrierBase();
        return this;
    }

    @Override
    public Examiner.OfInt map(IntUnaryOperator map) {
        if (barrier)
            return this;
        if (map == null) {
            failure("map (not null)", map);
            return forceFailed(new IntExaminer(t, this, 0));
        } else {
            int newValue ;
            try {
                newValue = map.applyAsInt(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                // Do not catch errors here, assuming that they should
                // be fatal.
                t.recordException(this, e);
                return forceFailed(new IntExaminer(t, this, 0));
            }
            return transfer(new IntExaminer(t, this, newValue));
        }
    }

    @Override
    public  Examiner.OfInt map(IntUnaryOperator map,
        Consumer<Examiner.OfInt> consumer) {
        if (barrier)
            return this;
        if (map == null || consumer == null)
            failureWithObjects("map (not null)", map, consumer);
        else {
            int newValue ;
            try {
                newValue = map.applyAsInt(value);
            } catch (TestFailure e) {
                // Abort the test.
                throw e;
            } catch (Exception e) {
                // Do not catch errors here, assuming that they should
                // be fatal.
                t.recordException(this, e);
                return this;
            }
            var newExaminer = transfer(new IntExaminer(t, this, newValue));
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
