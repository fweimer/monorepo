package enyo.build;

import enyo.lang.classfile.ClassFile;
import enyo.lang.classfile.ClassFileException;
import enyo.lang.classfile.SourceFileAttribute;
import enyo.testing.Cost;
import enyo.testing.introspect.TestResources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    final ClassFile clazz;
    private final ArrayList<String> classErrors = new ArrayList<>();

    private final TestResources resources;
    private final double cost;

    // Long.MIN_VALUE + 1 if unknown (to help with negative sorting).
    private final long sourceFileChange;

    TestClassDetails(DirectoryLayout dirs, String module, ClassFile clazz) {
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

        {
            double cost0 = Cost.DEFAULT;
            try {
                cost0 = clazz.annotations()
                    .filter(ann -> ann.descriptor().equals(Descriptors.Cost))
                    .findFirst()
                    .map(CostParser::parse)
                    .orElse(Cost.DEFAULT);
            } catch (AnnotationParseException | ClassFileException e) {
                classErrors.add(e.getMessage());
            }
            cost = cost0;
        }

        sourceFileChange = guessSourcePath(dirs, module, clazz)
            .flatMap(TestClassDetails::fileModificationTime)
            .orElse(Long.MIN_VALUE + 1);
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

    double cost() {
        return cost;
    }

    Stream<TestMethodDetails> tests() {
        return clazz.methods()
            .map(method -> new TestMethodDetails(this, method))
            .filter(method -> method.hasTestAnnotation);
    }

    long sourceFileChange() {
        return sourceFileChange;
    }

    static Optional<Path> guessSourcePath(DirectoryLayout dirs, String module,
        ClassFile classFile) {
        String packagePath;
        {
            String name = classFile.className();
            int lastSlash = name.lastIndexOf('/');
            if (lastSlash < 0)
                return Optional.empty();
            String substring = name.substring(0, lastSlash);
            if (File.separatorChar != '/')
                substring = substring.replace('/', File.separatorChar);
            packagePath = substring;
        }
        return classFile.attributes()
            .filter(attr -> attr instanceof SourceFileAttribute)
            .findFirst()
            .<String>map(attr -> ((SourceFileAttribute) attr).sourceFile())
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
