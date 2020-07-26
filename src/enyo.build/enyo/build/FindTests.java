package enyo.build;

import enyo.io.function.IOFunction;
import enyo.io.function.IOPredicate;
import enyo.lang.classfile.ClassFile;
import enyo.util.StringFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class FindTests {
    static boolean isTestModule(DirectoryLayout dirs, int generation,
        String module) throws IOException {
        Path modulePath = dirs.buildGeneration(generation).resolve(module);
        Path moduleInfoPath = modulePath.resolve("module-info.class");
        // If this throws, the module has not been compiled correctly.
        byte[] blob = Files.readAllBytes(moduleInfoPath);
        ClassFile clazz = ClassFile.of(ByteBuffer.wrap(blob));
        return clazz.annotations()
            .anyMatch(ann -> ann.descriptor().equals(Descriptors.TestModule));
    }

    static List<String> onlyTestModules(DirectoryLayout dirs, int generation,
        List<String> modules) {
        return modules.stream()
            .filter(IOPredicate.of(
                    mod -> FindTests.isTestModule(dirs, generation, mod)))
            .collect(Collectors.toUnmodifiableList());
    }

    static Stream<TestMethodDetails> tests(DirectoryLayout dirs, int generation,
        String module) throws IOException {
        return potentialTestClasses(
            classFiles(dirs, generation, module), module)
            .flatMap(TestClassDetails::tests);
    }
    static Stream<Path> classFiles(DirectoryLayout dirs, int generation,
        String module) throws IOException {
        Path modulePath = dirs.buildGeneration(generation).resolve(module);
        return Files.walk(modulePath)
            .filter(path -> path.getFileName().toString().endsWith(".class"));
    }

    static boolean potentialTestClass(ClassFile clazz) {
        return clazz.utf8MatchesAny(testClassFilter);
    }

    static Stream<TestClassDetails> potentialTestClasses(
        Stream<Path> stream, String module) {
        return stream.flatMap(IOFunction.of(path -> {
                    byte[] blob = Files.readAllBytes(path);
                    ClassFile clazz = ClassFile.of(ByteBuffer.wrap(blob));
                    if (potentialTestClass(clazz))
                        return Stream.of(new TestClassDetails(module, clazz));
                    else
                        return Stream.of();
                }));
    }

    private static final StringFilter testClassFilter =
        StringFilter.of(Descriptors.Test);

    private FindTests() { } // No instances.
}
