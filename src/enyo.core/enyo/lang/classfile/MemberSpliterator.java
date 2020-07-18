package enyo.lang.classfile;

import java.util.Spliterator;
import java.util.function.Consumer;

abstract class MemberSpliterator<T extends Member>
    extends SizedSpliterator<T> {
    MemberSpliterator(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    abstract T create(int offset);

    final int skip(int offset) {
        return Attribute.skipArray(classFile, offset + 6);
    }
}
