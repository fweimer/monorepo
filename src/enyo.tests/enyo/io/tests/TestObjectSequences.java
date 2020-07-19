package enyo.io.tests;

import enyo.io.ObjectSequences;
import enyo.io.function.IOConsumer;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class TestObjectSequences {
    @Test
    public static void testWriteInSequence(Testing t) throws IOException {
        checkWrite(t, "\1\2\3\4\5",
            out -> ObjectSequences.writeInSequence(
                out, 0x02030405, DataOutputStream::writeInt));
    }

    @Test
    public static void testWriteSequenceEnd(Testing t) throws IOException {
        checkWrite(t, "\0", out -> ObjectSequences.writeSequenceEnd(out));
    }

    @Test
    public static void testWriteSequenceEmpty(Testing t) throws IOException {
        checkWrite(t, "\0", out -> ObjectSequences.writeSequence(out, List.of(),
                (out1, obj) -> t.assertFail(
                    "attempt to write non-existing object")));
    }

    @Test
    public static void testWriteSequence1(Testing t) throws IOException {
        checkWrite(t, "\1\0\0\0\1\0",
            out -> ObjectSequences.writeSequence(out, List.of(1),
                DataOutputStream::writeInt));
    }

    @Test
    public static void testWriteSequence2(Testing t) throws IOException {
        checkWrite(t, "\1\0\0\0\2\1\0\0\0\3\0",
            out -> ObjectSequences.writeSequence(out, List.of(2, 3),
                DataOutputStream::writeInt));
    }

    @Test
    public static void testWriteSequenceStreamEmpty(Testing t)
        throws IOException {
        checkWrite(t, "\0", out -> ObjectSequences.writeSequence(out, Stream.of(),
                (out1, obj) -> t.assertFail(
                    "attempt to write non-existing object")));
    }

    @Test
    public static void testWriteSequenceStream1(Testing t) throws IOException {
        checkWrite(t, "\1\0\0\0\1\0",
            out -> ObjectSequences.writeSequence(out, Stream.of(1),
                DataOutputStream::writeInt));
    }

    @Test
    public static void testWriteSequenceStream2(Testing t) throws IOException {
        checkWrite(t, "\1\0\0\0\2\1\0\0\0\3\0",
            out -> ObjectSequences.writeSequence(out, List.of(2, 3).stream(),
                DataOutputStream::writeInt));
    }

    @Test
    public static void testWriteAsSequenceEmpty(Testing t) throws IOException {
        checkWrite(t, "\0",
            out -> ObjectSequences.writeAsSequence(out, Optional.empty(),
                DataOutputStream::writeInt));
    }

    @Test
    public static void testWriteAsSequenceValue(Testing t) throws IOException {
        checkWrite(t, "\1\0\0\0\1\0",
            out -> ObjectSequences.writeAsSequence(out, Optional.of(1),
                DataOutputStream::writeInt));
    }

    @Test
    public static void testWriteAsOptionalEmpty(Testing t) throws IOException {
        checkWrite(t, "\0",
            out -> ObjectSequences.writeAsOptional(out, Optional.empty(),
                DataOutputStream::writeInt));
    }

    @Test
    public static void testWriteAsOptionalValue(Testing t) throws IOException {
        checkWrite(t, "\1\0\0\0\1",
            out -> ObjectSequences.writeAsOptional(out, Optional.of(1),
                DataOutputStream::writeInt));
    }

    @Test
    public static void testReadObject(Testing t) throws IOException {
        checkRead(t, "\1\0\0\0\2", in ->
            t.assertEquals(Optional.of(2),
                ObjectSequences.readObject(in, in1 -> in1.readInt())));
    }

    @Test
    public static void testReadObjectEmpty(Testing t) throws IOException {
        checkRead(t, "\0", in ->
            t.assertEquals(Optional.empty(),
                ObjectSequences.readObject(
                    in, in1 -> t.assertFail("non-existing object read"))));
    }

    @Test
    public static void testReadObjectEOF(Testing t) throws IOException {
        checkRead(t, "", in ->
            t.expect(() ->
                ObjectSequences.readObject(
                    in, in1 -> t.assertFail("object read despite EOF")),
                EOFException.class));
    }

    @Test
    public static void testReadListEmpty(Testing t) throws IOException {
        checkRead(t, "\0", in ->
            t.assertEquals(List.of(),
                ObjectSequences.readList(
                    in, in1 -> t.assertFail("object read despite EOF"))));

    }

    @Test
    public static void testReadList1(Testing t) throws IOException {
        checkRead(t, "\1\0\0\0\1\0", in ->
            t.assertEquals(List.of(1),
                ObjectSequences.readList(in, in1 -> in.readInt())));
    }

    @Test
    public static void testReadList2(Testing t) throws IOException {
        checkRead(t, "\1\0\0\0\1\1\0\0\0\2\0", in ->
            t.assertEquals(List.of(1, 2),
                ObjectSequences.readList(in, in1 -> in.readInt())));
    }

    @Test
    public static void testReadList3(Testing t) throws IOException {
        checkRead(t, "\1\0\0\0\1\1\0\0\0\2\1\0\0\0\3\0", in ->
            t.assertEquals(List.of(1, 2, 3),
                ObjectSequences.readList(in, in1 -> in.readInt())));
    }

    @Test
    public static void testReadListEOF1(Testing t) throws IOException {
        checkRead(t, "", in ->
            t.expect(() ->
                ObjectSequences.readList(
                    in, in1 -> t.assertFail("object read despite EOF")),
                EOFException.class));
    }

    @Test
    public static void testReadListEOF2(Testing t) throws IOException {
        checkRead(t, "\1\0\0\0\0", in ->
            t.expect(() ->
                ObjectSequences.readList(in, in1 -> in.readInt()),
                EOFException.class));
    }

    private static void checkWrite(Testing t, String expected,
        IOConsumer<DataOutputStream> writer) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.accept(new DataOutputStream(out));
        t.assertByteArrayEquals(expected.getBytes(StandardCharsets.ISO_8859_1),
            out.toByteArray());
    }

    private static <T> void checkRead(Testing t, String input,
        IOConsumer<DataInputStream> reader) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(
            input.getBytes(StandardCharsets.ISO_8859_1));
        reader.accept(new DataInputStream(in));
        t.assertEquals("Unread data in input", -1, in.read());
    }
}
