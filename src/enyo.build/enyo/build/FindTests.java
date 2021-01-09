package enyo.build;

import enyo.io.function.IOFunction;
import enyo.io.function.IOPredicate;
import enyo.lang.classfile.ClassFile;
import enyo.testing.introspect.ScheduledTest;
import enyo.util.Result;
import enyo.util.StringFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

final class FindTests {
    static boolean isTestModule(DirectoryLayout dirs, String module)
        throws IOException {
        Path modulePath = dirs.buildGeneration().resolve(module);
        Path moduleInfoPath = modulePath.resolve("module-info.class");
        // If this throws, the module has not been compiled correctly.
        byte[] blob = Files.readAllBytes(moduleInfoPath);
        ClassFile clazz = ClassFile.of(ByteBuffer.wrap(blob));
        return clazz.annotations()
            .anyMatch(ann -> ann.descriptor().equals(Descriptors.TestModule));
    }

    static List<String> onlyTestModules(DirectoryLayout dirs,
        List<String> modules) {
        return modules.stream()
            .filter(IOPredicate.of(mod -> isTestModule(dirs, mod)))
            .collect(Collectors.toUnmodifiableList());
    }

    static Stream<TestMethodDetails> tests(DirectoryLayout dirs,
        String module) throws IOException {
        return potentialTestClasses(dirs,
            classFiles(dirs, module), module)
            .flatMap(TestClassDetails::tests);
    }
    static Stream<Path> classFiles(DirectoryLayout dirs,
        String module) throws IOException {
        Path modulePath = dirs.buildGeneration().resolve(module);
        return Files.walk(modulePath)
            .filter(path -> path.getFileName().toString().endsWith(".class"))
            .filter(Files::isRegularFile);
    }

    static boolean potentialTestClass(ClassFile clazz) {
        return clazz.utf8MatchesAny(testClassFilter);
    }

    static Stream<TestClassDetails> potentialTestClasses(DirectoryLayout dirs,
        Stream<Path> stream, String module) {
        return stream.flatMap(IOFunction.of(path -> {
                    byte[] blob = Files.readAllBytes(path);
                    ClassFile clazz = ClassFile.of(ByteBuffer.wrap(blob));
                    if (potentialTestClass(clazz))
                        return Stream.of(
                            new TestClassDetails(dirs, module, clazz));
                    else
                        return Stream.of();
                }));
    }

    FindTests(DirectoryLayout dirs, List<String> modules) {
        AtomicInteger idSource = new AtomicInteger();
        List<TestClassDetails> testClassDetails;
        try (Stream<TestClassDetails> stream = modules.stream()
            .flatMap(IOFunction.of(moduleName ->
                    potentialTestClasses(dirs,
                        classFiles(dirs, moduleName), moduleName)))) {
            testClassDetails = stream.collect(Collectors.toList());
        }

        var result = testClassDetails.stream()
            // Split into classes with and without errors.
            .<Result<TestClassDetails, TestClassDetails>>map(
                classDetails -> {
                    if (classDetails.hasErrors())
                        return Result.ofError(classDetails);
                    else
                        return Result.of(classDetails);
                })
            // Convert the classes to test methods, and the errors to
            // strings.
            .<Result<TestMethodDetails, String>>flatMap(
                r -> r.compute(
                    classDetails -> classDetails.tests().map(Result::of),
                    error -> error.classErrors().map(
                        message -> Result.ofError(
                            String.format("%s: %s",
                                error.className(), message)))))
            // Converted erroneous methods to errors.
            .map(r -> r.flatMap((TestMethodDetails method) -> {
                    if (method.error == null)
                        return Result.of(method);
                    else
                        return Result.ofError(String.format(
                                "%s %s %s: %s",
                                method.classDetails.module,
                                method.classDetails.clazz.className(),
                                method.method.name(),
                                method.error));
                    }))
            // Convert test methods to unscheduled tests.
            .<Result<UnscheduledTest, String>>map(r -> r.map(method ->
                    new UnscheduledTest(new ScheduledTest(
                            idSource.getAndIncrement(),
                            method.locator(),
                            method.resources(),
                            method.exitDuringTest),
                        method.classDetails.sourceFileChange(),
                        method.cost)))
            .collect(Result.Collectors.toCollection(
                    ArrayList::new, ArrayList::new));

        tests = List.copyOf(result.values());
        errors = List.copyOf(result.errors());
        testFiltersActive = testClassDetails.stream()
            .anyMatch(TestClassDetails::hasTestFilters);
    }

    final List<UnscheduledTest> tests;
    final List<String> errors;
    final boolean testFiltersActive;

    private static final StringFilter testClassFilter =
        StringFilter.of(Descriptors.Test);
}
