package enyo.util.tests;

import enyo.util.StringParsers;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.stream.Stream;

public final class TestStringParsers {
    @Test
    public static void testString(Testing t) throws Throwable {
        t.examine(
            (String) StringParsers.parser(String.class).invokeExact(""))
            .is("");
        t.examine(
            (String) StringParsers.parser(String.class).invokeExact("1"))
            .is("1");
        t.expect(() -> {
                String ignored = (String)
                    StringParsers.parser(String.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
    }

    @Test
    public static void testByte(Testing t) throws Throwable {
        t.examine((byte) StringParsers.parser(byte.class).invokeExact("1"))
            .is(1);
        t.examine((Byte) StringParsers.parser(Byte.class).invokeExact("2"))
            .is((byte) 2);
        t.expect(() -> {
                byte ignored = (byte)
                    StringParsers.parser(byte.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                Byte ignored = (Byte)
                    StringParsers.parser(Byte.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
    }

    @Test
    public static void testShort(Testing t) throws Throwable {
        t.examine((short) StringParsers.parser(short.class).invokeExact("1"))
            .is(1);
        t.examine((Short) StringParsers.parser(Short.class).invokeExact("2"))
            .is((short) 2);
        t.expect(() -> {
                short ignored = (short)
                    StringParsers.parser(short.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                Short ignored = (Short)
                    StringParsers.parser(Short.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
    }

    @Test
    public static void testInt(Testing t) throws Throwable {
        t.examine((int) StringParsers.parser(int.class).invokeExact("1"))
            .is(1);
        t.examine(
            (Integer) StringParsers.parser(Integer.class).invokeExact("2"))
            .is(2);
        t.expect(() -> {
                int ignored = (int)
                    StringParsers.parser(int.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                Integer ignored = (Integer)
                    StringParsers.parser(Integer.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
    }

    @Test
    public static void testLong(Testing t) throws Throwable {
        t.examine((long) StringParsers.parser(long.class).invokeExact("1"))
            .is(1);
        t.examine((Long) StringParsers.parser(Long.class).invokeExact("2"))
            .is(2L);
        t.expect(() -> {
                long ignored = (long)
                    StringParsers.parser(long.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                Long ignored = (Long)
                    StringParsers.parser(Long.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
    }

    @Test
    public static void testFloat(Testing t) throws Throwable {
        t.examine((float) StringParsers.parser(float.class).invokeExact("1"))
            .is(1.0f);
        t.examine((Float) StringParsers.parser(Float.class).invokeExact("2"))
            .is(2.0f);
        t.expect(() -> {
                float ignored = (float)
                    StringParsers.parser(float.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                Float ignored = (Float)
                    StringParsers.parser(Float.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
    }

    @Test
    public static void testDouble(Testing t) throws Throwable {
        t.examine((double) StringParsers.parser(double.class).invokeExact("1"))
            .is(1.0);
        t.examine((Double) StringParsers.parser(Double.class).invokeExact("2"))
            .is(2.0);
        t.expect(() -> {
                double ignored = (double)
                    StringParsers.parser(double.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                Double ignored = (Double)
                    StringParsers.parser(Double.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
    }

    @Test
    public static void testBoolean(Testing t) throws Throwable {
        t.examine(
            (boolean) StringParsers.parser(boolean.class).invokeExact("true"))
            .is(true);
        t.examine(
            (Boolean) StringParsers.parser(Boolean.class).invokeExact("false"))
            .is(false);
        t.expect(() -> {
                boolean ignored = (boolean)
                    StringParsers.parser(boolean.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                Boolean ignored = (Boolean)
                    StringParsers.parser(Boolean.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        Stream.of("", "0", "1", "t", "f", "TRUE", "FALSE", "True", "False",
            "yes", "no", "y", "n", "-", "+", " true")
            .forEach(invalid -> {
                    t.expect(() -> {
                            boolean ignored = (boolean) StringParsers.parser(
                                boolean.class).invokeExact(invalid);
                        }, IllegalArgumentException.class);
                    t.expect(() -> {
                            boolean ignored = (Boolean) StringParsers.parser(
                                Boolean.class).invokeExact(invalid);
                        }, IllegalArgumentException.class);
                });
    }

    @Test
    public static void testChar(Testing t) throws Throwable {
        t.examine((char) StringParsers.parser(char.class).invokeExact("1"))
            .is('1');
        t.examine(
            (Character) StringParsers.parser(Character.class).invokeExact("2"))
            .is('2');
        t.expect(() -> {
                char ignored = (char)
                    StringParsers.parser(char.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                Character ignored = (Character)
                    StringParsers.parser(Character.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        Stream.of("", "11", "123",
            String.valueOf(Character.toChars(65536)))
            .forEach(invalid -> {
                    t.expect(() -> {
                            char ignored = (char) StringParsers.parser(
                                char.class).invokeExact(invalid);
                        }, IllegalArgumentException.class);
                    t.expect(() -> {
                            char ignored = (Character) StringParsers.parser(
                                Character.class).invokeExact(invalid);
                        }, IllegalArgumentException.class);
                });
    }

    @Test
    public static void testBigInteger(Testing t) throws Throwable {
        t.examine((BigInteger) StringParsers.parser(
                BigInteger.class).invokeExact("1"))
            .is(BigInteger.valueOf(1));
        t.expect(() -> {
                BigInteger ignored = (BigInteger)
                    StringParsers.parser(BigInteger.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
    }

    @Test
    public static void testBigDecimal(Testing t) throws Throwable {
        t.examine((BigDecimal) StringParsers.parser(
                BigDecimal.class).invokeExact("1"))
            .is(BigDecimal.valueOf(1));
        t.expect(() -> {
                BigDecimal ignored = (BigDecimal)
                    StringParsers.parser(BigDecimal.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
    }

    public enum E1 {
        X, Y
    }

    @Test
    public static void testEnumPublic(Testing t) throws Throwable {
        t.examine((E1) StringParsers.parser(
                E1.class).invokeExact("X"))
            .isSame(E1.X);
        t.expect(() -> {
                E1 ignored = (E1)
                    StringParsers.parser(E1.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                E1 ignored = (E1)
                    StringParsers.parser(E1.class).invokeExact("x");
            }, IllegalArgumentException.class);
    }

    private enum E2 {
        X, Y
    }

    @Test
    public static void testEnumPrivate(Testing t) throws Throwable {
        t.examine((E2) StringParsers.parser(
                E2.class).invokeExact("X"))
            .isSame(E2.X);
        t.expect(() -> {
                E2 ignored = (E2)
                    StringParsers.parser(E2.class).invokeExact(
                        (String) null);
            }, NullPointerException.class);
        t.expect(() -> {
                E2 ignored = (E2)
                    StringParsers.parser(E2.class).invokeExact("x");
            }, IllegalArgumentException.class);
    }
}
