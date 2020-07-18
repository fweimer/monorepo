package enyo.lang.classfile;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

abstract class SizedSpliterator<T extends Sized> extends SizedCached
    implements Spliterator<T> {
    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.NONNULL;
    }

    @Override
    public long estimateSize() {
        return count - index;
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (index < count) {
            T obj = create(currentOffset);
            ++index;
            action.accept(obj);
            currentOffset += obj.size();
            return true;
        } else
            return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (; index < count; ++index) {
            if (false)
            System.out.format("%s: creating object %d at %d%n",
                getClass().getName(), index, currentOffset);
            T obj = create(currentOffset);
            action.accept(obj);
            currentOffset += obj.size();
        }
    }

    private final int count;
    private int index;
    private int currentOffset;

    SizedSpliterator(ClassFile classFile, int offset) {
        super(classFile, offset);
        count = classFile.unsignedShortAt(offset);
        currentOffset = offset + 2;
            if (false)
        System.out.format("%s: count=%d offset=%d%n",
            getClass().getName(), count, currentOffset);
    }

    abstract T create(int offset);
    abstract int skip(int offset);

    final int computeSize() {
        int off = currentOffset;
        for (int i = index; i < count; ++i)
            off = skip(off);
        return off - offset;
    }
}
