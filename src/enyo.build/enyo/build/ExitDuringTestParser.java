package enyo.build;

import enyo.testing.ExitDuringTest;

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;

/**
 * Parser for {@link Resources} annotations.
 */
final class ExitDuringTestParser {
    /**
     * Computes the effective flag in an {@link ExitDuringTest} annotation.
     * @throws IllegalArgumentException if the argument is not a
     *   {@link ExitDuringTest} annotation
     * @throws AnnotationParseException if the annotation is a
     *   {@link ExitDuringTest} annotation, but is malformed
     */
    static boolean parse(Annotation ann) {
        return new ExitDuringTestParser(ann).flag;
    }

    private ExitDuringTestParser(Annotation ann) {
        if (!ann.classSymbol().equals(Descriptors.ExitDuringTest))
            throw new IllegalArgumentException(ann.classSymbol().displayName());
        ann.elements().forEach(this::parse);
    }

    private boolean flag = true;

    private void parse(AnnotationElement elem) {
        if (elem.value() instanceof AnnotationValue.OfBoolean value) {
            switch (elem.name().stringValue()) {
            case "value":
                flag = value.booleanValue();
                break;
            default:
                throw new AnnotationParseException(
                    "Unrecognized @ExitDuringTest value: "
                    + elem.name().stringValue());
            }
        }
    }
}
