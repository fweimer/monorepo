package enyo.build;

import enyo.lang.classfile.Annotation;
import enyo.lang.classfile.NamedElementValue;
import enyo.testing.ExitDuringTest;

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
        if (!ann.descriptor().equals(Descriptors.ExitDuringTest))
            throw new IllegalArgumentException(ann.descriptor());
        ann.values().forEach(this::parse);
    }

    private boolean flag = true;

    private void parse(NamedElementValue nev) {
        switch (nev.name()) {
        case "value":
            flag = nev.value().asBoolean();
            break;
        default:
            throw new AnnotationParseException(
                "Unrecognized @ExitDuringTest value: " + nev.name());
        }
    }
}
