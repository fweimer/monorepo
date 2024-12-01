package enyo.build;

import enyo.testing.Cost;
import enyo.testing.introspect.TestResources;

import java.io.File;
import java.io.IOException;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Information about a class that contains test methods.
 *
 * The fields of this class are populated while the
 * {@linkplain ClassFile class file} is traversed in the constructor.
 */
final class TestClassDetails {
    final String module;
    final ClassModel cm;
    private final ArrayList<String> classErrors = new ArrayList<>();

    private final TestResources resources;
    private final double cost;
    private final boolean exitDuringTest;

    // Test filter.  Non-empty array or null (no filter).
    private final String[] onlyTests;

    // Long.MIN_VALUE + 1 if unknown (to help with negative sorting).
    private final long sourceFileChange;

    TestClassDetails(DirectoryLayout dirs, String module, ClassModel cm) {
        this.module = Objects.requireNonNull(module, "module");
        this.cm = Objects.requireNonNull(cm, "cm");

        var attrs = cm.findAttribute(Attributes.runtimeInvisibleAnnotations())
            .map(attr -> attr.annotations())
            .orElse(List.of());

        TestResources resources0 = TestResources.DEFAULT;
        try {
            resources0 = attrs.stream()
                .filter(ann -> ann.classSymbol().equals(Descriptors.Resources))
                .findFirst()
                .map(ResourcesParser::parse)
                .orElse(TestResources.DEFAULT);
        } catch (AnnotationParseException | IllegalArgumentException e) {
            classErrors.add(e.getMessage());
        }
        resources = resources0;

        {
            double cost0 = Cost.DEFAULT;
            try {
                cost0 = attrs.stream()
                    .filter(ann -> ann.classSymbol().equals(
                            Descriptors.Cost))
                    .findFirst()
                    .map(CostParser::parse)
                    .orElse(Cost.DEFAULT);
            } catch (AnnotationParseException | IllegalArgumentException e) {
                classErrors.add(e.getMessage());
            }
            cost = cost0;
        }

        {
            boolean exitDuringTest0 = false;
            try {
                exitDuringTest0 = attrs.stream()
                    .filter(ann -> ann.classSymbol().equals(
                            Descriptors.ExitDuringTest))
                    .findFirst()
                    .map(ExitDuringTestParser::parse)
                    .orElse(false);
            } catch (AnnotationParseException | IllegalArgumentException e) {
                classErrors.add(e.getMessage());
            }
            exitDuringTest = exitDuringTest0;
        }

        {
            String[] onlyTests0 = null;
            try {
                onlyTests0 = attrs.stream()
                    .filter(ann -> ann.classSymbol().equals(
                            Descriptors.OnlyTests))
                    .findFirst()
                    .map(OnlyTestsParser::parse)
                    .orElse(null);
            } catch (AnnotationParseException | IllegalArgumentException e) {
                classErrors.add(e.getMessage());
            }
            onlyTests = onlyTests0;
        }

        sourceFileChange = guessSourcePath(dirs, module, cm)
            .flatMap(TestClassDetails::fileModificationTime)
            .orElse(Long.MIN_VALUE + 1);
    }

    String className() {
        var cd = cm.thisClass().asSymbol();
        if (!cd.isClassOrInterface())
            throw new IllegalStateException("Not a class or interface: " + cd);
        // FIXME: This is not ideal.  We should probably use the
        // binary name for serialization purposes, not the Java name.
        String pkg = cd.packageName();
        if (pkg.isEmpty())
            return cd.displayName();
        else
            return pkg + '.' + cd.displayName();
    }

    boolean hasErrors() {
        return !classErrors.isEmpty();
    }

    boolean hasTestFilters() {
        return onlyTests != null;
    }

    Stream<String> classErrors() {
        return classErrors.stream();
    }

    TestResources resources() {
        return resources;
    }

    double cost() {
        return cost;
    }

    boolean exitDuringTest() {
        return exitDuringTest;
    }

    Stream<TestMethodDetails> tests() {
        Stream<TestMethodDetails> stream = cm.methods()
            .stream()
            .map(method -> new TestMethodDetails(this, method))
            .filter(method -> method.hasTestAnnotation);
        if (onlyTests != null)
            stream = stream.filter(method ->
                Arrays.binarySearch(onlyTests,
                    method.method.methodName().stringValue()) >= 0);
        return stream;
    }

    long sourceFileChange() {
        return sourceFileChange;
    }

    static Optional<Path> guessSourcePath(DirectoryLayout dirs, String module,
        ClassModel cm) {
        String packagePath = cm.thisClass().asSymbol().packageName()
            .replace('.', File.separatorChar);
        return cm.findAttributes(Attributes.sourceFile())
            .stream()
            .findFirst()
            .<String>map(attr -> attr.sourceFile().stringValue())
            .map(fileName ->
                dirs.sources()
                .resolve(module)
                .resolve(packagePath)
                .resolve(fileName));
    }

    static Optional<Long> fileModificationTime(Path path) {
        try {
            return Optional.of(Files.getLastModifiedTime(path).toMillis());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
