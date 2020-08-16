package enyo.lang.classfile.tests;

import enyo.io.function.IOFunction;
import enyo.lang.classfile.Annotation;
import enyo.lang.classfile.AnnotationDefaultAttribute;
import enyo.lang.classfile.ClassFile;
import enyo.lang.classfile.ClassFileException;
import enyo.lang.classfile.Constants;
import enyo.lang.classfile.Descriptor;
import enyo.lang.classfile.ElementValue;
import enyo.lang.classfile.EnumConstant;
import enyo.lang.classfile.Field;
import enyo.lang.classfile.Method;
import enyo.lang.classfile.NamedElementValue;
import enyo.util.StringFilter;

import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TestClassFile {
    @interface A {
        String value();
    }

    enum E { X, Y, Z }

    @interface B {
        byte byteValue() default 0;
        char charValue() default '0';
        double doubleValue() default 1;
        float floatValue() default 2;
        int intValue() default 3;
        long longValue() default 4;
        short shortValue() default 5;
        boolean booleanValue() default true;
        String stringValue() default "six";
        E enumValue() default E.X;
        Class<?> classValue() default TestClassFile.class;
        A annotationValue() default @A("seven");
        int[] arrayType() default {8, 9};
    }

    @Test
    @A("argument")
    public void testThisClass(Testing t) throws IOException {
        ClassFile cf = classFile(TestClassFile.class);
        String className = getClass().getName().replace('.', '/');
        t.examine(cf.className()).is(className);
        t.examine(cf.accessFlags()).is(Constants.ACC_PUBLIC
            | Constants.ACC_FINAL
            | Constants.ACC_SUPER);

        t.examine(cf.methods()
            .map(Method::name)
            .filter(name -> name.indexOf('$') < 0)
            .filter(name -> name.indexOf('<') < 0)
            .collect(Collectors.toSet()))
            .is(Set.of("testThisClass", "testEnum",
                    "testInterface", "testAllClasses",
                    "testAnnotationB", "testAnnotationDefault",
                    "testElementValueAs", "latestBuild", "classFile",
                    "findAnnotationB", "elementValueType",
                    "elementValueTypeCheck"));

        String classFileName = Descriptor.descriptor(ClassFile.class);
        String testName = Descriptor.descriptor(Test.class);
        String aName = Descriptor.descriptor(A.class);
        String eName = Descriptor.descriptor(E.class);
        {
            Method method = cf.methods()
                .filter(field -> field.name().equals("testThisClass")
                    && field.descriptor().equals("(Lenyo/testing/Testing;)V"))
                .findFirst()
                .get();
            t.examine(method.accessFlags()).is(Constants.ACC_PUBLIC);
            t.examine(method.annotations()
                .map(Annotation::descriptor)
                .collect(Collectors.toSet()))
                .is(Set.of(testName, aName));
            {
                Annotation test = method.annotations()
                    .filter(ann -> ann.descriptor().equals(testName))
                    .findFirst()
                    .get();
                t.examine(test.toString()).is("Annotation Lenyo/testing/Test;");
                t.examine(test.values().collect(Collectors.toList()))
                    .is(List.of());
            }
            {
                Annotation a = method.annotations()
                    .filter(ann -> ann.descriptor().equals(aName))
                    .findFirst()
                    .get();
                t.examine(a.toString()).is("Annotation " + aName);
                t.examine(a.values()
                    .map(NamedElementValue::toString)
                    .collect(Collectors.toList()))
                    .is(List.of("value=argument"));
                NamedElementValue nev = a.values().findFirst().get();
                t.examine(nev.name()).is("value");
            }
        }

        {
            t.examine(cf.utf8Strings().collect(Collectors.toSet()))
                .check(Checks.contains(className))
                .check(Checks.contains(testName))
                .check(Checks.contains(
                        ClassFile.class.getName().replace('.', '/')));

            t.assertTrue(cf.utf8MatchesAny(StringFilter.of(className)));
            t.assertTrue(cf.utf8MatchesAny(StringFilter.of(testName)));
            t.assertTrue(cf.utf8MatchesAny(StringFilter.of(
                        ClassFile.class.getName().replace('.', '/'))));
        }
    }

    @Test
    public static void testEnum(Testing t) throws IOException {
        ClassFile cf = classFile(E.class);
        t.examine(cf.accessFlags())
            .is(Constants.ACC_FINAL
                | Constants.ACC_SUPER
                | Constants.ACC_ENUM);
    }

    @Test
    public static void testInterface(Testing t) throws IOException {
        ClassFile cf = classFile(IOFunction.class);
        t.examine(cf.accessFlags())
            .is(Constants.ACC_PUBLIC
                | Constants.ACC_ABSTRACT
                | Constants.ACC_INTERFACE);
    }

    @Test
    @B(byteValue=100, charValue='@', doubleValue=101,
        floatValue=102, intValue=103, longValue=104, shortValue=105,
        booleanValue=false, stringValue="six'", enumValue=E.Y,
        classValue=ClassFile.class, annotationValue=@A("seven'"))
    public void testAnnotationB(Testing t) throws IOException {
        testAnnotationB(t, nev -> nev.value().object());
        testAnnotationB(t, NamedElementValue::object);
    }

    protected class C {}

    @Test
    public void testAllClasses(Testing t) throws IOException {
        int classAccessFlags = Constants.ACC_PUBLIC
            | Constants.ACC_FINAL
            | Constants.ACC_SUPER
            | Constants.ACC_INTERFACE
            | Constants.ACC_ABSTRACT
            | Constants.ACC_SYNTHETIC
            | Constants.ACC_ANNOTATION
            | Constants.ACC_ENUM
            | Constants.ACC_MODULE;
        int methodAccessFlags = Constants.ACC_PUBLIC
            | Constants.ACC_PRIVATE
            | Constants.ACC_PROTECTED
            | Constants.ACC_STATIC
            | Constants.ACC_FINAL
            | Constants.ACC_SYNCHRONIZED
            | Constants.ACC_BRIDGE
            | Constants.ACC_VARARGS
            | Constants.ACC_NATIVE
            | Constants.ACC_ABSTRACT
            | Constants.ACC_STRICT
            | Constants.ACC_SYNTHETIC;
        int fieldAccessFlags = Constants.ACC_PUBLIC
            | Constants.ACC_PRIVATE
            | Constants.ACC_PROTECTED
            | Constants.ACC_STATIC
            | Constants.ACC_FINAL
            | Constants.ACC_VOLATILE
            | Constants.ACC_TRANSIENT
            | Constants.ACC_SYNTHETIC
            | Constants.ACC_ENUM;

        Files.walk(latestBuild())
            .filter(path -> path.getFileName().toString().endsWith(".class"))
            .map(IOFunction.of(Files::readAllBytes))
            .map(ByteBuffer::wrap)
            .map(ClassFile::of)
            .forEach(cf -> {
                    String name = cf.className();
                    t.assertTrue(name.length() > 0);
                    t.examine(cf.accessFlags() & ~classAccessFlags).is(0);
                    try {
                        t.assertTrue(cf.utf8Strings().count() > 0);
                        t.assertTrue(cf.attributes().count() > 0);
                        t.assertTrue(cf.methods().count() >= 0);
                        t.assertTrue(cf.fields().count() >= 0);
                        t.assertTrue(cf.annotations().count() >= 0);
                        cf.methods().forEach(
                            method -> {
                                t.examine(method.accessFlags()
                                    & ~methodAccessFlags)
                                    .is(0);
                                t.assertTrue(method.name().length() > 0);
                            });
                        cf.fields().forEach(
                            field -> {
                                t.examine(field.accessFlags()
                                    & ~fieldAccessFlags)
                                    .is(0);
                                t.assertTrue(field.name().length() > 0);
                            });
                        Stream.of(
                            cf.methods().flatMap(Method::annotations),
                            cf.fields().flatMap(Field::annotations),
                            cf.annotations())
                            .flatMap(Function.identity())
                            .forEach(ann -> {
                                    t.examine(ann.descriptor())
                                        .test(desc -> desc.length() > 2)
                                        .check(Checks.startsWith("L"))
                                        .check(Checks.endsWith(";"));
                                    t.examine(ann)
                                        .map(Annotation::values)
                                        .map(Stream::count);
                                });
                    } catch (Throwable e) {
                        throw new AssertionError(name, e);
                    }
                });
    }

    @Test
    public void testAnnotationDefault(Testing t) throws IOException {
        testAnnotationDefault(t, AnnotationDefaultAttribute::object);
        testAnnotationDefault(t, attr -> attr.value().object());
    }

    @Test
    public static void testElementValueAs(Testing t) throws IOException {
        Annotation b = findAnnotationB();
        Map<String, ElementValue> map = b.mapOfValues();
        for (Map.Entry<String, ElementValue> e : map.entrySet()) {
            ElementValue ev = e.getValue();
            switch (e.getKey()) {
            case "byteValue":
                t.examine(ev.asByte()).is((byte) 100);
                t.examine(elementValueType(t, ev)).is("byte");
                break;
            case "charValue":
                t.examine(ev.asChar()).is('@');
                t.examine(elementValueType(t, ev)).is("char");
                break;
            case "doubleValue":
                t.examine(ev.asDouble()).is(101.0);
                t.examine(elementValueType(t, ev)).is("double");
                break;
            case "floatValue":
                t.examine(ev.asFloat()).is(102.0f);
                t.examine(elementValueType(t, ev)).is("float");
                break;
            case "intValue":
                t.examine(ev.asInt()).is(103);
                t.examine(elementValueType(t, ev)).is("int");
                break;
            case "longValue":
                t.examine(ev.asLong()).is(104L);
                t.examine(elementValueType(t, ev)).is("long");
                break;
            case "shortValue":
                t.examine(ev.asShort()).is((short) 105);
                t.examine(elementValueType(t, ev)).is("short");
                break;
            case "booleanValue":
                t.examine(ev.asBoolean()).is(false);
                t.examine(elementValueType(t, ev)).is("boolean");
                break;
            case "stringValue":
                t.examine(ev.asString()).is("six'");
                t.examine(elementValueType(t, ev)).is("String");
                break;
            case "enumValue":
                EnumConstant ec = ev.asEnum();
                t.examine(ec.type()).is(Descriptor.descriptor(E.class));
                t.examine(ec.name()).is("Y");
                t.examine(elementValueType(t, ev)).is("enum");
                break;
            case "classValue":
                t.examine(ev.asClass())
                    .is(Descriptor.descriptor(ClassFile.class));
                t.examine(elementValueType(t, ev)).is("class");
                break;
            case "annotationValue":
                Annotation a = ev.asAnnotation();
                t.examine(a.descriptor()).is(Descriptor.descriptor(A.class));
                t.examine(elementValueType(t, ev)).is("annotation");
                break;
            default:
                t.assertFail("Unknown value: " + e.getKey());
            }
        }
    }

    /**
     * Return the latest-generation build directory.
     */
    private static Path latestBuild() throws IOException {
        Path build = Path.of("build");
        int generation =Files.list(build)
            .flatMap(path -> {
                    String fileName = path.getFileName().toString();
                    if (fileName.startsWith("g")) {
                        try {
                            return Stream.of(
                                Integer.parseInt(fileName.substring(1)));
                        } catch (NumberFormatException e) {
                            // fall through
                        }
                    }
                    return Stream.of();
                })
            .max(Integer::compare)
            .orElseThrow(() -> new IOException(
                    "Could not find latest build directory"));
        return build.resolve("g" + generation);
    }

    private static ClassFile classFile(Class<?> cls) throws IOException {
        String binaryName = cls.getName().replace('.', File.separatorChar);
        // Guess the file name.
        Path path = latestBuild()
            .resolve(cls.getModule().getName())
            .resolve(binaryName + ".class");
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return ClassFile.of(ByteBuffer.wrap(bytes));
    }

    private static Annotation findAnnotationB() throws IOException {
        ClassFile cf = classFile(TestClassFile.class);
        Method method = cf.methods()
            .filter(field -> field.name().equals("testAnnotationB")
                && field.descriptor().equals("(Lenyo/testing/Testing;)V"))
            .findFirst()
            .get();

        String bName = Descriptor.descriptor(B.class);
        return method.annotations()
            .filter(ann -> ann.descriptor().equals(bName))
            .findFirst()
            .get();
    }

    private void testAnnotationB(Testing t,
        Function<NamedElementValue, Object> object) throws IOException {
        Annotation b = findAnnotationB();
        t.examine(b.toString())
            .is("Annotation " + Descriptor.descriptor(B.class));
        t.examine(b.values()
            .sorted(Comparator.comparing(NamedElementValue::name))
            .map(NamedElementValue::toString)
            .collect(Collectors.toList()))
            .is(List.of(
                "annotationValue=@" + Descriptor.descriptor(A.class),
                "booleanValue=false",
                "byteValue=100",
                "charValue=@",
                "classValue=" + Descriptor.descriptor(ClassFile.class),
                "doubleValue=101.0",
                "enumValue=" + Descriptor.descriptor(E.class) + ".Y",
                "floatValue=102.0",
                "intValue=103",
                "longValue=104",
                "shortValue=105",
                "stringValue=six'"));
        Map<String, Object> map = b.values()
            .collect(Collectors.toMap(NamedElementValue::name, object));
        t.examine(((Annotation) map.get("annotationValue")).descriptor())
            .is(Descriptor.descriptor(A.class));
        t.examine(((Annotation) map.get("annotationValue")).values()
            .collect(Collectors.toMap(NamedElementValue::name,
                    nev -> nev.value().object())))
            .is(Map.of("value", "seven'"));
        t.examine(map.get("booleanValue")).is(false);
        t.examine(map.get("byteValue")).is((byte) 100);
        t.examine(map.get("charValue")).is('@');
        t.examine(map.get("classValue")).is(Descriptor.of(ClassFile.class));
        t.examine(map.get("doubleValue")).is(101.0);
        t.examine(((EnumConstant) map.get("enumValue")).type())
            .is(Descriptor.descriptor(E.class));
        t.examine(((EnumConstant) map.get("enumValue")).name()).is("Y");
        t.examine(map.get("floatValue")).is(102.0f);
        t.examine(map.get("intValue")).is(103);
        t.examine(map.get("longValue")).is(104L);
        t.examine(map.get("shortValue")).is((short) 105);
        t.examine(map.get("stringValue")).is("six'");
        t.examine(map.size()).is(12);
    }

    private static void testAnnotationDefault(Testing t,
        Function<AnnotationDefaultAttribute, Object> object)
        throws IOException {
        ClassFile cf = classFile(B.class);
        t.examine(cf.accessFlags())
            .is(Constants.ACC_INTERFACE
                | Constants.ACC_ABSTRACT
                | Constants.ACC_ANNOTATION);
        Map<String, Object> map = cf.methods()
            .collect(Collectors.toMap(Method::name,
                    method -> method.attributes()
                    .filter(attr -> attr.getClass()
                        == AnnotationDefaultAttribute.class)
                    .map(attr -> (AnnotationDefaultAttribute) attr)
                    .map(object)
                    .findFirst()
                    .get()));
        t.examine(((Annotation) map.get("annotationValue")).descriptor())
            .is(Descriptor.descriptor(A.class));
        t.examine(((Annotation) map.get("annotationValue")).values()
            .collect(Collectors.toMap(NamedElementValue::name,
                    nev -> nev.value().object())))
            .is(Map.of("value", "seven"));
        t.examine(map.get("booleanValue")).is(true);
        t.examine(map.get("byteValue")).is((byte) 0);
        t.examine(map.get("charValue")).is('0');
        t.examine(map.get("classValue")).is(Descriptor.of(TestClassFile.class));
        t.examine(map.get("doubleValue")).is(1.0);
        t.examine(((EnumConstant) map.get("enumValue")).type())
            .is(Descriptor.descriptor(E.class));
        t.examine(((EnumConstant) map.get("enumValue")).name()).is("X");
        t.examine(map.get("floatValue")).is(2.0f);
        t.examine(map.get("intValue")).is(3);
        t.examine(map.get("longValue")).is(4L);
        t.examine(map.get("shortValue")).is((short) 5);
        t.examine(map.get("stringValue")).is("six");
        t.examine(map.get("arrayType")).isExactClass(Object[].class);
        t.assertArrayEquals(new Object[] { 8, 9 },
            ((Object[]) map.get("arrayType")));
        t.examine(map.size()).is(13);
    }


    // Determine the type of an ElementValue based on the as*() method
    // which does not throw.
    private static String elementValueType(Testing t, ElementValue ev) {
        ArrayList<String> result = new ArrayList<>(1);
        elementValueTypeCheck(result, ev, ElementValue::asAnnotation,
            "annotation");
        elementValueTypeCheck(result, ev, ElementValue::asBoolean, "boolean");
        elementValueTypeCheck(result, ev, ElementValue::asByte, "byte");
        elementValueTypeCheck(result, ev, ElementValue::asChar, "char");
        elementValueTypeCheck(result, ev, ElementValue::asClass, "class");
        elementValueTypeCheck(result, ev, ElementValue::asDouble, "double");
        elementValueTypeCheck(result, ev, ElementValue::asEnum, "enum");
        elementValueTypeCheck(result, ev, ElementValue::asFloat, "float");
        elementValueTypeCheck(result, ev, ElementValue::asInt, "int");
        elementValueTypeCheck(result, ev, ElementValue::asLong, "long");
        elementValueTypeCheck(result, ev, ElementValue::asShort, "short");
        elementValueTypeCheck(result, ev, ElementValue::asString, "String");
        t.examine(result.size()).is(1);
        return result.get(0);
    }

    interface ElementValueTypeChecker<T> {
        T valueMethod(ElementValue ev);
    }

    private static void elementValueTypeCheck(ArrayList<String> result,
        ElementValue ev, ElementValueTypeChecker<?> checker, String name) {
        try {
            checker.valueMethod(ev);
            result.add(name);
        } catch (ClassFileException e) {
            // expected
        }
    }
}
