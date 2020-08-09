package enyo.io.tests;

import enyo.io.ObjectSequences;
import enyo.io.function.IOConsumer;
import enyo.testing.Cost;
import enyo.testing.ExitDuringTest;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.util.ByteArrays;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExitDuringTest
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

    @Test
    @Cost(0.2)
    public static void testWriteString(Testing t) throws IOException {
        checkWrite(t, "\0\0\0\0",
            out -> ObjectSequences.writeString(out, ""));

        // The null character is special.
        checkWrite(t, "\0\0\0\1\0",
            out -> ObjectSequences.writeString(out, "\0"));

        // Other outputs are similar to what
        // DataOutputStream.writeUTF() produces.
        ByteArrayOutputStream outExpected = new ByteArrayOutputStream();
        DataOutputStream dataExpected = new DataOutputStream(outExpected);
        ByteArrayOutputStream outActual = new ByteArrayOutputStream();
        DataOutputStream dataActual = new DataOutputStream(outActual);
        for (int i = 1; i <= 65535; ++i) {
            String string = Character.toString((char) i);

            outExpected.reset();
            // Compensate for missing length bytes.
            outExpected.write(0);
            outExpected.write(0);
            dataExpected.writeUTF(string);
            byte[] expected = outExpected.toByteArray();
            // Compensate for byte counts instead of character counts.
            expected[3] = 1;

            outActual.reset();
            ObjectSequences.writeString(dataActual, string);
            byte[] actual = outActual.toByteArray();

            t.assertByteArrayEquals(expected, actual);
        }

        // Check one multi-character string.
        checkWrite(t, "\0\0\0\3[@]",
            out -> ObjectSequences.writeString(out, "[@]"));
    }

    @Test
    @Cost(0.2)
    public static void testReadString(Testing t) throws IOException {
        // The null character is special.
        checkRead(t, "\0\0\0\1\0", in ->
            t.assertEquals("\0", ObjectSequences.readString(in)));
        checkRead(t, "\0\0\0\3[\0]", in ->
            t.assertEquals("[\0]", ObjectSequences.readString(in)));

        // Other inputs are similar to what
        // DataOutputStream.writeUTF() produces.
        WriteStringOutput out = new WriteStringOutput();
        for (int i = 1; i <= 65535; ++i) {
            String expected = Character.toString((char) i);
            ByteArrayInputStream in = new ByteArrayInputStream(
                out.toBytesPlatform(expected));
            DataInputStream dataIn = new DataInputStream(in);
            String actual = ObjectSequences.readString(dataIn);
            t.assertEquals(expected, actual);
            t.assertEquals(-1, in.read());
        }

        // Similar test with three characters.
        for (int i = 1; i <= 65535; ++i) {
            String expected = "[" + Character.toString((char) i) + "]";
            ByteArrayInputStream in = new ByteArrayInputStream(
                out.toBytesPlatform(expected));
            DataInputStream dataIn = new DataInputStream(in);
            t.assertEquals(expected, ObjectSequences.readString(dataIn));
            t.assertEquals(-1, in.read());
        }
    }

    @Test
    @Cost(0.2)
    public static void testReadStringTruncated(Testing t) throws IOException {
        WriteStringOutput out = new WriteStringOutput();
        for (int i = 0; i <= 65535; ++i) {
            byte[] input = out.toBytes((char) i);
            ByteArrayInputStream in = new ByteArrayInputStream(
                input, 0, input.length - 1);
            DataInputStream dataIn = new DataInputStream(in);
            t.expect(() -> ObjectSequences.readString(dataIn),
                EOFException.class);
        }
    }

    @Cost(0.5)
    @Test
    public static void testReadStringInvalid2(Testing t) throws IOException {
        WriteStringOutput out = new WriteStringOutput();

        byte[] buffer = new byte[4 + 2];
        buffer[3] = 1;          // One character.
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        DataInputStream dataIn = new DataInputStream(in);
        for (int b1 = 0; b1 <= 255; ++b1) {
            buffer[4] = (byte) b1;
            for (int b2 = 0; b2 <= 255; ++b2) {
                buffer[5] = (byte) b2;
                in.reset();

                readStringInvalidTail(t, buffer, dataIn, out);
            }
        }
    }

    @Test
    @Cost(10)
    public static void testReadStringInvalid3(Testing t) throws IOException {
        WriteStringOutput out = new WriteStringOutput();

        byte[] buffer = new byte[4 + 3];
        buffer[3] = 1;          // One character.
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        DataInputStream dataIn = new DataInputStream(in);
        // Do not test leading ASCII bytes because they are all valid.
        for (int b1 = 128; b1 <= 255; ++b1) {
            buffer[4] = (byte) b1;
            for (int b2 = 0; b2 <= 255; ++b2) {
                buffer[5] = (byte) b2;
                for (int b3 = 0; b3 <= 255; ++b3) {
                    buffer[6] = (byte) b3;
                    in.reset();

                    readStringInvalidTail(t, buffer, dataIn, out);
                }
            }
        }
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

    private static final class WriteStringOutput {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final DataOutputStream dataOut = new DataOutputStream(out);

        /**
         * Generates the expected output for a single character using
         * {@link ObjectSequences#writeString(DataOutput,String)}.
         */
        byte[] toBytes(char ch) throws IOException {
            return toBytes(Character.toString(ch));
        }

        /**
         * Generates the expected output for a single character using
         * {@link ObjectSequences#writeString(DataOutput,String)}.
         */
        byte[] toBytes(String string) throws IOException {
            out.reset();
            ObjectSequences.writeString(dataOut, string);
            return out.toByteArray();
        }

        /**
         * Uses the platform implementation to produce the same output
         * as {@link #toBytes(char)}.
         */
        byte[] toBytesPlatform(char ch) throws IOException {
            if (ch == 0)
                // Special case: ObjectSequences::writeString does not
                // use overlong sequences.
                return new byte[5];
            out.reset();
            // Additional length bytes.
            dataOut.writeByte(0);
            dataOut.writeByte(0);
            dataOut.writeUTF(Character.toString(ch));
            byte[] result = out.toByteArray();
            // Patch in length in characters (not bytes).
            result[3] = 1;
            return result;
        }

        /**
         * Null input characters are not supported.
         */
        byte[] toBytesPlatform(String input) throws IOException {
            if (input.indexOf(0) >= 0 || input.length() > 255)
                throw new IllegalArgumentException();
            out.reset();
            // Additional length bytes.
            dataOut.writeByte(0);
            dataOut.writeByte(0);
            dataOut.writeUTF(input);
            byte[] result = out.toByteArray();
            // Patch in length in characters (not bytes).
            result[3] = (byte) input.length();
            return result;
        }
    }

    private static void readStringInvalidTail(Testing t, byte[] buffer,
        DataInputStream dataIn, WriteStringOutput out) throws IOException {
        String decoded;
        try {
            decoded = ObjectSequences.readString(dataIn);
        } catch (IOException e) {
            // If the sequence is invalid, no further checks
            // are needed.  testReadString() already ensures
            // that valid sequences are accepted.
            return;
        }

        // Input is valid if buffer is a prefix of the real
        // encoding of the decoded string.  (buffer could
        // contain trailing bytes after it.)
        byte[] validInput = out.toBytes(decoded);
        if (!Arrays.equals(buffer, 0, validInput.length,
                validInput, 0, validInput.length))
            t.assertFail("Invalid input incorrectly accepted: "
                + ByteArrays.unsignedStream(buffer).boxed().collect(
                    Collectors.toList()));
    }
}
