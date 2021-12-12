package enyo.testing;

import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Testing ordering behavior of classes.
 *
 * This class can check both the {@link Object#equals(Object)}
 * and {@link Object#hashCode()} methods for consistency, and ordering
 * based on {@link Comparable} and {@link Comparator}s.
 *
 * <p>Individual objects are registered using {@link #add(Object)}.
 * Groups of objects which compare equal with each other can be created
 * using {@link #group()}.
 *
 * <p>If ordering checks are used, the order in which the elements and
 * groups are added must match the sorted order.  Otherwise, only the
 * grouping of the elements matters.  (The order within a
 * {@linkplain Group group} never matters.)
 *
 * @param <T> type of the class which is being checked
 */
public final class Order<T> {
    /**
     * Creates a new order checker.
     * Initially, there are no ordeded elements.
     */
    public Order() {
    }

    /**
     * Adds an element that is not equal to any other element.
     * Conceptually, this indicates that the equivalence class of the
     * value is a singleton set.
     * @param element the element to add
     * @throws IllegalStateException
     *   if there is an open {@linkplain Group group}
     * @throws NullPointerException if {@code element} is null
     */
    public void add(T element) {
        checkNoGroup();
        Objects.requireNonNull(element);
        elements.add(new Element<T>(element, index, 2));
        ++index;
    }

    /**
     * Starts a group of elements which compare equal to each other.
     * These elements are not equal to any other elements.
     * @return the new group
     * @throws IllegalStateException
     *   if there is an open {@linkplain Group group}
     * @see Group#add(Object)
     */
    public Group group() {
        checkNoGroup();
        currentGroup =  new Group();
        return currentGroup;
    }

    /**
     * A group of elements that compare equal.
     *
     * Conceptually, this is an equivalence class of values.
     *
     * <p>Groups are populated using {@link #add(Object)}.  After the
     * last element as been added, {@link #close()} should be called.
     */
    public class Group {
        /**
         * Adds another element to this group.
         * @param element the element to add
         * @return this group
         * @throws IllegalStateException if this group has been
         *   {@linkplain #close() closed}
         * @throws NullPointerException if {@code element} is null
         * @see Order#add(Object)
         * @see #close()
         */
        public Group add(T element) {
            if (currentGroup != this)
                throw new IllegalStateException("Group is closed");
            Objects.requireNonNull(element);
            elements.add(new Element<T>(element, index, 2));
            return this;
        }

        /**
         * Closes this group.
         * @throws IllegalStateException if this group has already been
         *   {@linkplain #close() closed}
         */
        public void close() {
            if (currentGroup != this)
                throw new IllegalStateException("Group is closed");
            currentGroup = null;
            ++index;
        }

        private Group() {
        }
    }

    /**
     * Returns the values added so far.
     * @return a list of elements; it is unspecified if the list is
     *   modifiable
     */
    public List<T> values() {
        return elements.stream()
            .map(e -> e.value)
            .collect(Collectors.toList());
    }

    /**
     * Performs consistency checks on {@link Object#equals(Object)}
     * and {@link Object#hashCode()}.
     * Ordered comparision via {@link Comparable} is not checked.x
     */
    public void check() {
        checkNoGroup();
        for (int i = 0; i < elements.size(); ++i) {
            Element<T> left = elements.get(i);
            if (left.value.equals(null))
                throw new ObjectFailure(buildLabel(i) + ": equals null",
                    Failed.EQUALS_NULL, left.value);
            for (int j = 0; j < elements.size(); ++j) {
                Element<T> right = elements.get(j);
                if (left.index == right.index) {
                    if (!left.value.equals(right.value)) {
                        String label = buildLabel(i, "==", j);
                        throw new PairFailure(label, FailedPair.EQUALS,
                            0, left.value, right.value);
                    }
                    if (left.value.hashCode() != right.value.hashCode()) {
                        String label = buildLabel(i, "=(hash)=", j);
                        throw new PairFailure(label, FailedPair.HASH_CODE,
                            0, left.value, right.value);
                    }
                } else {
                    if (left.value.equals(right.value)) {
                        String label = buildLabel(i, "!=", j);
                        throw new PairFailure(label, FailedPair.NOT_EQUALS,
                            0, left.value, right.value);
                    }
                }
            }
        }
    }

    /**
     * Checks for consistent ordering according to a comparator.
     * Also checks  {@link Object#equals(Object)}
     * and {@link Object#hashCode()}, like {@link #check()}.
     * @param comparator the comparator whose ordering is checked
     * @throws NullPointerException if {@code comparator} is null
     * @see #check()
     * @see #checkComparatorOnly(Comparator)
     */
    public void check(Comparator<T> comparator) {
        check();
        checkComparatorOnly(comparator);
    }

    /**
     * Checks for consistent ordering according to a comparator.
     * @param comparator the comparator against which the element
     *   ordering is checked
     * @throws NullPointerException if {@code comparator} is null
     */
    public void checkComparatorOnly(Comparator<T> comparator) {
        for (int i = 0; i < elements.size(); ++i) {
            Element<T> left = elements.get(i);

            for (int j = 0; j < elements.size(); ++j) {
                Element<T> right = elements.get(j);
                int expected = Integer.signum(Integer.compare(
                        left.index, right.index));
                int actual = Integer.signum(comparator.compare(
                        left.value, right.value));
                if (expected != actual) {
                    String label = String.format(
                        "%s: expected <%d>, actual <%d>",
                        buildLabel(i, "<=>", j), expected, actual);
                    throw new PairFailure(label, FailedPair.COMPARE_TO,
                        expected, left.value, right.value);
                }
            }
        }
    }

    /**
     * Checks the consistency of the {@link Comparable} implemention.
     * Also checks  {@link Object#equals(Object)}
     * and {@link Object#hashCode()}, like {@link #check()}.
     * @param cls the type of the objects; this type must implement
     *   {@link Comparable}, and the added values must be assignable
     *   to this type
     * @see #check()
     * @throws NullPointerException if {@code cls} is null
     * @throws ClassFailure if the class does not implement
     *   {@link Comparable}
     */
    public void checkComparable(Class<?> cls) {
        if (!Comparable.class.isAssignableFrom(cls))
            throw new ClassFailure(cls);
        check();

        // Dynamically check that the stored values are Comparable
        // instances.
        for (int i = 0; i < elements.size(); ++i) {
            Element<T> left = elements.get(i);
            if (!cls.isAssignableFrom(left.value.getClass()))
                throw new ObjectFailure(
                    buildLabel(i) + ": not comparable as " + cls.getName(),
                    Failed.COMPARABLE, left.value);
        }

        for (int i = 0; i < elements.size(); ++i) {
            Element<T> left = elements.get(i);

            // The raw type is needed for compareTo(Comparable) call
            // below.  It is not possible to statically prove that
            // the generic types are correct.
            @SuppressWarnings("rawtypes")
            Comparable leftValue = (Comparable) left.value;

            for (int j = 0; j < elements.size(); ++j) {
                Element<T> right = elements.get(j);
                @SuppressWarnings("rawtypes")
                Comparable rightValue = (Comparable) right.value;
                int expected = Integer.signum(Integer.compare(
                        left.index, right.index));
                @SuppressWarnings("unchecked")
                int actual = Integer.signum(
                    leftValue.compareTo(rightValue));
                if (expected != actual) {
                    String label = String.format(
                        "%s: expected <%d>, actual <%d>",
                        buildLabel(i, "<=>", j), expected, actual);
                    throw new PairFailure(label, FailedPair.COMPARE_TO,
                        expected, leftValue, rightValue);
                }
            }
        }
    }

    /**
     * Converts the values to a different type, preserving ordering.
     * @param map the function used to convert the values
     * @param <U> the type of the resulting order
     * @return a new order object
     * @throws NullPointerException if {@code map} is null
     */
    public <U> Order<U> map(Function<? super T, U> map) {
        checkNoGroup();
        Order<U> result = new Order<U>();
        result.index = index;
        result.elements.ensureCapacity(elements.size());
        for (var e : elements)
            result.elements.add(new Element<U>(
                    map.apply(e.value), e.index, e.stack));
        return result;
    }

    /**
     * Base class for ordering test failures.
     */
    public static abstract class Failure extends TestFailure {
        private Failure(String message) {
            super(message);
        }

        private static final long serialVersionUID = 0x825bf4a580d8f846L;
    }

    /**
     * The type of pair-based ordering failure.
     */
    public enum FailedPair {
        /**
         * Two values that should compare equal do not.
         */
        EQUALS,

        /**
         * Two values that should not compare equal do.
         */
        NOT_EQUALS,

        /**
         * Two values which compare equal do not have the same hash code.
         */
        HASH_CODE,

        /**
         * The ordering according to {@link Comparable#compareTo(Object)}
         * or {@link Comparator#compare(Object,Object)} is incorrect.
         */
        COMPARE_TO
    }

    /**
     * An ordering check failure that involves a pair of values.
     */
    public static final class PairFailure extends Failure {
        private PairFailure(String message, FailedPair failed,
            int comparison, Object left, Object right) {
            super(message);
            this.failed = Objects.requireNonNull(failed, "failed");
            if (comparison < -1 || comparison > 1
                || (failed != FailedPair.COMPARE_TO && comparison != 0))
                throw new IllegalArgumentException("comparison");
            this.comparison = comparison;
            this.left = Objects.requireNonNull(left, "left");
            this.right = Objects.requireNonNull(right, "right");
        }

        /**
         * The failure type.
         * @return an enumeration value describing the kind of test failure
         */
        public FailedPair failed() {
            return failed;
        }

        /**
         * The sign of the expected comparison result.
         * @return an ordering indicator, either -1, 0, or 1
         *   (0 if not available)
         * @see FailedPair#COMPARE_TO
         */
        public int expectedComparison() {
            return comparison;
        }

        /**
         * Returns the value on the left side of the comparison.
         * @return an object involved in the failure
         */
        public Object left() {
            return left;
        }

        /**
         * Returns the value on the right side of the comparison.
         * @return an object involved in the failure
         */
        public Object right() {
            return right;
        }

        /**
         * The failure type.
         * @see #failed()
         */
        private final FailedPair failed;

        /**
         * The sign of the expected comparison result.
         * @see #expectedComparison()
         */
        private final int comparison;

        /**
         * An object involved in the failure.
         * @see #left()
         */
        @SuppressWarnings("serial") // Serializable if input object is.
        private final Object left;

        /**
         * An object involved in the failure.
         * @see #right()
         */
        @SuppressWarnings("serial") // Serializable if input object is.
        private final Object right;

        private static final long serialVersionUID = 0xf08e2470b894fd41L;
    }

    /**
     * The type of a test failure that involves only one object.
     */
    public enum Failed {
        /**
         * The {@link Object#equals(Object)} method returns true for a
         * null argument.
         */
        EQUALS_NULL,

        /**
         * The value is not {@linkplain Comparable comparable}.
         * @see Order#checkComparable(Class)
         */
        COMPARABLE
    }

    /**
     * An ordering check failure that involves a single value.
     */
    public static final class ObjectFailure extends Failure {
        private ObjectFailure(String message, Failed failed, Object object) {
            super(message);
            this.failed = Objects.requireNonNull(failed);
            this.object = Objects.requireNonNull(object);
        }

        /**
         * Returns the type of check that failed.
         * @return the failed check
         */
        public Failed failed() {
            return failed;
        }

        /**
         * Returns the value for which the check failed.
         * @return the failed object
         */
        public Object object() {
            return object;
        }

        /**
         * The failed check.
         * @see #failed()
         */
        private final Failed failed;

        /**
         * The failed object.
         * @see #object()
         */
        @SuppressWarnings("serial") // Serializable if input object is.
        private final Object object;

        private static final long serialVersionUID = 0x6fcf01370dd5176eL;
    }

    /**
     * An ordering check failure that applies to entire class.
     */
    public static final class ClassFailure extends Failure {
        private ClassFailure(Class<?> failingClass) {
            super(String.format("Class %s does not implement Comparable",
                    failingClass.getName()));
            this.failingClass = failingClass;
        }

        /**
         * Returns the class that failed.
         * @return a class
         */
        public Class<?> failingClass() {
            return failingClass;
        }

        /**
         * The class that failed.
         * @see #failingClass()
         */
        private final Class<?> failingClass;

        private static final long serialVersionUID = 0xaff4aa20f514e577L;
    }

    private int index;
    private Group currentGroup;
    ArrayList<Element<T>> elements = new ArrayList<>();

    private void checkNoGroup() {
        if (currentGroup != null)
            throw new IllegalStateException("Open group present");
    }

    private String buildLabel(int i) {
        if (elements.size() == 1)
            return "[0] " + elements.get(0).value.toString();
        else {
            // Try a neighbor to shorten the stack depth.
            int shared;
            if (i == 0)
                shared = sharedStackSuffix(0, 1);
            else
                shared = sharedStackSuffix(i - 1, i);
            return buildLabel(i, shared);
        }
    }

    private String buildLabel(int i, String op, int j) {
        int shared = sharedStackSuffix(i, j);
        return buildLabel(i, shared) + ' ' + op + ' ' + buildLabel(j, shared);
    }

    private int sharedStackSuffix(int i, int j) {
        List<StackFrame> left = elements.get(i).stack;
        List<StackFrame> right = elements.get(j).stack;
        int shared = 0;
        int size = Math.min(left.size(), right.size());
        while (shared < size) {
            // Use StackTraceElement because it implements equals().
            StackTraceElement leftFrame
                = left.get(left.size() - shared - 1).toStackTraceElement();
            StackTraceElement rightFrame =
                right.get(right.size() - shared - 1).toStackTraceElement();
            if (!leftFrame.equals(rightFrame))
                break;
            ++shared;
        }
        return shared;
    }

    private String buildLabel(int i, int shared) {
        var sb = new StringBuilder();
        sb.append('[');
        sb.append(i);
        sb.append("] ");
        Element<T> element = elements.get(i);
        sb.append(element.value.toString());
        sb.append(" {");
        boolean first = true;
        for (int j = 0; j < element.stack.size() - shared; ++j) {
            if (first)
                first = false;
            else
                sb.append("; ");
            sb.append(element.stack.get(j).toStackTraceElement().toString());
        }
        sb.append('}');
        return sb.toString();
    }

    private static class Element<T> {
        private final T value;
        private final int index;
        private final ArrayList<StackFrame> stack;

        private Element(T value, int index, int skip) {
            this.value = Objects.requireNonNull(value);
            this.index = index;
            stack = new ArrayList<>();
            StackWalker.getInstance().forEach(new Consumer<StackFrame>() {
                    int skipRemaining = skip;
                    public void accept(StackFrame frame) {
                        if (skipRemaining > 0)
                            --skipRemaining;
                        else
                            stack.add(frame);
                    }
                });
            stack.trimToSize();
        }

        private Element(T value, int index, ArrayList<StackFrame> stack) {
            this.value = value;
            this.index = index;
            this.stack = stack;
        }

        public boolean equals(Object o) {
            throw new UnsupportedOperationException();
        }

        public int hashCode() {
            throw new UnsupportedOperationException();
        }
    }

}
