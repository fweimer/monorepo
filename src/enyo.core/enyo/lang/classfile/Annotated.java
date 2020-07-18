package enyo.lang.classfile;

import java.util.stream.Stream;

/**
 * A class file element that has annotations.
 *
 * @see Annotation
 */
public interface Annotated {
    /**
     * Returns a stream of annotations associated on this element.
     * @return a stream of {@linkplain Annotation annotations}
     */
    Stream<Annotation> annotations();
}
