package enyo.build;

import enyo.testing.OnlyTests;

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;

/**
 * Parser for {@link OnlyTests} annotations.
 */
final class OnlyTestsParser {
    /**
     * Computes the effective flag in an {@link OnlyTests} annotation.
     * @throws IllegalArgumentException if the argument is not a
     *   {@link OnlyTests} annotation
     * @throws AnnotationParseException if the annotation is a
     *   {@link OnlyTests} annotation, but is malformed
     */
    static String[] parse(Annotation ann) {
        return new OnlyTestsParser(ann).value;
    }

    private OnlyTestsParser(Annotation ann) {
        if (!ann.classSymbol().equals(Descriptors.OnlyTests))
            throw new IllegalArgumentException(ann.classSymbol().displayName());
        ann.elements().forEach(this::parse);
    }

    private String[] value;

    private void parse(AnnotationElement elem) {
        if (elem.value() instanceof AnnotationValue.OfArray array) {
            switch (elem.name().stringValue()) {
            case "value":
                value = array.values().stream()
                    .map(e1 -> {
                            if (e1 instanceof AnnotationValue.OfString e2)
                                return e2.stringValue();
                            else
                                throw new AnnotationParseException(
                                    "Unrecognized @OnlyTests value: "
                                    + e1.toString());
                                    })
                    .toArray(String[]::new);
                break;
            default:
                throw new AnnotationParseException(
                    "Unrecognized @OnlyTests value: "
                    + elem.name().stringValue());
            }
        }
    }
}
