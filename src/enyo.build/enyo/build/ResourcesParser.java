package enyo.build;

import enyo.lang.classfile.Annotation;
import enyo.lang.classfile.NamedElementValue;
import enyo.testing.Resources;

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
        if (!ann.descriptor().equals(Descriptors.Resources))
            throw new IllegalArgumentException(ann.descriptor());
        ann.values().forEach(this::parse);
    }

    private TestResources resources = TestResources.DEFAULT;
    private int threads = Resources.THREADS;
    private int memory = Resources.MEMORY;

    private void parse(NamedElementValue nev) {
        switch (nev.name()) {
        case "threads":
            threads = nev.value().asInt();
            break;
        case "memory":
            memory = nev.value().asInt();
            break;
        default:
            throw new AnnotationParseException(
                "Unrecognized @Resources value: " + nev.name());
        }
        try {
            resources = new TestResources(threads, memory);
        } catch (IllegalArgumentException e) {
            throw new AnnotationParseException(
                "Invalid @Resources annotation: " + e.getMessage());
        }
    }
}
