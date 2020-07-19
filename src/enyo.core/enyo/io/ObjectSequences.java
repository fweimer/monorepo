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

    private ObjectSequences() { } // No instances.
}
