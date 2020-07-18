package enyo.lang.classfile;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.Objects;

final class NamedElementValueSpliterator
    extends SizedSpliterator<NamedElementValue> {
    NamedElementValueSpliterator(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    @Override
    NamedElementValue create(int offset) {
        return new NamedElementValue(classFile, offset);
    }

    @Override
    int skip(int offset) {
        return offset + NamedElementValue.computeSize(classFile, offset);
    }
}
