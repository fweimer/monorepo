package enyo.build;

import enyo.lang.classfile.ClassFile;

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

    TestClassDetails(String module, ClassFile clazz) {
        this.module = Objects.requireNonNull(module, "module");
        this.clazz = Objects.requireNonNull(clazz, "clazz");
    }

    Stream<TestMethodDetails> tests() {
        return clazz.methods()
            .map(method -> new TestMethodDetails(this, method))
            .filter(method -> method.hasTestAnnotation);
    }
}
