package enyo.lang.classfile.tests;

import enyo.io.function.IOFunction;
import enyo.lang.classfile.Annotation;
import enyo.lang.classfile.AnnotationDefaultAttribute;
import enyo.lang.classfile.ClassFile;
import enyo.lang.classfile.Constants;
import enyo.lang.classfile.Descriptor;
import enyo.lang.classfile.EnumConstant;
import enyo.lang.classfile.Field;
import enyo.lang.classfile.Method;
import enyo.lang.classfile.NamedElementValue;
import enyo.util.StringFilter;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
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
        t.assertEquals(className, cf.className());
        t.assertEquals(Constants.ACC_PUBLIC
            | Constants.ACC_FINAL
            | Constants.ACC_SUPER,
            cf.accessFlags());

        {
            Set<String> methods = cf.methods()
                .map(Method::name)
                .filter(name -> name.indexOf('$') < 0)
                .filter(name -> name.indexOf('<') < 0)
                .collect(Collectors.toSet());
            t.assertEquals(Set.of("testThisClass", "testEnum",
                    "testInterface", "testAllClasses",
                    "testAnnotationB", "testAnnotationDefault",
                    "latestBuild", "classFile"), methods);
        }

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
            t.assertEquals(Constants.ACC_PUBLIC, method.accessFlags());
            t.assertEquals(Set.of(testName, aName),
                method.annotations()
                .map(Annotation::descriptor)
                .collect(Collectors.toSet()));
            {
                Annotation test = method.annotations()
                    .filter(ann -> ann.descriptor().equals(testName))
                    .findFirst()
                    .get();
                t.assertEquals("Annotation Lenyo/testing/Test;",
                    test.toString());
                t.assertEquals(List.of(),
                    test.values().collect(Collectors.toList()));
            }
            {
                Annotation a = method.annotations()
                    .filter(ann -> ann.descriptor().equals(aName))
                    .findFirst()
                    .get();
                t.assertEquals("Annotation " + aName, a.toString());
                t.assertEquals(List.of("value=argument"),
                    a.values()
                    .map(NamedElementValue::toString)
                    .collect(Collectors.toList()));
                NamedElementValue nev = a.values().findFirst().get();
                t.assertEquals("value", nev.name());
            }
        }

        {
            Set<String> strings = cf.utf8Strings().collect(Collectors.toSet());
            t.assertTrue(strings.contains(className));
            t.assertTrue(strings.contains(testName));

            t.assertTrue(strings.contains(
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
        t.assertEquals(Constants.ACC_FINAL
            | Constants.ACC_SUPER
            | Constants.ACC_ENUM,
            cf.accessFlags());
    }

    @Test
    public static void testInterface(Testing t) throws IOException {
        ClassFile cf = classFile(IOFunction.class);
        t.assertEquals(Constants.ACC_PUBLIC
            | Constants.ACC_ABSTRACT
            | Constants.ACC_INTERFACE,
            cf.accessFlags());
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
                    t.assertEquals(0, cf.accessFlags() & ~classAccessFlags);
                    try {
                        t.assertTrue(cf.utf8Strings().count() > 0);
                        t.assertTrue(cf.attributes().count() > 0);
                        t.assertTrue(cf.methods().count() >= 0);
                        t.assertTrue(cf.fields().count() >= 0);
                        t.assertTrue(cf.annotations().count() >= 0);
                        cf.methods().forEach(
                            method -> {
                                t.assertEquals(0,
                                    method.accessFlags() & ~methodAccessFlags);
                                t.assertTrue(method.name().length() > 0);
                            });
                        cf.fields().forEach(
                            field -> {
                                t.assertEquals(0,
                                    field.accessFlags() & ~fieldAccessFlags);
                                t.assertTrue(field.name().length() > 0);
                            });
                        Stream.of(
                            cf.methods().flatMap(Method::annotations),
                            cf.fields().flatMap(Field::annotations),
                            cf.annotations())
                            .flatMap(Function.identity())
                            .forEach(ann -> {
                                    t.assertTrue(ann.descriptor(),
                                        ann.descriptor().length() > 2);
                                    t.assertTrue(ann.descriptor(),
                                        ann.descriptor().startsWith("L"));
                                    t.assertTrue(ann.descriptor(),
                                        ann.descriptor().endsWith(";"));
                                    t.assertTrue(ann.descriptor(),
                                        ann.values().count() >= 0);
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

    private void testAnnotationB(Testing t,
        Function<NamedElementValue, Object> object) throws IOException {
        ClassFile cf = classFile(TestClassFile.class);
        Method method = cf.methods()
            .filter(field -> field.name().equals("testAnnotationB")
                && field.descriptor().equals("(Lenyo/testing/Testing;)V"))
            .findFirst()
            .get();

        String bName = Descriptor.descriptor(B.class);
        Annotation b = method.annotations()
            .filter(ann -> ann.descriptor().equals(bName))
            .findFirst()
            .get();
        t.assertEquals("Annotation " + bName, b.toString());
        t.assertEquals(List.of(
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
                "stringValue=six'"),
            b.values()
            .sorted(Comparator.comparing(NamedElementValue::name))
            .map(NamedElementValue::toString)
            .collect(Collectors.toList()));
        Map<String, Object> map = b.values()
            .collect(Collectors.toMap(NamedElementValue::name, object));
        t.assertEquals(Descriptor.descriptor(A.class),
            ((Annotation) map.get("annotationValue")).descriptor());
        t.assertEquals(Map.of("value", "seven'"),
            ((Annotation) map.get("annotationValue")).values()
            .collect(Collectors.toMap(NamedElementValue::name,
                    nev -> nev.value().object())));
        t.assertEquals(false, map.get("booleanValue"));
        t.assertEquals((byte) 100, map.get("byteValue"));
        t.assertEquals('@', map.get("charValue"));
        t.assertEquals(Descriptor.of(ClassFile.class), map.get("classValue"));
        t.assertEquals(101.0, map.get("doubleValue"));
        t.assertEquals(Descriptor.descriptor(E.class),
            ((EnumConstant) map.get("enumValue")).type());
        t.assertEquals("Y", ((EnumConstant) map.get("enumValue")).name());
        t.assertEquals(102.0f, map.get("floatValue"));
        t.assertEquals(103, map.get("intValue"));
        t.assertEquals(104L, map.get("longValue"));
        t.assertEquals((short) 105, map.get("shortValue"));
        t.assertEquals("six'", map.get("stringValue"));
        t.assertEquals(12, map.size());
    }

    private static void testAnnotationDefault(Testing t,
        Function<AnnotationDefaultAttribute, Object> object)
        throws IOException {
        ClassFile cf = classFile(B.class);
        t.assertEquals(Constants.ACC_INTERFACE
            | Constants.ACC_ABSTRACT
            | Constants.ACC_ANNOTATION,
            cf.accessFlags());
        Map<String, Object> map = cf.methods()
            .collect(Collectors.toMap(Method::name,
                    method -> method.attributes()
                    .filter(attr -> attr.getClass()
                        == AnnotationDefaultAttribute.class)
                    .map(attr -> (AnnotationDefaultAttribute) attr)
                    .map(object)
                    .findFirst()
                    .get()));
        t.assertEquals(Descriptor.descriptor(A.class),
            ((Annotation) map.get("annotationValue")).descriptor());
        t.assertEquals(Map.of("value", "seven"),
            ((Annotation) map.get("annotationValue")).values()
            .collect(Collectors.toMap(NamedElementValue::name,
                    nev -> nev.value().object())));
        t.assertEquals(true, map.get("booleanValue"));
        t.assertEquals((byte) 0, map.get("byteValue"));
        t.assertEquals('0', map.get("charValue"));
        t.assertEquals(Descriptor.of(TestClassFile.class),
            map.get("classValue"));
        t.assertEquals(1.0, map.get("doubleValue"));
        t.assertEquals(Descriptor.descriptor(E.class),
            ((EnumConstant) map.get("enumValue")).type());
        t.assertEquals("X",
            ((EnumConstant) map.get("enumValue")).name());
        t.assertEquals(2.0f, map.get("floatValue"));
        t.assertEquals(3, map.get("intValue"));
        t.assertEquals(4L, map.get("longValue"));
        t.assertEquals((short) 5, map.get("shortValue"));
        t.assertEquals("six", map.get("stringValue"));
        t.assertEquals(Object[].class, map.get("arrayType").getClass());
        t.assertArrayEquals(new Object[] { 8, 9 },
            ((Object[]) map.get("arrayType")));
        t.assertEquals(13, map.size());
    }
}
