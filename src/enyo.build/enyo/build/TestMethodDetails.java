package enyo.build;

import enyo.lang.classfile.Constants;
import enyo.lang.classfile.Method;
import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.introspect.TestResources;

import java.util.Objects;
import java.util.Optional;

final class TestMethodDetails {
    final TestClassDetails classDetails;
    final Method method;
    boolean hasTestAnnotation;
    boolean hasTestingArgument;
    TestResources resources;
    double cost;
    boolean exitDuringTest;
    String error;

    TestMethodDetails(TestClassDetails classDetails, Method method) {
        this.classDetails = Objects.requireNonNull(
            classDetails, "classDetails");
        this.method = Objects.requireNonNull(method, "method");
        resources = classDetails.resources();
        cost = classDetails.cost();

        method.annotations()
            .forEach(ann -> {
                    if (ann.descriptor().equals(Descriptors.Test))
                        hasTestAnnotation = true;
                    else if (ann.descriptor().equals(Descriptors.Resources))
                        try {
                            resources = ResourcesParser.parse(ann);
                        } catch (AnnotationParseException e) {
                            error = e.getMessage();
                        }
                    else if (ann.descriptor().equals(Descriptors.Cost))
                        try {
                            cost = CostParser.parse(ann);
                        } catch (AnnotationParseException e) {
                            error = e.getMessage();
                        }
                    else if (ann.descriptor().equals(
                            Descriptors.ExitDuringTest))
                        try {
                            exitDuringTest = ExitDuringTestParser.parse(ann);
                        } catch (AnnotationParseException e) {
                            error = e.getMessage();
                        }
                });

        String descriptor = method.descriptor();
        if (descriptor.equals(withTestingDescriptor))
            hasTestingArgument = true;
        else if (!descriptor.equals("()V"))
            error = "Invalid descriptor: " + descriptor;
        if ((method.accessFlags() & Constants.ACC_PUBLIC) == 0
            && error == null)
            error = "Method is not public";
    }

    TestMethodLocator locator() {
        if (!hasTestAnnotation || error != null)
            throw new IllegalStateException();
        return new TestMethodLocator(
            Optional.of(classDetails.module),
            classDetails.clazz.className().replace('/', '.'),
            method.name(),
            hasTestingArgument);
    }

    TestResources resources() {
        return resources;
    }

    private static final String withTestingDescriptor =
        '(' + Descriptors.Testing + ")V";
}
