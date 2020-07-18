package enyo.lang.classfile;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An attribute that stores multiple annotations on a class, field, or
 * method.
 *
 * Attributes can be visible or invisible at run time, as determined
 * by {@link #isVisible()}.
 */
public final class AnnotationsAttribute extends Attribute {
    /**
     * Returns a new stream supplying the contained annotations.
     * @return a stream of annotations
     */
    public Stream<Annotation> stream() {
        return StreamSupport.stream(
            new AnnotationSpliterator(classFile, offset + 6), false);
    }

    /**
     * Returns true if the annotaiton is loaded at run time.
     *
     * @return whether the annotation is visible at run time
     */
    public boolean isVisible() {
        return name().equals(Constants.STRING_RuntimeVisibleAnnotations);
    }

    AnnotationsAttribute(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    static Stream<Annotation> asAnnotationStream(Stream<Attribute> attrs) {
        return attrs.flatMap(attr -> {
                if (attr.getClass() == AnnotationsAttribute.class)
                        return ((AnnotationsAttribute) attr).stream();
                    else
                        return Stream.of();
            });
    }
}
