package enyo.lang.classfile;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.Objects;

final class AnnotationSpliterator extends SizedSpliterator<Annotation> {
    AnnotationSpliterator(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    @Override
    Annotation create(int offset) {
        return new Annotation(classFile, offset);
    }

    @Override
    int skip(int offset) {
        return offset + Annotation.computeSize(classFile, offset);
    }
}
