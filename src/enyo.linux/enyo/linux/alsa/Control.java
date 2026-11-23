package enyo.linux.alsa;

import enyo.io.ExtendedCharsets;
import enyo.linux.Descriptor;
import enyo.linux.Ioctl;
import enyo.linux.OpenFlag;
import enyo.linux.SyscallException;
import enyo.linux.Errno;
import enyo.util.OpenEnum;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/**
 * Access to the top-level control file.
 */
public final class Control implements AutoCloseable {
    /**
     * Creates a new control device for the numbered device.
     * @param number the device number
     * @see #availableControls()
     */
    public Control(int number) {
        if (number < 0)
            throw new IllegalArgumentException("number: " + number);

        this.number = number;
        descriptor = Descriptor.open(Path.of("/dev/snd/controlC" + number),
                                     OpenFlag.O_RDWR);
    }

    /**
     * Lists the available control devices.
     * Control devices for the numbers can be created using
     * {@link #Control(int)}.
     * @return an immutable list of integers
     */
    public static List<Integer> availableControls() {
        try {
            return Files.list(Path.of("/dev/snd"))
                .flatMap(path -> {
                        String prefix = "controlC";
                        String fileName = path.getFileName().toString();
                        if (fileName.startsWith(prefix)) {
                            String suffix = fileName.substring(prefix.length());
                            int number;
                            try {
                                number = Integer.parseInt(suffix);
                            } catch (NumberFormatException e) {
                                return Stream.of();
                            }
                            if (number >= 0)
                                return Stream.of(number);
                        }
                        return Stream.of();
                    })
                .sorted()
                .collect(Collectors.toUnmodifiableList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        descriptor.close();
    }

    /**
     * Returns the number that was used to open this control file.
     * @return a non-negative integer
     */
    public int number() {
        return number;
    }

    /**
     * Returns the version number.
     * @return the version number, as reported by
     * {@code SNDRV_CTL_IOCTL_PVERSION}.
     */
    public int version() {
        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocate(ValueLayout.JAVA_INT);
            Ioctl.ioctl(
                descriptor, Constants.SNDRV_CTL_IOCTL_PVERSION, buffer);
            return buffer.get(ValueLayout.JAVA_INT, 0);
        }
    }

    /**
     * Information about a card.
     * Corresponds to {@code struct snd_ctl_card_info}.
     */
    public static final class CardInfo {
        /**
         * Returns the card number.
         * @return an integer
         */
        public int card() {
            return card;
        }

        /**
         * Returns the ID of the card.
         * This can be configured by the system administrator.
         * @return a string
         */
        public String id() {
            return id;
        }

        /**
         * Returns the driver name.
         * @return a string
         */
        public String driver() {
            return driver;
        }

        /**
         * Returns the short name of the card.
         * @return a string
         */
        public String name() {
            return name;
        }

        /**
         * Returns the name and some information about the card.
         * @return a string
         */
        public String longName() {
            return longName;
        }

        /**
         * Returns the visual mixer identification.
         * @return a string
         */
        public String mixerName() {
            return mixerName;
        }

        /**
         * Returns the card components and fine identification.
         * @return an immutable list of strings
         */
        public List<String> components() {
            return components;
        }

        @Override
        public String toString() {
            return String.format(
                "CardInfo(card=%d, id=%s, driver=%s, name=%s, longName=%s,"
                + " mixerName=%s, components=%s",
                card(), id(), driver(), name(), longName(),
                mixerName(), components());
        }

        private final int card;
        private final String id;
        private final String driver;
        private final String name;
        private final String longName;
        private final String mixerName;
        private final List<String> components;

        private static final int SIZE = 376;

        private CardInfo(byte[] ioctlOutput) {
            ByteBuffer buffer = Native.wrap(ioctlOutput);
            card = buffer.getInt();
            buffer.getInt();
            id = Native.getString(buffer, 16);
            driver = Native.getString(buffer, 16);
            name = Native.getString(buffer, 32);
            longName = Native.getString(buffer, 80);
            buffer.position(buffer.position() + 16);
            mixerName = Native.getString(buffer, 80);
            components = components0(Native.getString(buffer, 128));
        }

        private List<String> components0(String components) {
            ArrayList<String> result = new ArrayList<>();

            int i;
            for (i = 0; i < components.length(); ) {
                int space = components.indexOf(' ', i);
                if (space < 0)
                    break;
                if (space > i)
                    result.add(components.substring(i, space));
                i = space + 1;
            }
            if (i < components.length())
                result.add(components.substring(i));

            return List.copyOf(result);
        }
    }

    /**
     * Returns information about this card.
     * @return the card information
     * @throws IllegalStateException if this object has already been closed
     */
    public CardInfo info() {
        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocate(CardInfo.SIZE);
            Ioctl.ioctl(
                descriptor, Constants.SNDRV_CTL_IOCTL_CARD_INFO, buffer);
            return new CardInfo(buffer.toArray(ValueLayout.JAVA_BYTE));
        }
    }

    /**
     * An element of a card.
     * Correspond to {@code struct snd_ctl_elem_id}.
     * @see ElementInfo
     */
    public final static class Element {
        /**
         * Returns the numeric identifier.
         * @return a non-zero integer
         */
        public int id() {
            return id;
        }

        /**
         * The element interface type.
         * This corresponds to the {@code snd_ctl_elem_iface_t} type.
         */
        public interface Interface extends OpenEnum.OfInt {
            /**
             * Well-known ALSA control interface values.
             */
            enum E implements Interface, OpenEnum.OfInt.Direct {
                /**
                 * Global control.
                 */
                CARD,

                /**
                 * Hardware dependent device.
                 */
                HWDEP,

                /**
                 * Virtual mixer device.
                 */
                MIXER,

                /**
                 * PCM device.
                 */
                PCM,

                /**
                 * MIDI device.
                 */
                RAWMIDI,

                /**
                 * Timer device.
                 */
                TIMER,

                /**
                 * Sequencer client.
                 */
                SEQUENCER,
                ;
            }
            /**
             * Meta-type object for control interface values.
             */
            static final OpenEnum.OfInt.Type<Interface> T = OpenEnum.intType(
                MethodHandles.lookup(), Interface.class, E.class);
        }

        /**
         * Returns the type of interface.
         * @return an {@link Interface.E} constant if a known value
         *  is provided, or a wrapped integer otherwise
         */
        public Interface interfaceType() {
            return Interface.T.ofInt(iface);
        }

        /**
         * Returns the device or client number.
         * @return an integer
         */
        public int device() {
            return device;
        }

        /**
         * Returns the subdevice or substream number.
         * @return an integer
         */
        public int subdevice() {
            return subdevice;
        }

        /**
         * Returns the name of the element.
         * @return a string
         */
        public String name() {
            return name;
        }

        /**
         * Returns the index of the item.
         * @return an integer
         */
        public int index() {
            return index;
        }

        @Override
        public String toString() {
            return String.format(
                "Element(id=%d, iface=%s, device=%d, subdevice=%d, name=%s,"
                + " index=%d",
                id(), interfaceType(), device(), subdevice(), name(),
                index());
        }

        private final int id;
        private final int iface;
        private final int device;
        private final int subdevice;
        private final String name;
        private final int index;

        private static final int SIZE = 64;

        private Element(MemorySegment ioctlOutput) {
            id = (int) Struct.snd_ctl_elem_id$numid.get(ioctlOutput, 0L);
            iface = (int) Struct.snd_ctl_elem_id$iface.get(ioctlOutput, 0L);
            device = (int) Struct.snd_ctl_elem_id$device.get(ioctlOutput, 0L);
            subdevice = (int) Struct.snd_ctl_elem_id$subdevice.get(
                ioctlOutput, 0L);
            MemorySegment name0;
            try {
                name0 = (MemorySegment) Struct.snd_ctl_elem_id$name.invokeExact(
                    ioctlOutput, 0L);
            } catch (Throwable e) {
                throw new InternalError(e);
            }
            int len;
            for (len = 0; len < name0.byteSize(); ++len)
                if (name0.get(ValueLayout.JAVA_BYTE, len) == 0)
                    break;
            name = name0.getString(0, ExtendedCharsets.UTF_8S, len);
            index = (int) Struct.snd_ctl_elem_id$index.get(ioctlOutput, 0L);
        }

        private void writeTo(MemorySegment buffer) {
            Struct.snd_ctl_elem_id$numid.set(buffer, 0L, id);
            Struct.snd_ctl_elem_id$iface.set(buffer, 0L, iface);
            Struct.snd_ctl_elem_id$device.set(buffer, 0L, device);
            Struct.snd_ctl_elem_id$subdevice.set(buffer, 0L, subdevice);
            byte[] nameBytes = name.getBytes();
            if (nameBytes.length > 44)
                    throw new IllegalStateException(
                        "Name cannot be serialized");
            MemorySegment name0;
            try {
                name0 = (MemorySegment) Struct.snd_ctl_elem_id$name.invokeExact(
                    buffer, 0L);
            } catch (Throwable e) {
                throw new InternalError(e);
            }
            name0.copyFrom(MemorySegment.ofArray(nameBytes));
            name0.asSlice(nameBytes.length).fill((byte) 0);
            Struct.snd_ctl_elem_id$index.set(buffer, 0L, index);
        }
    }


    /**
     * Returns a list of elements associated with this card.
     * This corresponds to the {@code SNDRV_CTL_IOCTL_ELEM_LIST} ioctl.
     * @return an immutable list of elements
     * @see #elementInfo(Element)
     */
    public List<Element> elements() {
        while (true) {
            int count;
            try (var arena = Arena.ofConfined()) {
                var buf = arena.allocate(Struct.snd_ctl_elem_list);
                Ioctl.ioctl(
                    descriptor, Constants.SNDRV_CTL_IOCTL_ELEM_LIST, buf);
                count = (int) Struct.snd_ctl_elem_list$count.get(buf, 0L);
            }
            if (count < 0)
                throw new ArithmeticException();
            if (count == 0)
                return List.of();
            try (var arena = Arena.ofConfined()) {
                var buf =  arena.allocate(Struct.snd_ctl_elem_list);
                var pids = arena.allocate(Struct.snd_ctl_elem_id, count);
                Struct.snd_ctl_elem_list$pids.set(buf, 0L, pids);
                Struct.snd_ctl_elem_list$space.set(buf, 0L, count);
                Ioctl.ioctlUnchecked(
                    descriptor, Constants.SNDRV_CTL_IOCTL_ELEM_LIST, buf);
                int newCount = (int) Struct.snd_ctl_elem_list$count.get(buf, 0L);
                if (newCount != count)
                    // Retry with changed buffer size.
                    continue;
                int used = (int) Struct.snd_ctl_elem_list$used.get(buf, 0L);
                return pids.asSlice(0, Math.multiplyExact(
                        used, (int) Struct.snd_ctl_elem_id.byteSize()))
                    .elements(Struct.snd_ctl_elem_id)
                    .map(Element::new)
                    .toList();
            }
        }
    }

    /**
     * Information associated with a {@linkplain Element card element}.
     * Corresponds to {@code struct snd_ctl_elem_info}.
     * @see Element
     * @see Control#elementInfo(Element)
     */
    public final static class ElementInfo {
        /**
         * Returns the associated element.
         * @return the element
         */
        public Element element() {
            return element;
        }

        /**
         * Type information for a card element.
         * Corresponds to {@code snd_ctl_elem_type_t}.
         * @see ElementInfo#type()
         */
        public interface Type extends OpenEnum.OfInt {
            /**
             * Well-known card element type information values.
             */
            enum E implements ElementInfo.Type, OpenEnum.OfInt.Direct {
                /**
                 * No type information available.
                 */
                NONE,

                /**
                 * A boolean value.
                 */
                BOOLEAN,

                /**
                 * An integer value.  It can be 32 bits or 64 bits,
                 * depending on the platform.
                 */
                INTEGER,

                /**
                 * An enumerated value.
                 */
                ENUMERATED,

                /**
                 * A value containing raw bytes.
                 */
                BYTES,

                /**
                 * An AES/IEC 958 value describing a digital audio
                 * interface status.
                 */
                IEC958,

                /**
                 * A 64-bit integer value (similar to {@link Long}).
                 */
                INTEGER64,
                ;
            }
            /**
             * Meta-type object for card element type information values.
             */
            static final OpenEnum.OfInt.Type<ElementInfo.Type> T =
                OpenEnum.intType(
                    MethodHandles.lookup(), ElementInfo.Type.class, E.class);
        }

        /**
         * Returns the type of the element value.
         * @return a {@link Type} enum constant, or if that is not availabel,
         *   the raw integer for the type in the error alternative
         */
        public Type type() {
            return Type.T.ofInt(type);
        }

        /**
         * Access modes for an element.
         */
        public enum Access {
            /**
             * The control value can be written.
             */
            READ,

            /**
             * The control value can be read.
             */
            WRITE,

            /**
             * The value may change without notification.
             */
            VOLATILE,

            /**
             * Unused.
             */
            UNUSED_BIT_3,

            /**
             * The control value can be read as a {@linkplain TLV}.
             */
            TLV_READ,

            /**
             * The control value can be written as a {@linkplain TLV}.
             */
            TLV_WRITE,

            /**
             * A {@linkplain TLV} command is available.
             */
            TLV_COMMAND,

            /**
             * Unused.
             */
            UNUSED_BIT_7,

            /**
             * The control value does not have an effect, but reports
             * some form of status information.
             */
            INACTIVE,

            /**
             * Writel locking is available.
             */
            LOCK,

            /**
             * Access to owner information of a write lock.
             */
            OWNER,

            /**
             * Unused.
             */
            UNUSED_BIT_11,

            /**
             * Unused.
             */
            UNUSED_BIT_12,

            /**
             * Unused.
             */
            UNUSED_BIT_13,

            /**
             * Unused.
             */
            UNUSED_BIT_14,

            /**
             * Unused.
             */
            UNUSED_BIT_15,

            /**
             * Unused.
             */
            UNUSED_BIT_16,

            /**
             * Unused.
             */
            UNUSED_BIT_17,

            /**
             * Unused.
             */
            UNUSED_BIT_18,

            /**
             * Unused.
             */
            UNUSED_BIT_19,

            /**
             * Unused.
             */
            UNUSED_BIT_20,

            /**
             * Unused.
             */
            UNUSED_BIT_21,

            /**
             * Unused.
             */
            UNUSED_BIT_22,

            /**
             * Unused.
             */
            UNUSED_BIT_23,

            /**
             * Unused.
             */
            UNUSED_BIT_24,

            /**
             * Unused.
             */
            UNUSED_BIT_25,

            /**
             * Unused.
             */
            UNUSED_BIT_26,

            /**
             * Unused.
             */
            UNUSED_BIT_27,

            /**
             * The kernel uses a {@linkplain TLV} callback internally.
             */
            TLV_CALLBACK,

            /**
             * This control value exists solely for the benefit of
             * user space.
             */
            USER,

            /**
             * Unused.
             */
            UNUSED_BIT_30,

            /**
             * Unused.
             */
            UNUSED_BIT_31,
            ;

            /**
             * Returns a set with members whose corresponding bit is set.
             * @param bitset the value to convert to a set of enum constants
             * @return an immutable set that contains those enum
             *   constants whose ordinal value has its bit set in
             *   {@code bitset}
             */
            public static Set<Access> of(int bitset) {
                Access[] values = values();
                EnumSet<Access> result = EnumSet.noneOf(Access.class);
                for (int i = 0; i < values.length; ++i)
                    if ((bitset & (1 << i)) != 0)
                        result.add(values[i]);
                return Set.copyOf(result);
            }
        }

        /**
         * Returns the set of access flags associated with this element.
         * Corresponds to the {@code access} field in
         * {@code struct snd_ctl_elem_info}.
         * @return an immutable set
         */
        public Set<Access> access() {
            return Access.of(access);
        }

        /**
         * Returns the number of values that are available under this
         * control.  This corresponds to the length of the arrays
         * returned by {@link Control#longValues(ElementInfo)} and
         * {@link Control#booleanValues(ElementInfo)}
         * @return the number of values available;
         *   a small, non-negative integer
         */
        public int count() {
            return count;
        }

        /**
         * Returns the current owner of this control.
         * Corresponds to the {@code owner} field in
         * {@code struct snd_ctl_elem_info}.
         * @return the process ID of the current owner
         */
        public int owner() {
            return owner;
        }

        /**
         * Returns the minimum value of this control.
         * 32-bit integers are signed-extended to the {@code long} type.
         * Corresponds to the {@code value.integer.min} and
         * {@code value.integer64.min} fields in
         * {@code struct snd_ctl_elem_info}.
         * @return the value
         */
        public long min() {
            return min;
        }

        /**
         * Returns the maximum value of this control.
         * 32-bit integers are signed-extended to the {@code long} type.
         * Corresponds to the {@code value.integer.max} and
         * {@code value.integer64.max} fields in
         * {@code struct snd_ctl_elem_info}.
         * @return the value
         */
        public long max() {
            return max;
        }

        /**
         * Returns the step value for this control.
         * Some devices use this to indicate that not all possible
         * intervalues in the range in fact available.
         * Corresponds to the {@code value.integer.step} and
         * {@code value.integer64.step} fields in
         * {@code struct snd_ctl_elem_info}.
         * @return the step value; can be zero to indicate that there
         *   are no further restrictions on the value
         */
        public long step() {
            return step;
        }

        @Override
        public String toString() {
            return String.format("ElementInfo(type=%s,"
                + " access=%s, count=%d, owner=%d, min=%d, max=%d, step=%d)",
                type(), access(), count(), owner(), min(), max(), step());
        }

        private final Element element;
        private final int type;
        private final int access;
        private final int count;
        private final int owner;
        private final long min;
        private final long max;
        private final long step;

        private static final int SIZE = 272;
        private static final int VALUE_SIZE = 1224;

        private ElementInfo(MemorySegment buf) {
            MemorySegment seg;
            try {
                seg = (MemorySegment) Struct.snd_ctl_elem_info$id.invokeExact(
                    buf, 0L);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new InternalError(e);
            }
            element = new Element(seg);
            type = (int) Struct.snd_ctl_elem_info$type.get(buf, 0L);
            access = (int) Struct.snd_ctl_elem_info$access.get(buf, 0L);
            count = (int) Struct.snd_ctl_elem_info$count.get(buf, 0L);
            owner = (int) Struct.snd_ctl_elem_info$owner.get(buf, 0L);

            if (type == Type.E.BOOLEAN.ordinal()) {
                min = 0;
                max = 1;
                step = 0;
            } else if (type == Type.E.INTEGER.ordinal()) {
                // The min/max/step types are declared as long in the
                // UAPI, so their sizes are architecture-dependent.
                min = (long) Struct.snd_ctl_elem_info$integer$min.get(buf, 0L);
                max = (long) Struct.snd_ctl_elem_info$integer$max.get(buf, 0L);
                step = (long) Struct.snd_ctl_elem_info$integer$step.get(buf, 0L);
            } else if (type == Type.E.INTEGER64.ordinal()) {
                min = (long) Struct.snd_ctl_elem_info$integer64$min.get(buf, 0L);
                max = (long) Struct.snd_ctl_elem_info$integer64$max.get(buf, 0L);
                step = (long) Struct.snd_ctl_elem_info$integer64$step.get(buf, 0L);
            } else if (type == Type.E.ENUMERATED.ordinal()) {
                min = 0;
                max = (long) Struct.snd_ctl_elem_info$enumerated$items.get(buf, 0L);
                step = 0;
            } else {
                min = 0;
                max = 0;
                step = 0;
            }
        }
    }

    /**
     * Returns {@code ElementInfo} data for the specified element.
     * @param element the element to query
     * @return information related to the element
     * @throws IllegalArgumentException if {@code element} has a
     *   zero {@linkplain Element#id() ID}, which is not supported by the
     *   kernel interface
     * @throws SyscallException if the kernel reports an error
     * @throws NullPointerException if {@code element} is null
     */
    public ElementInfo elementInfo(Element element) {
        if (element.id() == 0)
            // Overloaded for index lookup.
            throw new IllegalArgumentException(
                "Numeric identifier 0 is not valid");

        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocate(Struct.snd_ctl_elem_info);
            element.writeTo(buffer);
            Ioctl.ioctl(
                descriptor, Constants.SNDRV_CTL_IOCTL_ELEM_INFO, buffer);
            return new ElementInfo(buffer);
        }
    }

    /**
     * Returns {@code ElementInfo} data for the specified index.
     * @param index the index to query
     * @return information related to the index
     * @throws SyscallException if the kernel reports an error
     */
    public ElementInfo elementInfo(int index) {
        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocate(Struct.snd_ctl_elem_info);
            Struct.snd_ctl_elem_id$index.set(
                (MemorySegment) Struct.snd_ctl_elem_info$id$numid.get(buffer, 0L),
                0L, index);
            Ioctl.ioctl(
                descriptor, Constants.SNDRV_CTL_IOCTL_ELEM_INFO, buffer);
            return new ElementInfo(buffer);
        }
    }

    /**
     * Returns the integer values associated with an element of type
     * {@link ElementInfo.Type.E#INTEGER} or
     * {@link ElementInfo.Type.E#INTEGER64}.
     * {@link ElementInfo.Type.E#INTEGER} values are sign-extended to type
     * {@code long}.
     * @param info the element to query for the data
     * @return a new array with a copy of the values
     * @throws IllegalArgumentException if the type in {@code info}
     *   is not an integer type
     * @throws SyscallException if the kernel does not provide the
     *   information
     * @throws NullPointerException if {@code info} is null
     */
    public long[] longValues(ElementInfo info) {
        var type = info.type();
        boolean typeInteger = type.equals(ElementInfo.Type.E.INTEGER);
        boolean typeInteger64 = type.equals(ElementInfo.Type.E.INTEGER64);
        if (!(typeInteger || typeInteger64))
            throw new IllegalArgumentException(
                "Type INTEGER or INTEGER64 expected: " + info.type());

        // We cannot easily dispatch on c_long's layout below, so
        // encode the type directly.
        if (Struct.c_long.byteSize() == 8) {
            typeInteger = false;
            typeInteger64 = true;
        }

        // Protect against mis-decoded out-of-bounds values.
        if (info.count() < 0 || info.count() > 128)
            throw new IllegalArgumentException(
                "Element count out of range: " + info.count());

        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocate(ElementInfo.VALUE_SIZE);
            info.element().writeTo(buffer);
            Ioctl.ioctl(
                descriptor, Constants.SNDRV_CTL_IOCTL_ELEM_READ, buffer);
            buffer = buffer.asSlice(Element.SIZE);
            long[] result = new long[info.count()];
            for (int i = 0; i < result.length; ++i)
                if (typeInteger)
                    result[i] = buffer.getAtIndex(ValueLayout.JAVA_INT, i);
                else
                    result[i] = buffer.getAtIndex(ValueLayout.JAVA_LONG, i);
            return result;
        }
    }

    /**
     * Returns the boolean values associated with an element of type
     * {@link ElementInfo.Type.E#BOOLEAN}.
     * @param info the element to query for the data
     * @return a new array with a copy of the values
     * @throws IllegalArgumentException if the type in {@code info}
     *   is not {@link ElementInfo.Type.E#BOOLEAN}.
     * @throws SyscallException if the kernel does not provide the
     *   information
     * @throws NullPointerException if {@code info} is null
     */
    public boolean[] booleanValues(ElementInfo info) {
        if (!info.type().equals(ElementInfo.Type.E.BOOLEAN))
            throw new IllegalArgumentException(
                "Type BOOLEAN expected: " + info.type());

        // Protect against mis-decoded out-of-bounds values.
        if (info.count() < 0 || info.count() > 128)
            throw new IllegalArgumentException(
                "Element count out of range: " + info.count());

        boolean typeInteger = Struct.c_long.byteSize() != 8 ;

        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocate(ElementInfo.VALUE_SIZE);
            info.element().writeTo(buffer);
            Ioctl.ioctl(
                descriptor, Constants.SNDRV_CTL_IOCTL_ELEM_READ, buffer);
            buffer = buffer.asSlice(Element.SIZE);
            boolean[] result = new boolean[info.count()];
            for (int i = 0; i < result.length; ++i)
                if (typeInteger)
                    result[i] = buffer.getAtIndex(ValueLayout.JAVA_INT, i) != 0;
                else
                    result[i] = buffer.getAtIndex(ValueLayout.JAVA_LONG, i) != 0;
            return result;
        }
    }

    /**
     * Reads a TLV pairing from a control.
     * @param info identifies the element whose TLV information should be read
     * @return the TLV structure returned form the kernel
     * @throws SyscallException if the kernel does not provide the
     *   information
     * @throws NullPointerException if {@code info} is null
     */
    public TLV readTlv(ElementInfo info) {
        int size = 2048;
        while (true)
            try (var arena = Arena.ofConfined()) {
                var buffer = arena.allocate(size);
                // struct snd_ctl_tlv {
                //   unsigned int numid;
                //   unsigned int length;
                //   unsigned int tlv[]
                // };
                buffer.setAtIndex(ValueLayout.JAVA_INT, 0, info.element().id());
                buffer.setAtIndex(ValueLayout.JAVA_INT, 1, size - 8);
                try {
                    Ioctl.ioctlUnchecked(
                        descriptor, Constants.SNDRV_CTL_IOCTL_TLV_READ, buffer);
                } catch (SyscallException e) {
                    // Overloaded error code: In this case, it likely
                    // means to retry with a larger buffer size.
                    if (e.errno() == Errno.ENOMEM) {
                        size = Math.addExact(size, size);
                        continue;
                    }
                    throw e;
                }
                int type = buffer.getAtIndex(ValueLayout.JAVA_INT, 2);
                int nestedLen = buffer.getAtIndex(ValueLayout.JAVA_INT, 3);
                int totalLen = Math.addExact(8, nestedLen);
                if (totalLen > size - 8)
                    throw new SoundException(String.format(
                            "TLV length mismatch: length %d, nested size %d",
                            size, nestedLen));
                return new TLV(type,
                    MemorySegment.ofArray(
                        buffer.asSlice(16, nestedLen).toArray(
                            ValueLayout.JAVA_BYTE)).asReadOnly());
            }
    }

    /**
     * Sets the preferred PCM subdevice.
     * Corresponds to the {@code SNDRV_CTL_IOCTL_PCM_PREFER_SUBDEVICE}
     * {@code ioctl}.
     * @param subdevice the subdevice ID
     * @throws SyscallException if the subdevice could not be set
     */
    public void preferPCMSubdevice(int subdevice) {
        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocateFrom(ValueLayout.JAVA_INT, subdevice);
            Ioctl.ioctl(descriptor,
                Constants.SNDRV_CTL_IOCTL_PCM_PREFER_SUBDEVICE, buffer);
        }
    }

    /**
     * Sets the preferred MIDI subdevice.
     * Corresponds to the {@code SNDRV_CTL_IOCTL_RAWMIDI_PREFER_SUBDEVICE}
     * {@code ioctl}.
     * @param subdevice the subdevice ID
     * @throws SyscallException if the subdevice could not be set
     */
    public void preferMIDISubdevice(int subdevice) {
        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocateFrom(ValueLayout.JAVA_INT, subdevice);
            Ioctl.ioctl(descriptor,
                Constants.SNDRV_CTL_IOCTL_RAWMIDI_PREFER_SUBDEVICE, buffer);
        }
    }

    @Override
    public String toString() {
        return "enyo.linux.alsa.Control(" + number + ')';
    }

    private final int number;
    private Descriptor descriptor;

    private final static class Struct {
        static final StructLayout snd_ctl_elem_id = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("numid"),
            ValueLayout.JAVA_INT.withName("iface"),
            ValueLayout.JAVA_INT.withName("device"),
            ValueLayout.JAVA_INT.withName("subdevice"),
            MemoryLayout.sequenceLayout(
                Constants.SNDRV_CTL_ELEM_ID_NAME_MAXLEN,
                ValueLayout.JAVA_BYTE).withName("name"),
            ValueLayout.JAVA_INT.withName("index"));
        static final VarHandle snd_ctl_elem_id$numid =
            snd_ctl_elem_id.varHandle(groupElement("numid"));
        static final VarHandle snd_ctl_elem_id$iface =
            snd_ctl_elem_id.varHandle(groupElement("iface"));
        static final VarHandle snd_ctl_elem_id$device =
            snd_ctl_elem_id.varHandle(groupElement("device"));
        static final VarHandle snd_ctl_elem_id$subdevice =
            snd_ctl_elem_id.varHandle(groupElement("subdevice"));
        static final MethodHandle snd_ctl_elem_id$name =
            snd_ctl_elem_id.sliceHandle(groupElement("name"));
        static final VarHandle snd_ctl_elem_id$index =
            snd_ctl_elem_id.varHandle(groupElement("index"));

        static final StructLayout snd_ctl_elem_list = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("offset"),
            ValueLayout.JAVA_INT.withName("space"),
            ValueLayout.JAVA_INT.withName("used"),
            ValueLayout.JAVA_INT.withName("count"),
            ValueLayout.ADDRESS.withName("pids"), // snd_ctl_elem_id
            MemoryLayout.sequenceLayout(50, ValueLayout.JAVA_BYTE),
            // Only supports two pointer sizes (64 bits and 32 bits).
            MemoryLayout.paddingLayout(
                ValueLayout.ADDRESS.byteSize() == 8 ? 6 : 2));
        static final VarHandle snd_ctl_elem_list$space =
            snd_ctl_elem_list.varHandle(groupElement("space"));
        static final VarHandle snd_ctl_elem_list$used =
            snd_ctl_elem_list.varHandle(groupElement("used"));
        static final VarHandle snd_ctl_elem_list$count =
            snd_ctl_elem_list.varHandle(groupElement("count"));
        static final VarHandle snd_ctl_elem_list$pids =
            snd_ctl_elem_list.varHandle(groupElement("pids"));

        static final StructLayout snd_ctl_elem_info;

        static final MemoryLayout c_long;

        static {
            var linker = Linker.nativeLinker();
            c_long = linker.canonicalLayouts().get("long");
            var u64 = linker.canonicalLayouts().get("long long");
            snd_ctl_elem_info = MemoryLayout.structLayout(
                snd_ctl_elem_id.withName("id"),
                ValueLayout.JAVA_INT.withName("type"),
                ValueLayout.JAVA_INT.withName("access"),
                ValueLayout.JAVA_INT.withName("count"),
                ValueLayout.JAVA_INT.withName("owner"),
                MemoryLayout.unionLayout(
                    MemoryLayout.structLayout(
                        c_long.withName("min"),
                        c_long.withName("max"),
                        c_long.withName("step"))
                    .withName("integer"),
                    MemoryLayout.structLayout(
                        u64.withName("min"),
                        u64.withName("max"),
                        u64.withName("step"))
                    .withName("integer64"),
                    MemoryLayout.structLayout(
                        ValueLayout.JAVA_INT.withName("items"),
                        ValueLayout.JAVA_INT.withName("item"),
                        MemoryLayout.sequenceLayout(
                            64, ValueLayout.JAVA_BYTE).withName("name"),
                        u64.withName("names_ptr"),
                        ValueLayout.JAVA_INT.withName("names_length"))
                    .withName("enumerated"),
                    MemoryLayout.sequenceLayout(128, ValueLayout.JAVA_BYTE))
                .withName("value"),
                MemoryLayout.sequenceLayout(64, ValueLayout.JAVA_BYTE));
        }

        static final MethodHandle snd_ctl_elem_info$id =
            snd_ctl_elem_info.sliceHandle(groupElement("id"));
        static final VarHandle snd_ctl_elem_info$id$numid =
            snd_ctl_elem_info.varHandle(
                groupElement("id"), groupElement("numid"));
        static final VarHandle snd_ctl_elem_info$type =
            snd_ctl_elem_info.varHandle(groupElement("type"));
        static final VarHandle snd_ctl_elem_info$access =
            snd_ctl_elem_info.varHandle(groupElement("access"));
        static final VarHandle snd_ctl_elem_info$count =
            snd_ctl_elem_info.varHandle(groupElement("count"));
        static final VarHandle snd_ctl_elem_info$owner =
            snd_ctl_elem_info.varHandle(groupElement("owner"));
        static final VarHandle snd_ctl_elem_info$integer$min =
            snd_ctl_elem_info.varHandle(groupElement("value"),
                groupElement("integer"), groupElement("min"));
        static final VarHandle snd_ctl_elem_info$integer$max =
            snd_ctl_elem_info.varHandle(groupElement("value"),
                groupElement("integer"), groupElement("max"));
        static final VarHandle snd_ctl_elem_info$integer$step =
            snd_ctl_elem_info.varHandle(groupElement("value"),
                groupElement("integer"), groupElement("step"));
        static final VarHandle snd_ctl_elem_info$integer64$min =
            snd_ctl_elem_info.varHandle(groupElement("value"),
                groupElement("integer64"), groupElement("min"));
        static final VarHandle snd_ctl_elem_info$integer64$max =
            snd_ctl_elem_info.varHandle(groupElement("value"),
                groupElement("integer64"), groupElement("max"));
        static final VarHandle snd_ctl_elem_info$integer64$step =
            snd_ctl_elem_info.varHandle(groupElement("value"),
                groupElement("integer64"), groupElement("step"));
        static final VarHandle snd_ctl_elem_info$enumerated$items =
            snd_ctl_elem_info.varHandle(groupElement("value"),
                groupElement("enumerated"), groupElement("items"));
    }
}
