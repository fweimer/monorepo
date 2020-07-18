package enyo.build;

import enyo.lang.classfile.Method;

import java.util.Objects;

final class TestMethodDetails {
    final TestClassDetails classDetails;
    final Method method;
    boolean hasTestAnnotation;
    boolean hasTestingArgument;
    String error;

    TestMethodDetails(TestClassDetails classDetails, Method method) {
        this.classDetails = Objects.requireNonNull(
            classDetails, "classDetails");
        this.method = Objects.requireNonNull(method, "method");

        method.annotations()
            .forEach(ann -> {
                    if (ann.descriptor().equals(Descriptors.Test))
                        hasTestAnnotation = true;
                });

        String descriptor = method.descriptor();
        if (descriptor.equals(withTestingDescriptor))
            hasTestingArgument = true;
        else if (!descriptor.equals("()V"))
            error = "Invalid descriptor: " + descriptor;
    }

    TestMethodLocator locator() {
        if (!hasTestAnnotation || error != null)
            throw new IllegalStateException();
        return new TestMethodLocator(
            classDetails.module,
            classDetails.clazz.className().replace('/', '.'),
            method.name(),
            hasTestingArgument);
    }

    private static final String withTestingDescriptor =
        '(' + Descriptors.Testing + ")V";
}
