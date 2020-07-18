package enyo.lang.classfile;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Type annotations.
 *
 * They occur as {@code RuntimeVisibleTypeAnnotations} and
 * {@code RuntimeInvisibleTypeAnnotations} on
 * {@linkplain ClassFile class files}, {@linkplain Field fields},
 * {@linkplain Method methods}, * and on
 * {@linkplain CodeAttribute code attributes}.
 */
public final class TypeAnnotationsAttribute extends Attribute {
    /**
     * Returns the attributes in this type annotation attribute.
     * @return a stream of {@linkplain Annotation annotations}
     */
    public Stream<Annotation> stream() {
        return StreamSupport.stream(
            new AnnotationSpliterator(classFile, offset + 6), false);
    }

    TypeAnnotationsAttribute(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    static Stream<Annotation> asAnnotationStream(Stream<Attribute> attrs) {
        return attrs.flatMap(attr -> {
                if (attr.getClass() == TypeAnnotationsAttribute.class)
                        return ((TypeAnnotationsAttribute) attr).stream();
                    else
                        return Stream.of();
            });
    }
}
