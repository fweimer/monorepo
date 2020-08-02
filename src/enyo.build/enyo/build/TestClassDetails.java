package enyo.build;

import enyo.lang.classfile.ClassFile;
import enyo.lang.classfile.ClassFileException;
import enyo.testing.introspect.TestResources;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Information about a class that contains test methods.
 *
 * The fields of this class are populated while the
 * {@linkplain ClassFile class file} is traversed in the constructor.
 */
final class TestClassDetails {
    final String module;
    final ClassFile clazz;
    private final ArrayList<String> classErrors = new ArrayList<>();

    private final TestResources resources;

    TestClassDetails(String module, ClassFile clazz) {
        this.module = Objects.requireNonNull(module, "module");
        this.clazz = Objects.requireNonNull(clazz, "clazz");

        TestResources resources0 = TestResources.DEFAULT;
        try {
            resources0 = clazz.annotations()
                .filter(ann -> ann.descriptor().equals(Descriptors.Resources))
                .findFirst()
                .map(ResourcesParser::parse)
                .orElse(TestResources.DEFAULT);
        } catch (AnnotationParseException | ClassFileException e) {
            classErrors.add(e.getMessage());
        }
        resources = resources0;
    }

    String className() {
        return clazz.className();
    }

    boolean hasErrors() {
        return !classErrors.isEmpty();
    }

    Stream<String> classErrors() {
        return classErrors.stream();
    }

    TestResources resources() {
        return resources;
    }

    Stream<TestMethodDetails> tests() {
        return clazz.methods()
            .map(method -> new TestMethodDetails(this, method))
            .filter(method -> method.hasTestAnnotation);
    }
}
