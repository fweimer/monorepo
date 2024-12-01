package enyo.build;

import enyo.testing.Cost;

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;

/**
 * Parser for {@link Resources} annotations.
 */
final class CostParser {
    /**
     * Computes cost from a {@link Cost} annotation.
     * @throws IllegalArgumentException if the argument is not a
     *   {@link Cost} annotation
     * @throws AnnotationParseException if the annotation is a
     *   {@link Cost} annotation, but is malformed
     */
    static double parse(Annotation ann) {
        return new CostParser(ann).cost;
    }

    private CostParser(Annotation ann) {
        if (!ann.classSymbol().equals(Descriptors.Cost))
            throw new IllegalArgumentException(ann.classSymbol().displayName());
        ann.elements().forEach(this::parse);
    }

    private double cost = Cost.DEFAULT;

    private void parse(AnnotationElement elem) {
        if (elem.value() instanceof AnnotationValue.OfDouble value) {
            switch (elem.name().stringValue()) {
            case "value":
                cost = value.doubleValue();
                break;
            default:
                throw new AnnotationParseException(
                    "Unrecognized @Cost value: " + elem.name().stringValue());
            }
        }
        if (cost <= 0.0)
            throw new AnnotationParseException("Invalid @Cost value: " + cost);
    }
}
