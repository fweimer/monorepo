package enyo.build;

import enyo.lang.classfile.Annotation;
import enyo.lang.classfile.NamedElementValue;
import enyo.testing.ExitDuringTest;

import java.util.Arrays;

/**
 * Parser for {@link OnlyTests} annotations.
 */
final class OnlyTestsParser {
    /**
     * Computes the effective flag in an {@link ExitDuringTest} annotation.
     * @throws IllegalArgumentException if the argument is not a
     *   {@link ExitDuringTest} annotation
     * @throws AnnotationParseException if the annotation is a
     *   {@link ExitDuringTest} annotation, but is malformed
     */
    static String[] parse(Annotation ann) {
        return new OnlyTestsParser(ann).value;
    }

    private OnlyTestsParser(Annotation ann) {
        if (!ann.descriptor().equals(Descriptors.OnlyTests))
            throw new IllegalArgumentException(ann.descriptor());
        ann.values().forEach(this::parse);
    }

    private String[] value;

    private void parse(NamedElementValue nev) {
        switch (nev.name()) {
        case "value":
            Object[] value0 = (Object[]) nev.value().object();
            if (value0.length == 0)
                value = null;
            else {
                value = new String[value0.length];
                System.arraycopy(value0, 0, value, 0, value.length);
                Arrays.sort(value);
            }
            break;
        default:
            throw new AnnotationParseException(
                "Unrecognized @ExitDuringTest value: " + nev.name());
        }
    }
}
