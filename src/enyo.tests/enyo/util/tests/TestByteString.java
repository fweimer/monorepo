package enyo.util.tests;

import enyo.util.ByteString;

import enyo.testing.Checks;
import enyo.testing.ExitDuringTest;
import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@ExitDuringTest
public final class TestByteString {
    @Test
    public static void testEmpty(Testing t) {
        t.examine(ByteString.of().length()).is(0);
        t.examine(ByteString.of().isEmpty()).is(true);
        t.examine(ByteString.of().hashCode()).is(Arrays.hashCode(new byte[0]));
        t.examine(ByteString.of().getBytes().length).is(0);
        t.examine(ByteString.of().asByteBuffer())
            .test(ByteBuffer::isReadOnly)
            .mapToInt(ByteBuffer::position, e0 -> e0.is(0))
            .mapToInt(ByteBuffer::limit, e0 -> e0.is(0))
            .mapToInt(ByteBuffer::capacity, e0 -> e0.is(0))
            .map(ByteBuffer::order, e0 -> e0.is(ByteOrder.BIG_ENDIAN))
            .is(ByteBuffer.allocate(0));
        t.examine(ByteString.of(new byte[0])).isSame(ByteString.of());
    }

    @Test
    public static void testOfByte(Testing t) {
        // Used for checking lexicographic ordering.
        var last = ByteString.of();
        for (int i = 0; i < 256; ++i) {
            byte b = (byte) i;
            var s = ByteString.of(b);
            t.examine(s)
                .mapToInt(ByteString::length, e -> e.is(1))
                .map(ByteString::isEmpty, e -> e.is(false))
                .mapToInt(ByteString::hashCode,
                    e -> e.is(Arrays.hashCode(new byte[] { b })))
                .mapToInt(s0 -> s0.byteAt(0), e -> e.is(b))
                .map(ByteString::getBytes,
                    e -> e.check(Checks.byteArrayEquals(b)))
                .map(ByteString::asByteBuffer, e -> e
                    .test(ByteBuffer::isReadOnly)
                    .mapToInt(ByteBuffer::position, e0 -> e0.is(0))
                    .mapToInt(ByteBuffer::limit, e0 -> e0.is(1))
                    .mapToInt(ByteBuffer::capacity, e0 -> e0.is(1))
                    .map(ByteBuffer::order, e0 -> e0.is(ByteOrder.BIG_ENDIAN))
                    .is(ByteBuffer.wrap(new byte[] { b })))
                .isSame(ByteString.of(new byte[] { b }))
                .check(Checks.greaterThan(last));
            t.expect(() -> s.byteAt(-1), IndexOutOfBoundsException.class);
            t.expect(() -> s.byteAt(2), IndexOutOfBoundsException.class);
            last = s;
        }
    }

    @Test
    public static void testTwoBytes(Testing t) {
        // Used for checking lexicographic ordering.
        var last = ByteString.of();
        for (int i0 = 0; i0 < 256; ++i0) {
            byte b0 = (byte) i0;
            {
                var s = ByteString.of(b0);
                t.examine(s).check(Checks.greaterThan(last));
                last = s;
            }
            for (int i1 = 0; i1 < 256; ++i1) {
                byte b1 = (byte) i1;
                var s = ByteString.of(b0, b1);
                t.examine(s)
                    .mapToInt(ByteString::length, e -> e.is(2))
                    .map(ByteString::isEmpty, e -> e.is(false))
                    .mapToInt(ByteString::hashCode,
                        e -> e.is(Arrays.hashCode(new byte[] { b0, b1 })))
                    .mapToInt(s0 -> s0.byteAt(0), e -> e.is(b0))
                    .mapToInt(s0 -> s0.byteAt(1), e -> e.is(b1))
                    .map(ByteString::getBytes,
                        e -> e.check(Checks.byteArrayEquals(b0, b1)))
                    .map(ByteString::asByteBuffer, e -> e
                        .test(ByteBuffer::isReadOnly)
                        .mapToInt(ByteBuffer::position, e0 -> e0.is(0))
                        .mapToInt(ByteBuffer::limit, e0 -> e0.is(2))
                        .mapToInt(ByteBuffer::capacity, e0 -> e0.is(2))
                        .map(ByteBuffer::order,
                            e0 -> e0.is(ByteOrder.BIG_ENDIAN))
                        .is(ByteBuffer.wrap(new byte[] { b0, b1 })))
                    .check(Checks.greaterThan(last));
                t.expect(() -> s.byteAt(-1), IndexOutOfBoundsException.class);
                t.expect(() -> s.byteAt(3), IndexOutOfBoundsException.class);
                last = s;
            }
        }
    }

    @Test
    public static void testFourBytes(Testing t) {
        t.examine(ByteString.of(
                (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd'))
            .mapToInt(ByteString::length, e -> e.is(4))
            .map(ByteString::getBytes,
                e -> e.check(Checks.byteArrayEquals(
                        new byte[] { 'a', 'b', 'c', 'd' })));
    }

    @Test
    public static void testBytes(Testing t) {
        t.examine(ByteString.of().bytes().count()).is(0);
        t.examine(ByteString.of("abcd\0\377", StandardCharsets.ISO_8859_1)
            .bytes().boxed().toList())
            .is(List.of((int) 'a', (int) 'b', (int) 'c', (int) 'd', 0, 255));
    }

    @Test
    public static void testOfByteBufferShifted(Testing t) {
        testByteBufferShifted(t, s -> ByteBuffer.wrap(s.getBytes()));
        testByteBufferShifted(t, s -> {
                byte[] data = s.getBytes();
                ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
                buffer.put(data);
                buffer.flip();
                return buffer;
            });
    }

    @Test
    public static void testOfString(Testing t) {
        t.examine(ByteString.of("abcd", StandardCharsets.ISO_8859_1))
            .mapToInt(ByteString::length, e -> e.is(4))
            .map(ByteString::getBytes,
                e -> e.check(Checks.byteArrayEquals(
                        new byte[] { 'a', 'b', 'c', 'd' })));
        t.examine(ByteString.of("AB", StandardCharsets.UTF_16BE))
            .map(ByteString::getBytes)
            .check(Checks.byteArrayEquals(new byte[] { 0, 65, 0, 66 }));
        t.examine(ByteString.of("AB", StandardCharsets.UTF_16LE))
            .map(ByteString::getBytes)
            .check(Checks.byteArrayEquals(new byte[] { 65, 0, 66, 0 }));
        t.expect(() -> ByteString.of("\u0100", StandardCharsets.ISO_8859_1),
            IllegalArgumentException.class,
            e -> {
                t.expect(() -> { throw e.getCause(); },
                    CharacterCodingException.class);
            });
    }

    @Test
    public static void testOfUTF8(Testing t) {
        t.examine(ByteString.ofUTF8("")).isSame(ByteString.of());
        t.examine(ByteString.ofUTF8("A")).isSame(ByteString.of((byte) 'A'));
        t.examine(ByteString.ofUTF8("\u0080"))
            .is(ByteString.of((byte) 0xc2, (byte) 0x80));
        t.examine(ByteString.ofUTF8("\u0080\u0081"))
            .is(ByteString.of(
                    (byte) 0xc2, (byte) 0x80, (byte) 0xc2, (byte) 0x81));
        t.examine(ByteString.ofUTF8("\u00E4"))
            .is(ByteString.of((byte) 0xc3, (byte) 0xa4));
        t.expect(() -> ByteString.ofUTF8("\uD800"),
            IllegalArgumentException.class,
            e -> {
                t.expect(() -> { throw e.getCause(); },
                    CharacterCodingException.class);
            });
    }

    @Test
    public static void testSubstring1(Testing t) {
        t.examine(ByteString.of().substring(0)).isSame(ByteString.of());
        var abc = ByteString.ofUTF8("abc");
        t.examine(abc.substring(0)).isSame(abc);
        t.examine(abc.substring(1)).is(ByteString.ofUTF8("bc"));
        t.examine(abc.substring(2)).isSame(ByteString.ofUTF8("c"));
        t.examine(abc.substring(3)).isSame(ByteString.of());
    }

    @Test
    public static void testSubstring2(Testing t) {
        t.examine(ByteString.of().substring(0, 0)).isSame(ByteString.of());
        t.expect(() -> ByteString.of().substring(1, 1),
            IndexOutOfBoundsException.class);
        var abc = ByteString.ofUTF8("abc");
        t.examine(abc.substring(0, 3)).isSame(abc);
        t.examine(abc.substring(1, 2)).isSame(ByteString.of((byte) 'b'));
        t.examine(abc.substring(1, 3)).is(ByteString.ofUTF8("bc"));
        t.examine(abc.substring(2, 3)).isSame(ByteString.of((byte) 'c'));
        t.examine(abc.substring(0, 0)).isSame(ByteString.of());
        t.examine(abc.substring(1, 1)).isSame(ByteString.of());
        t.examine(abc.substring(2, 2)).isSame(ByteString.of());
        t.examine(abc.substring(3, 3)).isSame(ByteString.of());
        t.expect(() -> ByteString.of().substring(4, 4),
            IndexOutOfBoundsException.class);
        t.expect(() -> ByteString.of().substring(3, 4),
            IndexOutOfBoundsException.class);
        t.expect(() -> ByteString.of().substring(1,0),
            IndexOutOfBoundsException.class);
    }

    @Test
    public static void testOrder() {
        var order = new Order<ByteString>();
        order.group()
            .add(ByteString.of())
            .add(ByteString.of(new byte[0]))
            .add(ByteString.of(new byte[0]))
            .add(ByteString.of((byte) 2).substring(1))
            .add(ByteString.of(ByteBuffer.allocate(0)))
            .close();
        order.group()
            .add(ByteString.of(new byte[] { 0 }))
            .add(ByteString.of(ByteBuffer.allocate(1)))
            .close();
        order.group()
            .add(ByteString.of(new byte[] { 0, 0 }))
            .add(ByteString.of((byte) 0, (byte) 0))
            .add(ByteString.of(ByteBuffer.allocate(2)))
            .close();
        order.group()
            .add(ByteString.of(new byte[] { 0, 1 }))
            .add(ByteString.of((byte) 0, (byte) 1))
            .close();
        order.group()
            .add(ByteString.of(new byte[] { 1 }))
            .add(ByteString.of((byte) 1))
            .close();
        order.group()
            .add(ByteString.of(new byte[] { 'A' }))
            .add(ByteString.of((byte) 'A'))
            .close();
        order.add(ByteString.ofUTF8("AB"));
        order.add(ByteString.ofUTF8("AB\0"));
        order.add(ByteString.ofUTF8("Ab"));
        order.add(ByteString.ofUTF8("Ab\0"));
        order.add(ByteString.ofUTF8("aB"));
        order.add(ByteString.ofUTF8("aB\0"));
        order.add(ByteString.ofUTF8("ab"));
        order.group()
            .add(ByteString.of(new byte[] { 127 }))
            .add(ByteString.of((byte) 127))
            .add(ByteString.of("\177", StandardCharsets.ISO_8859_1))
            .close();
        order.add(ByteString.of(new byte[] { 127, 0 }));
        order.group()
            .add(ByteString.of(new byte[] { -128 }))
            .add(ByteString.of((byte) -128))
            .close();
        order.add(ByteString.of(new byte[] { -127 }));
        order.add(ByteString.of(new byte[] { -126 }));
        order.group()
            .add(ByteString.of(new byte[] { -1 }))
            .add(ByteString.of((byte) -1))
            .close();
        order.checkComparable(ByteString.class);
    }

    @Test
    public static void testSerialize(Testing t)
        throws IOException, ClassNotFoundException {
        var s1 = ByteString.ofUTF8("abc");
        var s2 = ByteString.ofUTF8("abc");
        var list = List.of(s1, s1, s2, s2,
            ByteString.of(), ByteString.of((byte) 'a'));
        var out = new ByteArrayOutputStream();
        try (var stream = new ObjectOutputStream(out)) {
            stream.writeObject(list);
        }
        List<?> result;
        try (var stream = new ObjectInputStream(new ByteArrayInputStream(
                    out.toByteArray()))) {
            result = (List<?>) stream.readObject();
        }
        t.examine(result.size()).is(6);
        t.examine(result.get(0))
            .is(s1)
            .isNotSame(s1)
            .isNotSame(s2)
            .isSame(result.get(1));
        t.examine(result.get(1))
            .is(s1)
            .isNotSame(s1)
            .isNotSame(s2);
        t.examine(result.get(2))
            .is(s2)
            .isNotSame(s1)
            .isNotSame(s2)
            .isSame(result.get(2));
        t.examine(result.get(3))
            .is(s2)
            .isNotSame(s1)
            .isNotSame(s2);
        t.examine(result.get(4)).isSame(ByteString.of());
        t.examine(result.get(5)).isSame(ByteString.of((byte) 'a'));
    }

    private static void testByteBufferShifted(Testing t,
        Function<String, ByteBuffer> wrap) {
        for (int prefixLength = 0; prefixLength < 32; ++prefixLength)
            for (int suffixLength = 0; suffixLength < 32; ++suffixLength) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < prefixLength; ++i)
                sb.append('X');
            sb.append("abc");
            for (int i = 0; i < suffixLength; ++i)
                sb.append('Y');

            ByteBuffer buffer = wrap.apply(sb.toString());
            buffer.position(prefixLength);
            ByteString b = ByteString.of(buffer);
            t.examine(b.toString()).is(sb.toString().substring(prefixLength));

            buffer = ByteBuffer.wrap(sb.toString().getBytes());
            buffer.position(prefixLength);
            buffer.limit(prefixLength + 3);
            b = ByteString.of(buffer);
            t.examine(b.toString()).is("abc");
        }
    }
}
