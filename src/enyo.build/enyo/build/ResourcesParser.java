package enyo.build;

import enyo.testing.Resources;
import enyo.testing.introspect.TestResources;

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;

/**
 * Parser for {@link Resources} annotations.
 */
final class ResourcesParser {
    /**
     * Computes the {@link TestResources} from a {@link Resources} annotation.
     * @throws IllegalArgumentException if the argument is not a
     *   {@link Resources} annotation
     * @throws AnnotationParseException if the annotation is a
     *   {@link Resources} annotation, but is malformed
     */
    static TestResources parse(Annotation ann) {
        return new ResourcesParser(ann).resources;
    }

    private ResourcesParser(Annotation ann) {
        if (!ann.classSymbol().equals(Descriptors.Resources))
            throw new IllegalArgumentException(ann.classSymbol().displayName());
        ann.elements().forEach(this::parse);
    }

    private TestResources resources = TestResources.DEFAULT;
    private int threads = Resources.THREADS;
    private int memory = Resources.MEMORY;

    private void parse(AnnotationElement elem) {
        if (elem.value() instanceof AnnotationValue.OfInt value) {
            switch (elem.name().stringValue()) {
            case "threads":
                threads = value.intValue();
                break;
            case "memory":
                memory = value.intValue();
                break;
            default:
                throw new AnnotationParseException(
                    "Unrecognized @Resources value: "
                    + elem.name().stringValue());
            }
        }
        try {
            resources = new TestResources(threads, memory);
        } catch (IllegalArgumentException e) {
            throw new AnnotationParseException(
                "Invalid @Resources annotation: " + e.getMessage());
        }
    }
}
