package enyo.io;

import enyo.io.function.IOConsumer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Methods for reading and writing sequences of objects.
 *
 * Unlike the platform serialization framework, this approach is
 * geared towards lazy processing of data sets of unbounded size.
 */
public final class ObjectSequences {
    /**
     * Writes a value as part of a sequence.
     *
     * The sequence continues after this value, and more values can be
     * written using the same method, until the sequence is terminated
     * using {@link #writeSequenceEnd(DataOutput)}.
     *
     * @param <S> the type of the stream the object is written to
     * @param <T> the type of the object
     * @param out the stream to which the object is written
     * @param object the object to write
     * @param writer the callback which is used to write the object
     * @throws IOException if a write error occurs
     * @see #writeSequenceEnd(DataOutput)
     */
    public static <S extends DataOutput, T> void writeInSequence(
        S out, T object, WriteObject<? super S, ? super T> writer)
        throws IOException {
        out.writeByte(1);
        writer.writeObject(out, object);
    }

    /**
     * Writes the end delimiter of a sequence.
     *
     * @param out the stream to which the sequence terminator is written
     * @throws IOException if a write error occurs
     * @see #writeInSequence(DataOutput,Object,WriteObject)
     */
    public static void writeSequenceEnd(DataOutput out) throws IOException {
        out.writeByte(0);
    }

    /**
     * Writes the values provided by the iterable.
     *
     * This sequence can be read back using
     * {@link #readList(DataInput,ReadObject)}.
     *
     * @param <S> the type of the stream the objects are written to
     * @param <T> the type of the object
     * @param out the stream to which the objecs are written
     * @param objects the values to write
     * @param writer the callback which is used to write the objects
     * @throws IOException if a write error occurs
     * @see #writeSequence(DataOutput,Stream,WriteObject)
     * @see #readList(DataInput,ReadObject)
     */
    public static <S extends DataOutput, T> void writeSequence(
        S out, Iterable<? extends T> objects,
        WriteObject<? super S, ? super T> writer) throws IOException {
        for (T object : objects)
            writeInSequence(out, object, writer);
        writeSequenceEnd(out);
    }

    /**
     * Writes the values contain in the stream.
     *
     * This is a terminal operation for the stream.
     *
     * This sequence can be read back using
     * {@link #readList(DataInput,ReadObject)}.
     *
     * @param <S> the type of the output the objects are written to
     * @param <T> the type of the object
     * @param out the sink to which the objecs are written
     * @param stream a stream of values to write
     * @param writer the callback which is used to write the objects
     * @throws IOException if a write error occurs
     * @see #writeSequence(DataOutput,Iterable,WriteObject)
     * @see #readList(DataInput,ReadObject)
     */
    public static <S extends DataOutput, T> void writeSequence(
        S out, Stream<? extends T> stream,
        WriteObject<? super S, ? super T> writer) throws IOException {
        try {
            stream.sequential()
                .forEachOrdered(IOConsumer.of(
                        object -> writeInSequence(out, object, writer)));
        } catch (UncheckedIOException e) {
            if (e.getCause() != null)
                throw e.getCause();
            else
                throw e;
        }
        writeSequenceEnd(out);
    }

    /**
     * Writes an optional value as a sequence of zero or one element.
     *
     * This degenerated sequence can be read back using
     * {@link #readList(DataInput,ReadObject)}, as a list containing
     * zero or one element.
     *
     * @param <S> the type of the stream the object (if any) is written to
     * @param <T> the type of the object
     * @param out the stream to which the object (if any) is written
     * @param object the optional value to write
     * @param writer the callback which is used to write the object
     * @throws IOException if a write error occurs
     * @see #writeAsOptional(DataOutput, Optional,WriteObject)
     */
    public static <S extends DataOutput, T> void writeAsSequence(
        S out, Optional<? extends T> object,
        WriteObject<? super S, ? super T> writer) throws IOException {
        if (object.isPresent())
            writeInSequence(out, object.get(), writer);
        writeSequenceEnd(out);
    }

    /**
     * Writes an optional value as a stand-alone element.
     *
     * The optional value can be read back using
     * {@link #readObject(DataInput,ReadObject)}.  This serialization
     * form is not compatible with {@link #readList(DataInput,ReadObject)}.
     *
     * @param <S> the type of the stream the object (if any) is written to
     * @param <T> the type of the object
     * @param out the stream to which the object (if any) is written
     * @param object the optional value to write
     * @param writer the callback which is used to write the object
     * @throws IOException if a write error occurs
     * @see #writeAsSequence(DataOutput, Optional,WriteObject)
     */
    public static <S extends DataOutput, T> void writeAsOptional(
        S out, Optional<? extends T> object,
        WriteObject<? super S, ? super T> writer) throws IOException {
        if (object.isPresent())
            writeInSequence(out, object.get(), writer);
        else
            writeSequenceEnd(out);
    }

    /**
     * Returns the next element of the sequence in the stream.
     *
     * An empty optional value is returned if the end of the sequence
     * is reached.
     *
     * @param <S> the type of the stream from which the value is read
     * @param <T> the type of the object being read
     * @param in the stream from which the value is read
     * @param reader callback function used to read the object (if any)
     * @return an optional value
     * @throws IOException if a read error occurs
     */
    public static <S extends DataInput, T> Optional<T> readObject(
        S in, ReadObject<? super DataInput, T> reader) throws IOException {
        byte marker = in.readByte();
        switch (marker) {
        case 0:
            return Optional.empty();
        case 1:
            return Optional.of(reader.readObject(in));
        default:
            throw new IOException("Corrupted object sequence: byte " + marker);
        }
    }

    /**
     * Reads a list of objects from the stream.  The objects
     * must have been written by
     * {@link #writeSequence(DataOutput,Iterable,WriteObject)},
     * or {@link #writeInSequence(DataOutput,Object,WriteObject)}
     * followed by {@link #writeSequenceEnd(DataOutput)}.
     *
     * The mutability of the returned list is undefined.
     *
     * @param <S> the type of the stream from which the sequence is read
     * @param <T> the type of the objects being read
     * @param in the stream from which the sequence is read
     * @param reader callback function used to read individual objects
     * @return a list with the objects in the sequence (possibly empty)
     * @throws IOException if a read error occurs
     * @see #writeSequence(DataOutput,Iterable,WriteObject)
     */
    public static <S extends DataInput, T> List<T> readList(
        S in, ReadObject<? super DataInput, T> reader) throws IOException {
        ArrayList<T> result;
        byte marker = in.readByte();
        switch (marker) {
        case 0:
            return List.of();
        case 1:
            result = new ArrayList<>();
            result.add(reader.readObject(in));
            break;
        default:
            throw new IOException(
                "Corrupted object sequence start: byte " + marker);
        }

        while (true) {
            marker = in.readByte();
            switch (marker) {
            case 0:
                return result;
            case 1:
                result.add(reader.readObject(in));
                break;
            default:
                throw new IOException(String.format(
                        "Corrupted object sequence after %d elements: byte %d",
                        result.size(), marker));
            }
        }
    }

    /**
     * Write a string of arbitrary length.
     * First, the number of characters (not bytes) is written,
     * as a four-byte integer.
     * The string itself is written in a modified UTF-8 encoding:
     * <ul>
     * <li>The characters from U+0000 to U+007F are encoded as bytes
     *   {@code 0x00} to {@code 0x7f}.
     * <li>The characters from U+0080 to U+07FF are encoded as two bytes
     *   {@code 0b110x xxyy} and {@code 0b10yy yyyy}, where the {@code y}
     *   correspond to the lower eight bits of the character code,
     *   and the {@code x} to the next three bits.
     * <li>The characters from U+0800 to U+FFFF are written as three bytes
     *   {@code 0b1110 yyyy}, {@code 0b10yy yyxx}, {@code 0b10yy yyyy},
     *   where the {@code y} correspond to the lower eight bits of the
     *   the character code, and the {@code x} to the upper eight bits.
     * <li>Surrogate characters do not receive special treatment.
     * </ul>
     *
     * <p>These are the difference to the encoding used by
     * {@link DataOutput#writeUTF(String)}:
     * <ul>
     * <li>The length contains characters in the input, not bytes in the output.
     * <li>The length is an {@code int}, not just 16 bits.
     * <li>There is no special treatment for the null character, it
     *   it is encoded as a null byte.
     * </ul>
     * The first two changes allow encoding of arbitrary strings.
     * @param out where the string is written
     * @param string the string to write
     * @throws IOException if an output error occurs
     */
    public static void writeString(DataOutput out, String string)
        throws IOException {
        int length = string.length();
        out.writeInt(length);
        for (int i = 0; i < length; ++i) {
            char ch = string.charAt(i);
            if (ch < 0x80)
                out.write(ch);
            else if (ch < 0x0800) {
                out.write(0xc0 | (ch >> 6));
                out.write(0x80 | (ch & 0x3f));
            } else {
                out.write(0xe0 | (ch >> 12));
                out.write(0x80 | ((ch >> 6) & 0x3f));
                out.write(0x80 | (ch & 0x3f));
            }
        }
    }

    /**
     * Read a string from the input.
     * The string must have been written using
     * {@link #writeString(DataOutput,String)} (see there for a format
     * description).
     * @param in the data source containing the serialized string
     * @return the string that has been read from the input
     * @throws java.io.EOFException if an immedate end-of-stream occurred, or
     *   the stream is truncated in the middle of the string
     * @throws IOException if the string is malformed, or an input error occured
     */
    public static String readString(DataInput in) throws IOException {
        int length = in.readInt();
        if (length < 0)
            throw new IOException("Malformed string length: " + length);
        else if (length == 0)
            return "";
        else if (length == 1) {
            char ch = readChar(in);
            return Character.toString(ch);
        } else if (length <= 4096) {
            // Allocate reasonable sizes directly, without checking
            // that the stream contains that many bytes.
            char[] buffer = new char[length];
            for (int i = 0; i < length; ++i)
                buffer[i] = readChar(in);
            return String.valueOf(buffer);
        } else {
            // Large strings need to be constructed piece-by-piece, to
            // avoid corrupted (short) streams to trigger overly large
            // allocations.
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; ++i)
                sb.append(readChar(in));
            return sb.toString();
        }
    }

    /**
     * Read a character in modified UTF-8 encoding.
     */
    private static char readChar(DataInput in) throws IOException  {
        byte b1 = in.readByte();
        if (b1 >= 0)
            return (char) b1;
        if (b1 >= -16)
            throw new IOException("Invalid modified UTF-8 leader: " + b1);
        if (b1 >= -32) {
            byte b2 = in.readByte();
            if ((b2 & 0xc0) != 0x80)
                throw new IOException("Invalid modified UTF-8 follower:" + b2);
            byte b3 = in.readByte();
            if ((b3 & 0xc0) != 0x80)
                throw new IOException("Invalid modified UTF-8 follower: " + b3);
            int result =
                ((b1 & 0x0f) << 12)
                | ((b2 & 0x3f) << 6)
                | (b3 & 0x3f);
            if (result < 0x0800)
                throw new IOException(String.format(
                        "Overlong modified UTF-8: %d, %d, %d", b1, b2, b3));
            return (char) result;
        } else {
            if ((b1 & 0xe0) != 0xc0)
                throw new IOException("Invalid modified UTF-8 leader: " + b1);
            byte b2 = in.readByte();
            if ((b2 & 0xc0) != 0x80)
                throw new IOException("Invalid modified UTF-8 follower: " + b2);
            int result = (((b1 & 0x1f) << 6) | (b2 & 0x3f));
            if (result < 0x80)
                throw new IOException(String.format(
                        "Overlong modified UTF-8: %d, %d", b1, b2));
            return (char) result;
        }
    }

    private ObjectSequences() { } // No instances.
}
