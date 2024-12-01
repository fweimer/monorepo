package enyo.build;

import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.introspect.TestResources;

import java.lang.classfile.ClassFile;
import java.lang.classfile.MethodModel;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.Objects;
import java.util.Optional;
import java.lang.classfile.Attributes;

final class TestMethodDetails {
    final TestClassDetails classDetails;
    final MethodModel method;
    boolean hasTestAnnotation;
    boolean hasTestingArgument;
    TestResources resources;
    double cost;
    boolean exitDuringTest;
    String error;

    TestMethodDetails(TestClassDetails classDetails, MethodModel method) {
        this.classDetails = Objects.requireNonNull(
            classDetails, "classDetails");
        this.method = Objects.requireNonNull(method, "method");
        resources = classDetails.resources();
        cost = classDetails.cost();
        exitDuringTest = classDetails.exitDuringTest();

        method.findAttribute(Attributes.runtimeInvisibleAnnotations())
            .stream()
            .flatMap(attr -> attr.annotations().stream())
            .forEach(ann -> {
                    if (ann.classSymbol().equals(Descriptors.Test))
                        hasTestAnnotation = true;
                    else if (ann.classSymbol().equals(Descriptors.Resources))
                        try {
                            resources = ResourcesParser.parse(ann);
                        } catch (AnnotationParseException e) {
                            error = e.getMessage();
                        }
                    else if (ann.classSymbol().equals(Descriptors.Cost))
                        try {
                            cost = CostParser.parse(ann);
                        } catch (AnnotationParseException e) {
                            error = e.getMessage();
                        }
                    else if (ann.classSymbol().equals(
                            Descriptors.ExitDuringTest))
                        try {
                            exitDuringTest = ExitDuringTestParser.parse(ann);
                        } catch (AnnotationParseException e) {
                            error = e.getMessage();
                        }
                });

        MethodTypeDesc descriptor = method.methodTypeSymbol();
        if (descriptor.equals(withTestingDescriptor))
            hasTestingArgument = true;
        else if (!descriptor.equals(noArgumentDescriptor))
            error = "Invalid descriptor: " + descriptor;
        if (!method.flags().has(AccessFlag.PUBLIC) && error == null)
            error = "Method is not public";
    }

    TestMethodLocator locator() {
        if (!hasTestAnnotation || error != null)
            throw new IllegalStateException();
        return new TestMethodLocator(
            Optional.of(classDetails.module),
            classDetails.className(),
            method.methodName().stringValue(),
            hasTestingArgument);
    }

    TestResources resources() {
        return resources;
    }

    private static final MethodTypeDesc noArgumentDescriptor =
        MethodTypeDesc.of(ConstantDescs.CD_void);
    private static final MethodTypeDesc withTestingDescriptor =
        MethodTypeDesc.of(ConstantDescs.CD_void, Descriptors.Testing);
}
