package enyo.lang.classfile;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An annotation on a class file element.
 *
 * This corresponds to annotation value (not an annotation type).
 *
 * @see Annotated
 */
public final class Annotation extends SizedCached {
    /**
     * Returns a type descriptor for the type of this annotation.
     * For example, the {@link FunctionalInterface} annotation
     * has the descriptor {@code "Ljava/lang/FunctionalInterface;"}.
     * @return a string containing the descriptor for this annotations' type
     */
    public String descriptor() {
        return classFile.constantUtf8At(offset);
    }

    /**
     * Returns the element values for this annotation.
     * @return a stream of {@linkplain NamedElementValue named element values}
     */
    public Stream<NamedElementValue> values() {
        return StreamSupport.stream(
            new NamedElementValueSpliterator(classFile, offset + 2), false);
    }

    /**
     * Returns map from element names to their values.
     * @return a map (of unspecified mutability)
     * @see #mapOfObjects()
     */
    public Map<String, ElementValue> mapOfValues() {
        return values().collect(Collectors.toMap(
                NamedElementValue::name, NamedElementValue::value));
    }

    /**
     * Returns map from element names to their reified values.
     * @return a map (of unspecified mutability)
     * @see #mapOfValues()
     */
    public Map<String, Object> mapOfObjects() {
        return values().collect(Collectors.toMap(
                NamedElementValue::name, NamedElementValue::object));
    }

    @Override
    public String toString() {
        return "Annotation " + descriptor();
    }

    Annotation(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    int computeSize() {
        return computeSize(classFile, offset);
    }

    static int computeSize(ClassFile classFile, int startOffset) {
        int count = classFile.unsignedShortAt(startOffset + 2);
        int offset = startOffset + 4;
        for (int i = 0; i < count; ++i) {
            offset = offset + 2; // Element name.
            offset += ElementValue.computeSize(classFile, offset);
        }
        return offset - startOffset;
    }
}
