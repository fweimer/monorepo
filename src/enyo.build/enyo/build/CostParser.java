package enyo.build;

import enyo.lang.classfile.Annotation;
import enyo.lang.classfile.NamedElementValue;
import enyo.testing.Cost;

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
        if (!ann.descriptor().equals(Descriptors.Cost))
            throw new IllegalArgumentException(ann.descriptor());
        ann.values().forEach(this::parse);
    }

    private double cost = Cost.DEFAULT;

    private void parse(NamedElementValue nev) {
        switch (nev.name()) {
        case "value":
            cost = nev.value().asDouble();
            break;
        default:
            throw new AnnotationParseException(
                "Unrecognized @Cost value: " + nev.name());
        }
        if (cost <= 0.0)
            throw new AnnotationParseException("Invalid @Cost value: " + cost);
    }
}
