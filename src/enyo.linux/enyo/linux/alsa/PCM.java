package enyo.linux.alsa;

import enyo.linux.Descriptor;
import enyo.linux.Errno;
import enyo.linux.Ioctl;
import enyo.linux.OpenFlag;
import enyo.linux.SyscallException;
import enyo.util.EnumBitSet;
import enyo.util.OpenEnum;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Path;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/**
 * Sound devices for sample playback and capture.
 *
 * This class is called {@code PCM} for historic reasons; it can also
 * be used to interface with sample-oriented devices that do use other
 * kinds of modulation.
 */
public final class PCM implements AutoCloseable {
    /**
     * Direction of a PCM device.
     */
    public interface Direction extends OpenEnum.OfInt {
        /**
         * Well-known PCM direction values.
         */
        enum E implements Direction, OpenEnum.OfInt.Direct {
            /**
             * The device is for playing back samples.
             */
            PLAYBACK,

            /**
             * The device is for recording samples.
             */
            CAPTURE,
            ;

            /**
             * Returns the character to mark the direction in device names.
             * @return a letter
             */
            public char marker() {
                return Character.toLowerCase(name().charAt(0));
            }
        }
        /**
         * Meta-type object for device directions.
         */
        static final OpenEnum.OfInt.Type<Direction> T = OpenEnum.intType(
            MethodHandles.lookup(), Direction.class, E.class);
    }

    /**
     * Identification of a PCM device.
     *
     * This class only provides a name for a device, it does not
     * actually represent an open device.  See {@link PCM} for that.
     *
     * @see PCM
     */
    public final static class Device implements Comparable<Device> {
        /**
         * Creates a new device identifier.
         * @param card the card number, a non-negative integer
         * @param device the device number, a non-negative integer
         * @param direction whether this is a playback or caputre device
         * @throws IllegalArgumentException if {@code card} or
         *   {@code device} are negative
         * @throws NullPointerException if {@code direction} is null
         * @see #Device(String)
         */
        public Device(int card, int device, Direction.E direction) {
            if (card < 0)
                throw new IllegalArgumentException("card: " + card);
            if (device < 0)
                throw new IllegalArgumentException("device: " + device);
            Objects.requireNonNull(direction, "direction");
            this.card = card;
            this.device = device;
            this.direction = direction;
        }

        /**
         * Creates a new device identifier from a file name.
         * Valid strings are of the form of device names under
         * {@code /dev/snd}.
         * @param fileName the file name of the device
         * @throws IllegalArgumentException if the file name cannot be
         *   parsed into a device name
         * @throws NullPointerException if {@code fileName} is null
         * @see #Device(int,int,Direction.E)
         */
        public Device(String fileName) {
            if (fileName.endsWith("c"))
                direction = Direction.E.CAPTURE;
            else if (fileName.endsWith("p"))
                direction = Direction.E.PLAYBACK;
            else
                direction = null;
            int offsetD = fileName.indexOf('D');
            if (fileName.startsWith("pcmC")
                && direction != null && offsetD >= 0) {
                try {
                    card = Integer.parseInt(fileName.substring(4, offsetD));
                    device = Integer.parseInt(fileName.substring(
                            offsetD + 1, fileName.length() - 1));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("fileName: " + fileName);
                }
                if (card >= 0 && device >= 0)
                    return;
            }
            throw new IllegalArgumentException("fileName: " + fileName);
        }

        /**
         * Returns the number of the card.
         * @return a non-negative integer
         * @see Control
         */
        public int card() {
            return card;
        }

        /**
         * Returns the number of the device.
         * @return a non-negative integer
         */
        public int device() {
            return device;
        }

        /**
         * Returns whether this device is for playback or capture.
         * @return the direction in which the device operates
         */
        public Direction.E direction() {
            return direction;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || o.getClass() != Device.class)
                return false;
            Device other = (Device) o;
            return card == other.card
                && device == other.device
                && direction.equals(other.direction);
        }

        @Override
        public int hashCode() {
            return device * 202  + card * 2 + direction.value();
        }

        @Override
        public String toString() {
            return "pcmC" + card() + 'D' + device() + direction().marker();
        }

        @Override
        public int compareTo(Device other) {
            int ret = card - other.card;
            if (ret != 0)
                return ret;
            ret = device - other.device;
            if (ret != 0)
                return ret;
            return Direction.T.compare(direction, other.direction);
        }

        private final int card;
        private final int device;
        private final Direction.E direction;
    }

    /**
     * Returns the list of PCM devices.  Some or all of these devices
     * cannot actually be opened, or trying to open them may block.
     * @return an immutable list, possibly empty
     */
    public static List<Device> devices() {
        Path dir = Path.of("/dev/snd");
        while (true) {
            try {
                return Files.list(dir).flatMap(path -> {
                        String fileName = path.getFileName().toString();
                        Device device;
                        try {
                            device = new Device(fileName);
                        } catch (IllegalArgumentException e) {
                            return Stream.of();
                        }
                        return Stream.of(device);
                    })
                    .sorted()
                    .collect(Collectors.toUnmodifiableList());
            } catch (NoSuchFileException e) {
                if (Files.isDirectory(dir))
                    // Some device file probably went a way.  Retry.
                    continue;
                else
                    throw new UncheckedIOException(e);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    enum Timestamp {
        GETTIMEPOFDAY,
        MONOTONIC,
        MONOTONIC_RAW,
        ;
    }

    /**
     * Opens the specified PCM device.
     * @param device the PCM device to open
     * @throws NullPointerException if {@code device} is null
     * @throws SyscallException if the device cannot be opened
     * @see #PCM(Control,int,Direction.E)
     */
    public PCM(Device device) {
        this.device = device;
        String path = "/dev/snd/" + device.toString();
        descriptor = Descriptor.open(Path.of(path), OpenFlag.O_WRONLY);


        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocateFrom(
                ValueLayout.JAVA_INT, 0x2000e); // version
            Ioctl.ioctl(descriptor, Constants.SNDRV_PCM_IOCTL_USER_PVERSION,
                buffer);
            buffer.set(ValueLayout.JAVA_INT, 0L,
                Timestamp.MONOTONIC.ordinal());
            Ioctl.ioctl(descriptor, Constants.SNDRV_PCM_IOCTL_TTSTAMP, buffer);
        }
    }

    /**
     * Opens the sepcified PCM device.
     * @param control the card to which the device belongs
     * @param device the device number within the card
     * @param direction whether this is a playback or capture device
     * @throws NullPointerException if {@code control} or
     *   {@code direction} are null
     * @throws SyscallException if the device cannot be opened
     * @see #PCM(Device)
     */
    public PCM(Control control, int device, Direction.E direction) {
        this(new Device(control.number(), device, direction));
    }

    @Override
    public synchronized void close() {
        frameSize = 0;
        descriptor.close();
    }

    /**
     * Returns the protocol version supported by the kernel.
     * @return a protocol version, such as {@code 0x2000e}
     * @throws SyscallException if an internal error occurs
     */
    public int protocolVersion() {
        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocate(ValueLayout.JAVA_INT);
            Ioctl.ioctl(descriptor, Constants.SNDRV_PCM_IOCTL_PVERSION, buffer);
            return buffer.get(ValueLayout.JAVA_INT, 0L);
        }
    }

    /**
     * Returns information about the PCM device.
     * @return the obtained information
     * @throws SyscallException if an internal error occurs
     */
    public Info info() {
        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocate(Info.SIZE);
            Ioctl.ioctl(descriptor, Constants.SNDRV_PCM_IOCTL_INFO, buffer);
            return new Info(
                buffer.asByteBuffer().order(ByteOrder.nativeOrder()));
        }
    }

    /**
     * Information about a PCM device.
     * This class corresponds to {@code struct snd_pcm_info}.
     * @see PCM#info()
     */
    public static final class Info {
        /**
         * Returns the device number.
         * Corresponds to the {@code device} field of
         * {@code struct snd_pcm_info}.
         * @return a small, non-negative integer
         * @see Device#device()
         */
        public int device() {
            return device;
        }

        /**
         * The subdevice number.  Typically zero.
         * Corresponds to the {@code subdevice} field of
         * {@code struct snd_pcm_info}.
         * @return the subdevice number
         */
        public int subdevice() {
            return subdevice;
        }

        /**
         * Returns whether this device is a playback or capture device.
         * Corresponds to the {@code stream} field of
         * {@code struct snd_pcm_info}.
         * @return a result of {@link Direction} if the kernel provided
         *   data, or an error result with the integer provided from the kernel
         */
        public Direction stream() {
            return Direction.T.ofInt(stream);
        }

        /**
         * Returns the card/control number.
         * Corresponds to the {@code card} field of
         * {@code struct snd_pcm_info}.
         * @return a small, non-negative integer
         * @see Control
         * @see Device#card()
         */
        public int card() {
            return card;
        }

        /**
         * Returns the user-assigned device name.
         * Corresponds to the {@code id} field of
         * {@code struct snd_pcm_info}.
         * @return a short, human-readable string
         */
        public String id() {
            return id;
        }

        /**
         * Returns the device name assigned by the kernel.
         * Corresponds to the {@code name} field of
         * {@code struct snd_pcm_info}.
         * @return a string
         */
        public String name() {
            return name;
        }

        /**
         * Returns the subdevice name assigned by the kernel.
         * Corresponds to the {@code subname} field of
         * {@code struct snd_pcm_info}.
         * @return a string
         */
        public String subdeviceName() {
            return subdeviceName;
        }

        /**
         * Returns the device class.
         * Corresponds to the {@code dev_class} field of
         * {@code struct snd_pcm_info}.
         * @return the {@linkplain DeviceClass device class} if available,
         *   or an integer if unknown
         */
        public DeviceClass deviceClass() {
            return DeviceClass.T.ofInt(deviceClass);
        }

        /**
         * Returns the number of subdevices.
         * Corresponds to the {@code subdevices_count} field of
         * {@code struct snd_pcm_info}.
         * @return a non-negative integer
         */
        public int subdevicesCount() {
            return subdevicesCount;
        }

        /**
         * Returns the number of subdevices that are vailable.
         * Corresponds to the {@code subdevices_avail} field of
         * {@code struct snd_pcm_info}.
         * @return a non-negative integer
         */
        public int subdevicesAvailable() {
            return subdevicesAvailable;
        }

        /**
         * Returns the hardware synchronization ID.
         * Corresponds to the {@code sync} field of
         * {@code struct snd_pcm_info}.
         * @return a copy of the synchronization ID
         */
        public byte[] sync() {
            return sync.clone();
        }

        private final int device;
        private final int subdevice;
        private final int stream;
        private final int card;
        private final String id;
        private final String name;
        private final String subdeviceName;
        private final int deviceClass;
        private final int subdevicesCount;
        private final int subdevicesAvailable;
        private final byte[] sync;

        private static final int SIZE = 288;

        private Info(ByteBuffer buffer) {
            device = buffer.getInt();
            subdevice = buffer.getInt();
            stream = buffer.getInt();
            card = buffer.getInt();
            id = Native.getString(buffer, 64);
            name = Native.getString(buffer, 80);
            subdeviceName = Native.getString(buffer, 32);
            deviceClass = buffer.getInt();
            buffer.getInt(); // device subclass, not actually used.
            subdevicesCount = buffer.getInt();
            subdevicesAvailable = buffer.getInt();
            sync = new byte[16];
            buffer.get(sync);
        }
    }

    /**
     * The device class.
     * @see Info#deviceClass()
     */
    public interface DeviceClass extends OpenEnum.OfInt {
        /**
         * Well-known device classes.
         */
        enum E implements DeviceClass, OpenEnum.OfInt.Direct {
            /**
             * A standard mono or stero device.
             * Corresponds to {@code SNDRV_PCM_CLASS_GENERIC}.
             */
            GENERIC,

            /**
             * A multi-channel device.
             * Corresponds to {@code SNDRV_PCM_CLASS_MULTI}.
             */
            MULTI,

            /**
             * A software modem.  This is a communication device.
             * Corresponds to {@code SNDRV_PCM_CLASS_MODEM}.
             */
            MODEM,

            /**
             * A digitizer.  Currently unused.
             * Corresponds to {@code SNDRV_PCM_CLASS_MODEM}.
             */
            DIGITIZER,
            ;
        }
        /**
         * Meta-type object for device classes.
         */
        OpenEnum.OfInt.Type<DeviceClass> T = OpenEnum.intType(
            MethodHandles.lookup(), DeviceClass.class, E.class);
    }

    /**
     * Endianness associated with a format.
     * @see Format#endian()
     */
    public enum Endian {
        /**
         * Big-endian (network) byte order.
         * @see java.nio.ByteOrder#BIG_ENDIAN
         */
        BIG,

        /**
         * Little-endian byte order.
         * @see java.nio.ByteOrder#LITTLE_ENDIAN
         */
        LITTLE,

        /**
         * The format is byte-order-agnostic.
         */
        NEITHER,
        ;
    }

    /**
     * The encoding associated with a format.
     * @see Format#encoding()
     */
    public enum Encoding {
        /**
         * The encoding uses unsigned integers.
         */
        SIGNED_INTEGER,

        /**
         * The encoding uses unsigned integers.
         */
        UNSIGNED_INTEGER,

        /**
         * The encoding uses some floating-point format.
         */
        FLOAT,

        /**
         * Unspecified type of encoding.
         */
        OTHER,
    }

    /**
     * The format of one sample.
     */
    public enum Format {
        /**
         * 8-bit signed samples.
         */
        S8(Encoding.SIGNED_INTEGER, 8, 1, Endian.NEITHER),

        /**
         * 8-bit unsigned samples.
         */
        U8(Encoding.UNSIGNED_INTEGER, 8, 1, Endian.NEITHER),

        /**
         * 16-bit signed samples in little-endian byte order.
         */
        S16_LE(16, 2),

        /**
         * 16-bit signed samples in big-endian byte order.
         */
        S16_BE(16, 2),

        /**
         * 16-bit unsigned samples in little-endian byte order.
         */
        U16_LE(16, 2),

        /**
         * 16-bit unsigned samples in big-endian byte order.
         */
        U16_BE(16, 2),

        /**
         * 24-bit signed samples in little-endian byte order, stored
         * in four bytes.
         */
        S24_LE(24, 4),

        /**
         * 24-bit signed samples in big-endian byte order, stored
         * in four bytes.
         */
        S24_BE(24, 4),

        /**
         * 24-bit unsigned samples in little-endian byte order, stored
         * in four bytes.
         */
        U24_LE(24, 4),

        /**
         * 24-bit unsigned samples in big-endian byte order, stored
         * in four bytes.
         */
        U24_BE(24, 4),

        /**
         * 32-bit signed samples in little-endian byte order.
         */
        S32_LE(32, 4),

        /**
         * 32-bit signed samples in big-endian byte order.
         */
        S32_BE(32, 4),

        /**
         * 32-bit unsigned samples in little-endian byte order.
         */
        U32_LE(32, 4),

        /**
         * 32-bit unsigned samples in big-endian byte order.
         */
        U32_BE(32, 4),

        /**
         * 32-bit IEEE binary32 floating point values in little-endian
         * byte order.
         */
        FLOAT_LE(32, 4),

        /**
         * 32-bit IEEE binary32 floating point values in big-endian
         * byte order.
         */
        FLOAT_BE(32, 4),

        /**
         * 64-bit IEEE binary64 floating point values in little-endian
         * byte order.
         */
        FLOAT64_LE(64, 8),

        /**
         * 64-bit IEEE binary64 floating point values in big-endian
         * byte order.
         */
        FLOAT64_BE(64, 8),

        /**
         * IEC 958 subframe, little endian.
         */
        IEC958_SUBFRAME_LE(Encoding.OTHER, Endian.LITTLE),

        /**
         * IEC 958 subframe, big endian.
         */
        IEC958_SUBFRAME_BE(Encoding.OTHER, Endian.BIG),

        /**
         * &#xb5;-law related format.
         * Corresponds to {@code SNDRV_PCM_FORMAT_MU_LAW}.
         * @see <a href="https://en.wikipedia.org/wiki/M-law_algorithm">&#xb5;-law
         * algorithm</a>
         */
        MU_LAW(Encoding.OTHER),

        /**
         * A-law related format.
         * Corresponds to {@code SNDRV_PCM_FORMAT_A_LAW}.
         * @see <a href="https://en.wikipedia.org/wiki/A-law_algorithm">A-law
         * algorithm</a>
         */
        A_LAW(Encoding.OTHER),

        /**
         * IMA ADPCM format.
         * Corresponds to {@code SNDRV_PCM_FORMAT_IMA_ADPCM}.
         * @see
         * <a href="https://wiki.multimedia.cx/index.php?title=IMA_ADPCM">
         * IMA ADPCM</a>
         */
        IMA_ADPCM(Encoding.OTHER),

        /**
         * MPEG-related format.
         * Corresponds to {@code SNDRV_PCM_FORMAT_MPEG}.
         */
        MPEG(Encoding.OTHER),

        /**
         * GSM-related format.
         * Corresponds to {@code SNDRV_PCM_FORMAT_GSM}.
         */
        GSM(Encoding.OTHER),

        /**
         * 20-bit signed integer values in little-endian byte order,
         * encoded in 4 bytes.
         */
        S20_LE(20, 4),

        /**
         * 20-bit signed integer values in big-endian byte order,
         * encoded in 4 bytes.
         */
        S20_BE(20, 4),

        /**
         * 20-bit unsigned integer values in little-endian byte order,
         * encoded in 4 bytes.
         */
        U20_LE(20, 4),

        /**
         * 20-bit unsigned integer values in big-endian byte order,
         * encoded in 4 bytes.
         */
        U20_BE(20, 4),

        /**
         * Unused place-holder value.
         */
        UNUSED_29(Encoding.OTHER),

        /**
         * Unused place-holder value.
         */
        UNUSED_30(Encoding.OTHER),

        /**
         * Placeholder for format.
         * Corresponds to {@code SNDRV_PCM_FORMAT_SPECIAL}.
         */
        SPECIAL(Encoding.OTHER),

        /**
         * 24-bit signed integer values in little-endian byte order,
         * encoded in 3 bytes.
         */
        S24_3LE(24, 3),

        /**
         * 24-bit signed integer values in big-endian byte order,
         * encoded in 3 bytes.
         */
        S24_3BE(24, 3),

        /**
         * 24-bit unsigned integer values in little-endian byte order,
         * encoded in 3 bytes.
         */
        U24_3LE(24, 3),

        /**
         * 24-bit unsigned integer values in big-endian byte order,
         * encoded in 3 bytes.
         */
        U24_3BE(24, 3),

        /**
         * 20-bit signed integer values in little-endian byte order,
         * encoded in 3 bytes.
         */
        S20_3LE(20, 3),

        /**
         * 20-bit signed integer values in big-endian byte order,
         * encoded in 3 bytes.
         */
        S20_3BE(20, 3),

        /**
         * 20-bit unsigned integer values in little-endian byte order,
         * encoded in 3 bytes.
         */
        U20_3LE(20, 3),

        /**
         * 20-bit unsigned integer values in big-endian byte order,
         * encoded in 3 bytes.
         */
        U20_3BE(20, 3),

        /**
         * 18-bit signed integer values in little-endian byte order,
         * encoded in 3 bytes.
         */
        S18_3LE(18, 3),

        /**
         * 18-bit signed integer values in big-endian byte order,
         * encoded in 3 bytes.
         */
        S18_3BE(18, 3),

        /**
         * 18-bit unsigned integer values in little-endian byte order,
         * encoded in 3 bytes.
         */
        U18_3LE(18, 3),

        /**
         * 18-bit unsigned integer values in big-endian byte order,
         * encoded in 3 bytes.
         */
        U18_3BE(18, 3),

        /**
         * G.723 encoding.
         * Corresponds to {@code SNDRV_PCM_FORMAT_G723_24}.
         * @see <a href="https://en.wikipedia.org/wiki/G.723">G.723</a>
         */
        G723_24(Encoding.OTHER),

        /**
         * G.723 encoding.
         * Corresponds to {@code SNDRV_PCM_FORMAT_G723_24_1B}.
         * @see <a href="https://en.wikipedia.org/wiki/G.723">G.723</a>
         */
        G723_24_1B(Encoding.OTHER),

        /**
         * G.723 encoding.
         * Corresponds to {@code SNDRV_PCM_FORMAT_G723_40}.
         * @see <a href="https://en.wikipedia.org/wiki/G.723">G.723</a>
         */
        G723_40(Encoding.OTHER),

        /**
         * G.723 encoding.
         * Corresponds to {@code SNDRV_PCM_FORMAT_G723_40_1B}.
         * @see <a href="https://en.wikipedia.org/wiki/G.723">G.723</a>
         */
        G723_40_1B(Encoding.OTHER),

        /**
         * Pulse-density modulation encoding.  (Not actually PCM.)
         * Corresponds to {@code SNDRV_PCM_FORMAT_DSD_U8}.
         * @see
         * <a href="https://en.wikipedia.org/wiki/Direct_Stream_Digital">Direct
         * Stream Digital</a>
         */
        DSD_U8(Encoding.OTHER),

        /**
         * Pulse-density modulation encoding.  (Not actually PCM.)
         * Corresponds to {@code SNDRV_PCM_FORMAT_DSD_U16_LE}.
         * @see
         * <a href="https://en.wikipedia.org/wiki/Direct_Stream_Digital">Direct
         * Stream Digital</a>
         */
        DSD_U16_LE(Encoding.OTHER, Endian.LITTLE),

        /**
         * Pulse-density modulation encoding.  (Not actually PCM.)
         * Corresponds to {@code SNDRV_PCM_FORMAT_DSD_U32_LE}.
         * @see
         * <a href="https://en.wikipedia.org/wiki/Direct_Stream_Digital">Direct
         * Stream Digital</a>
         */
        DSD_U32_LE(Encoding.OTHER, Endian.LITTLE),

        /**
         * Pulse-density modulation encoding.  (Not actually PCM.)
         * Corresponds to {@code SNDRV_PCM_FORMAT_DSD_U16_BE}.
         * @see
         * <a href="https://en.wikipedia.org/wiki/Direct_Stream_Digital">Direct
         * Stream Digital</a>
         */
        DSD_U16_BE(Encoding.OTHER, Endian.BIG),

        /**
         * Pulse-density modulation encoding.  (Not actually PCM.)
         * Corresponds to {@code SNDRV_PCM_FORMAT_DSD_U32_BE}.
         * @see
         * <a href="https://en.wikipedia.org/wiki/Direct_Stream_Digital">Direct
         * Stream Digital</a>
         */
        DSD_U32_BE(Encoding.OTHER, Endian.BIG),
        ;

        /**
         * Returns the endianness associated with this format.
         * @return the endian enum value (not null)
         */
        public Endian endian() {
            return endian;
        }

        /**
         * Returns the number of significant bits in a sample of this
         * format.  This does not include padding.
         * @return the number of bits; a non-negative integer (0 if
         *   not applicable to this format)
         */
        public int bits() {
            return bits;
        }

        /**
         * Returns the number of bytes in a sample of this
         * format.  This includes padding.
         * @return the number of bytes; a non-negative integer (0 if
         *   not applicable to this format)
         */
        public int bytes() {
            return bytes;
        }

        /**
         * Returns the rough type of encoding for this format.
         * @return the encoding type (not null)
         */
        public Encoding encoding() {
            return encoding;
        }

        private final Encoding encoding;
        private final int bits;
        private final int bytes;
        private final Endian endian;

        private Format(Encoding encoding) {
            this(encoding, Endian.NEITHER);
        }

        private Format(Encoding encoding, Endian endian) {
            this(encoding, 0, 0, endian);
        }

        private Format(Encoding encoding, int bits, int bytes, Endian endian) {
            this.encoding = encoding;
            this.bits = bits;
            this.bytes = bytes;
            this.endian = endian;
        }

        private Format(int bits, int bytes) {
            String name = name();
            boolean signed = name.startsWith("S");
            boolean unsigned = name.startsWith("U");
            boolean isFloat = name.startsWith("FLOAT");
            boolean big = name.endsWith("_BE") || name.endsWith("_3BE");
            boolean little = name.endsWith("_LE") || name.endsWith("_3LE");
            if (!((signed || unsigned || isFloat)
                    && (big || little)
                    && (isFloat || Character.isDigit(name.charAt(1)))))
                // Cannot recognize the properties.
                throw new InternalError(name);
            if (signed)
                this.encoding = Encoding.SIGNED_INTEGER;
            else if (unsigned)
                this.encoding = Encoding.UNSIGNED_INTEGER;
            else
                this.encoding = Encoding.FLOAT;
            this.bits = bits;
            this.bytes = bytes;
            if (big)
                this.endian = Endian.BIG;
            else
                this.endian = Endian.LITTLE;
        }
    }

    /**
     * Supported subformats.
     * @see Format
     */
    public enum Subformat {
        /**
         * The default (and currently only) subformat.
         * Corresponds to {@code SNDRV_PCM_SUBFORMAT_STD}.
         */
        STD,
        ;
    }

    /**
     * The device access protocol between kernel and userspace.
     */
    public enum Access {
        /**
         * This device supports interleaved @{code mmap}.
         * Corresponds to {@code SNDRV_PCM_ACCESS_MMAP_INTERLEAVED}.
         */
        MMAP_INTERLEAVED,
        /**
         * This device supports interleaved noninterleaved @{code mmap}.
         * Corresponds to {@code SNDRV_PCM_ACCESS_MMAP_NONINTERLEAVED}.
         */
        MMAP_NONINTERLEAVED,
        /**
         * This device supports complex @{code mmap} scenarios.
         * Corresponds to {@code SNDRV_PCM_ACCESS_MMAP_COMPLEX}.
         */
        MMAP_COMPLEX,

        /**
         * The devices supports interleaved reading and writing.
         * Corresponds to {@code SNDRV_PCM_ACCESS_RW_INTERLEAVED}.
         * @see PCM#writeInterleaved(ByteBuffer)
         */
        RW_INTERLEAVED,

        /**
         * The devices supports non-interleaved reading and writing.
         * Corresponds to {@code SNDRV_PCM_ACCESS_RW_NONINTERLEAVED}.
         */
        RW_NONINTERLEAVED,
        ;
    }

    /**
     * Hardware capability flags.
     * @see Params#flags()
     */
    public enum Flags {
        /**
         * The hardware does not support sample-rate changes.
         * Corresponds to {@code SNDRV_PCM_HW_PARAMS_NORESAMPLE}.
         */
        NORESAMPLE,

        /**
         * Placeholder value.  Currently unused.
         * Corresponds to {@code SNDRV_PCM_HW_PARAMS_EXPORT_BUFFER}.
         */
        EXPORT_BUFFER,

        /**
         * The hardware does not support per-period wakeups,
         * which means that it can be used in non-blocking mode only.
         * Corresponds to {@code SNDRV_PCM_HW_PARAMS_NO_PERIOD_WAKEUP}.
         */
        NO_PERIOD_WAKEUP,
        ;
    }

    /**
     * An interval of values.  The values are unsigned 32-bit
     * integers, represented as {@code long}.
     * @see Params#interval(IntervalIndex)
     * @see IntervalIndex
     */
    public final static class Interval {
        /**
         * Returns the lower bound of the interval.
         * @return an integer in the range from 0 to
         *   {@code 0xFFFFFFFF} (inclusive)
         * @see #max()
         */
        public long min() {
            return Integer.toUnsignedLong(buffer.get(ValueLayout.JAVA_INT, 0));
        }

        /**
         * Returns the upper bound of the interval.
         * @return an integer in the range from 0 to
         *   {@code 0xFFFFFFFF} (inclusive)
         * @see #min()
         */
        public long max() {
            return Integer.toUnsignedLong(buffer.get(ValueLayout.JAVA_INT, 4));
        }

        /**
         * Returns true if this interval is marked as open at the
         * minimum end.  Corresponds to the {@code openmin} bitfield
         * in {@code struct snd_interal}.
         * @return true if the lower bound is excluded from the interval,
         *   false if it is included in the interval
         */
        public boolean openMin() {
            return flag(1);
        }

        /**
         * Returns true if this interval is marked as open at the
         * maximum end.  Corresponds to the {@code openmax} bitfield
         * in {@code struct snd_interal}.
         * @return true if the upper bound is excluded from the interval,
         *   false if it is included in the interval
         */
        public boolean openMax() {
            return flag(2);
        }

        /**
         * Returns true if this interval is marked an integer.
         * Corresponds to the {@code integer} bitfield in
         * {@code struct snd_interal}.
         * @return true if the integer indicator is set, false otherwise
         */
        public boolean integer() {
            return flag(4);
        }

        /**
         * Returns true if this interval is marked as empty.
         * Corresponds to the {@code empty} bitfield in
         * {@code struct snd_interal}.
         * @return true if the empty indicator is set, false otherwise
         */
        public boolean empty() {
            return flag(8);
        }

        /**
         * Returns true if the value is contained in this interval.
         * @param value the value to test (generally a non-negative integer)
         * @return true if the value is in this interval, false if not
         */
        public boolean contains(long value) {
            if (value < 0)
                return false;
            if (empty())
                return false;
            if (value > min() && value < max())
                return true;
            if (value == min() && !openMin())
                return true;
            if (value == max() && !openMax())
                return true;
            return false;
        }

        /**
         * Sets the lower bound of the interval.
         * @param value the new minimum, in the range from 0 to
         *   {@code 0xFFFFFFFF} (inclusive)
         * @throws IllegalArgumentException if the value is not in the
         *   required range
         */
        public void min(long value) {
            if (value < 0 || Integer.toUnsignedLong((int) value) != value)
                throw new IllegalArgumentException("value: " + value);
            buffer.set(ValueLayout.JAVA_INT, 0, (int) value);
        }

        /**
         * Sets the upper bound of the interval.
         * @param value the new maximum, in the range from 0 to
         *   {@code 0xFFFFFFFF} (inclusive)
         * @throws IllegalArgumentException if the value is not in the
         *   required range
         */
        public void max(long value) {
            if (value < 0 || Integer.toUnsignedLong((int) value) != value)
                throw new IllegalArgumentException("value: " + value);
            buffer.set(ValueLayout.JAVA_INT, 4, (int) value);
        }

        /**
         * Sets both the lower and upper bound to the value.
         * @param value the new minimum and maximum, in the range from 0 to
         *   {@code 0xFFFFFFFF} (inclusive)
         * @throws IllegalArgumentException if the value is not in the
         *   required range
         */
        public void set(long value) {
            if (value < 0 || Integer.toUnsignedLong((int) value) != value)
                throw new IllegalArgumentException("value: " + value);
            buffer.set(ValueLayout.JAVA_INT, 0, (int) value);
            buffer.set(ValueLayout.JAVA_INT, 4, (int) value);
            buffer.set(ValueLayout.JAVA_INT, 8, 0);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (openMin())
                sb.append('(');
            else
                sb.append('[');
            sb.append(min());
            sb.append(", ");
            sb.append(max());
            if (openMax())
                sb.append(')');
            else
                sb.append(']');
            if (integer())
                sb.append("/integer");
            if (empty())
                sb.append("/empty");
            return sb.toString();
        }

        private final MemorySegment buffer;

        Interval(MemorySegment buffer) {
            this.buffer = buffer;
        }

        private boolean flag(int mask) {
            return (buffer.get(ValueLayout.JAVA_INT, 8) & mask) != 0;
        }
    }

    /**
     * Indices of intervals in {@linkplain Params PCM device parameters}.
     * @see Params#interval(IntervalIndex)
     * @see Interval
     */
    public enum IntervalIndex {
        /**
         * Bits per sample.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_SAMPLE_BITS}.
         */
        SAMPLE_BITS,

        /**
         * Bits per frame.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_FRAME_BITS}.
         */
        FRAME_BITS,

        /**
         * Channels in each frame.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_CHANNELS}.
         */
        CHANNELS,

        /**
         * Number of frames per second.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_RATE}.
         */
        RATE,

        /**
         * Approximate length of a period in microseconds.
         * The period is the time between two hardware interrupts.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_PERIOD_TIME}.
         */
        PERIOD_TIME,

        /**
         * Approximate length of a period, counted in frames.
         * The period is the time between two hardware interrupts.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_PERIOD_SIZE}.
         */
        PERIOD_SIZE,

        /**
         * Approximate length of a period, counted in bytes.
         * The period is the time between two hardware interrupts.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_PERIOD_BYTES}.
         */
        PERIOD_BYTES,

        /**
         * Approximate number of periods that fit into one buffer
         * sample data.
         * The period is the time between two hardware interrupts.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_PERIODS}.
         */
        PERIODS,

        /**
         * Approximate duration of the buffer, in microseconds.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_BUFFER_TIME}.
         */
        BUFFER_TIME,

        /**
         * Approximate duration of the buffer, counted in frames.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_BUFFER_SIZE}.
         */
        BUFFER_SIZE,

        /**
         * Approximate duration of the buffer, counted in bytes.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_BUFFER_BYTES}.
         */
        BUFFER_BYTES,

        /**
         * Approximate interval between hardware ticks, measured in
         * microseconds.
         * Corresponds to {@code SNDRV_PCM_HW_PARAM_TICK_TIME}.
         */
        TICK_TIME,
        ;
    }

    /**
     * PCM device parameters.
     *
     * Instances of this class are mutable and can be adjusted to find
     * the appropriate device configuration.
     *
     * @see PCM#refine(Params)
     */
    public final static class Params {
        /**
         * Creates default parameters with very intervals.  These
         * parameters need to be narrowed down using
         * {@link PCM#refine(Params)}.
         */
        public Params() {
            buffer = Arena.ofAuto().allocate(SIZE);

            // Initialize the first 64 bits of the three known mask fields.
            for (int i = 0; i < 3; ++i) {
                buffer.set(ValueLayout.JAVA_INT,
                    MASK_OFFSET + i * MASK_BYTES, -1);
                buffer.set(ValueLayout.JAVA_INT,
                    MASK_OFFSET + i * MASK_BYTES + 4, -1);
            }

            // Initialize the known interval fields.
            var intervals = buffer.asSlice(INTERVAL_OFFSET);
            for (int i = 0; i < 12; ++i)
                intervals.setAtIndex(ValueLayout.JAVA_INT, i * 3 + 1, -1);

            buffer.set(ValueLayout.JAVA_INT, RMASK_OFFSET, -1); // rmask
            buffer.set(ValueLayout.JAVA_INT, INFO_OFFSET, -1); // info
        }

        /**
         * Returns the set of flags.
         * Changing the returned bitset affects this object.
         * @return a shared bitset
         * @see Flags
         */
        public EnumBitSet<Flags> flags() {
            return EnumBitSet.wrap(Flags.class,
                buffer.asByteBuffer().order(ByteOrder.nativeOrder())
                .limit(4).asIntBuffer());
        }

        /**
         * Returns the set of access methods.
         * Changing the returned bitset affects this object.
         * @return a shared bitset
         * @see Access
         */
        public EnumBitSet<Access> access() {
            return bitset(Access.class, 0);
        }

        /**
         * Returns the set of formats.
         * Changing the returned bitset affects this object.
         * @return a shared bitset
         * @see Format
         */
        public EnumBitSet<Format> format() {
            return bitset(Format.class, 1);
        }

        /**
         * Returns the set of subformats.
         * Changing the returned bitset affects this object.
         * (Subformats are not currently used)
         * @return a shared bitset
         * @see Subformat
         */
        public EnumBitSet<Subformat> subformat() {
            return bitset(Subformat.class, 2);

        }

        /**
         * Returns the interval for the specified index.
         * Changing the returned interval affects this object.
         * @param index selects which interval should be returned
         * @return a shared interval
         */
        public Interval interval(IntervalIndex index) {
            return new Interval(buffer.asSlice(
                    INTERVAL_OFFSET + index.ordinal() * INTERVAL_BYTES));
        }

        /**
         * Returns the mask bits used for the refinement request.
         * This controls which parameters are changed (refined)
         * during {@linkplain #refine(Params) refinement}.
         * Corresponds to the {@code rmask} field in
         * {@code struct snd_pcm_hw_params}.
         * @return a bit mask as an integer
         * @see PCM#refine(Params)
         */
        public int requestedMask() {
            return buffer.get(ValueLayout.JAVA_INT, RMASK_OFFSET);
        }

        /**
         * Returns the changed mask.
         * Corresponds to the {@code cmask} field in
         * {@code struct snd_pcm_hw_params}.
         * @return an integer
         * @see PCM#refine(Params)
         */
        public int changedMask() {
            return buffer.get(ValueLayout.JAVA_INT, CMASK_OFFSET);
        }

        /**
         * Returns flags related to the refinement performed.
         * Corresponds to the {@code info} field in
         * {@code struct snd_pcm_hw_params}.
         * @return an integer
         * @see PCM#refine(Params)
         */
        public int info() {
            return buffer.get(ValueLayout.JAVA_INT, INFO_OFFSET);
        }

        /**
         * Returns the device-reported {@code msbits} value.  This is often
         * used with {@linkplain Format#S32_LE @{code S32}/{@code U32} formats}
         * to indicate the number of MSBs that are significant in samples.
         * However, some devices report 0 instead of 32 if all bits are significant.
         * @return an integer
         */
        public int msBits() {
            return buffer.get(ValueLayout.JAVA_INT, MSBITS_OFFSET);
        }

        /**
         * Returns the rate numerator.
         * Together with {@link #rateDenominator}, this describes the
         * sample as an exact rational number.
         * @return an integer
         * @see #rateDenominator()
         */
        public int rateNumerator() {
            return buffer.get(ValueLayout.JAVA_INT, RATE_NUM_OFFSET);
        }

        /**
         * Returns the rate denominator.
         * Together with {@link #rateNumerator}, this describes the
         * sample as an exact rational number.
         * @return an integer
         * @see #rateNumerator()
         */
        public int rateDenominator() {
            return buffer.get(ValueLayout.JAVA_INT, RATE_DENOM_OFFSET);
        }

        /**
         * Returns the chip FIFO size in frames.
         *
         * @return an {@code unsigned long} C value (which could be
         *   theoretically negative in the Java representation)
         */
        public long fifoSize() {
            return buffer.get(ValueLayout.JAVA_LONG, FIFO_SIZE_OFFSET);
        }

        private final MemorySegment buffer;


        private static final int MASK_OFFSET = 4;  // After the flags field.
        private static final int MASK_BYTES = 32; // 256 bit flags.
        private static final int MASK_COUNT = 8; // Only three defined so far.
        private static final int INTERVAL_OFFSET =
            MASK_OFFSET + MASK_BYTES * MASK_COUNT;
        private static final int INTERVAL_BYTES = 12;
        private static final int INTERVAL_COUNT = 21; // Only 12 defined.
        private static final int RMASK_OFFSET =
            INTERVAL_OFFSET + INTERVAL_BYTES * INTERVAL_COUNT;
        private static final int CMASK_OFFSET = RMASK_OFFSET + 4;
        private static final int INFO_OFFSET = RMASK_OFFSET + 8;
        private static final int MSBITS_OFFSET = RMASK_OFFSET + 12;
        private static final int RATE_NUM_OFFSET = RMASK_OFFSET + 16;
        private static final int RATE_DENOM_OFFSET = RMASK_OFFSET + 20;
        private static final int FIFO_SIZE_OFFSET = RMASK_OFFSET + 24;

        private static final int LONG_SIZE = (int) Linker.nativeLinker()
            .canonicalLayouts().get("long").byteSize();
        private static final int SIZE = FIFO_SIZE_OFFSET + LONG_SIZE + 64;

        private <E extends Enum<E>> EnumBitSet<E> bitset(
            Class<E> type, int index) {
            ByteBuffer b = buffer.asByteBuffer().order(ByteOrder.nativeOrder());
            int offset = MASK_OFFSET + index * MASK_BYTES;
            b.position(offset);
            b.limit(offset + MASK_BYTES);
            return EnumBitSet.wrap(type, b.asIntBuffer());
        }
    }

    /**
     * Updates the parameter argument according to hardware constraints.
     * This method asks the kernel to apply hardware constraints
     * based on the information in the argument, and update the argument
     * accordingly to reflect additional restrictions.
     *
     * <p>For example, specifying a fixed
     * {@linkplain IntervalIndex#BUFFER_SIZE buffer size} via
     * {@link Interval#set(long)} narrows down the interval of possible
     * {@linkplain IntervalIndex#PERIOD_SIZE period sizes} and the
     * {@linkplain IntervalIndex#PERIODS number of interrupts per buffer}.
     *
     * <p>Corresponds to the {@code SNDRV_PCM_IOCTL_HW_REFINE} ioctl.
     *
     * @param params the device parameter information, modified in place
     * @throws SyscallException if the kernel cannot find parameters
     *   that match the specified sets and intervals and are also
     *   supported by the hardware
     * @see #set(Params)
     */
    public void refine(Params params) {
        frameSize = 0;
        params.buffer.set(ValueLayout.JAVA_INT, Params.RMASK_OFFSET, -1);
        params.buffer.set(ValueLayout.JAVA_INT, Params.INFO_OFFSET, -1);
        Ioctl.ioctl(descriptor, Constants.SNDRV_PCM_IOCTL_HW_REFINE,
            params.buffer);
    }

    /**
     * Applies the hardware parameters.
     * @param params the parameters to apply
     * <p>Corresponds to the {@code SNDRV_PCM_IOCTL_HW_PARAMS} ioctl.
     * @see #refine(Params)
     */
    public void set(Params params) {
        frameSize = 0;
        params.buffer.set(ValueLayout.JAVA_INT, Params.RMASK_OFFSET, -1);
        params.buffer.set(ValueLayout.JAVA_INT, Params.INFO_OFFSET, -1);
        Ioctl.ioctl(descriptor, Constants.SNDRV_PCM_IOCTL_HW_PARAMS,
            params.buffer);

        // Update the frame size if known.
        Interval frameBitsInterval = params.interval(IntervalIndex.FRAME_BITS);
        long min = frameBitsInterval.min();
        if (min == frameBitsInterval.max() && !frameBitsInterval.empty()
            && min >= 0 && (min % 8) == 0 && min < Integer.MAX_VALUE)
            frameSize = (int) min / 8;
    }

    /**
     * Timestamp mode.
     * @see SoftwareParams#timestampMode()
     */
    public interface TimestampMode extends OpenEnum.OfInt {
        /**
         * The well-known enum values of the open enum type {@link TimestampMode}.
         */
        enum E implements TimestampMode, OpenEnum.OfInt.Direct {
            /**
             * Timestamps are disabled.
             */
            NONE,

            /**
             * Timestamps are updated in status reports.
             */
            ENABLE,
            ;
        }

        /**
         * The control object for this open enum.
         */
        OpenEnum.OfInt.Type<TimestampMode> T = OpenEnum.intType(
            MethodHandles.lookup(), TimestampMode.class, E.class);
    }

    /**
     * Available timestamp modes.
     *
     * Timestamps are used in status reports.
     * @see SoftwareParams#timestampType()
     */
    public interface TimestampType extends OpenEnum.OfInt {
        /**
         * The well-known enum values of the open enum type {@link TimestampType}.
         */
        enum E implements TimestampType, OpenEnum.OfInt.Direct {
            /**
             * For backwards compatibility.  Behavior is dependent on
             * hardware type.  Do not use.
             Corresponds to {@code SNDRV_PCM_AUDIO_TSTAMP_TYPE_COMPAT}.
            */
            COMPAT,

            /**
             * So-called DMA time.
             */
            DEFAULT,

            /**
             * Link time with a counter that is reset on start.
             */
            LINK,


            /**
             * Link time with a counter that is not reset on start.
             */
            LINK_ABSOLUTE,

            /**
             * Link time that is merely estimated.
             */
            LINK_ESTIMATED,

            /**
             * Link time is synchronized with the system clock.
             */
            LINK_SYNCHRONIZED,
            ;
        }

        /**
         * The control object for this open enum.
         */
        OpenEnum.OfInt.Type<TimestampType> T = OpenEnum.intType(
            MethodHandles.lookup(), TimestampType.class, E.class);
    }

    /**
     * PCM software parameters.
     * Corresponds to {@code struct snd_pcm_sw_params}.
     */
    public final static class SoftwareParams {
        /**
         * Creates new parameters initialized to default values.
         */
        public SoftwareParams() {
            buffer = Arena.ofAuto().allocate(SIZE);
            // Too rarely changed to expose.
            buffer.set(ValueLayout.JAVA_INT, STEP_OFFSET, 1);
            setPlatformLong(XFER_ALIGN_OFFSET, 1);
            startThreshold(1);
            version(0x2000e);
            timestampType(TimestampType.E.DEFAULT);
        }

        /**
         * Returns the timestamp mode.
         * @return the {@linkplain TimestampMode mode} if available,
         *   or the raw integer value in the error case
         * Corresponds to the {@code tstamp_mode} field in
         * {@code struct snd_pcm_sw_params}.
         */
        public TimestampMode timestampMode() {
            return TimestampMode.T.ofInt(buffer.get(ValueLayout.JAVA_INT, 0));
        }

        /**
         * Returns the minimum number of available frames before wakeup happens.
         * This corresponds to the {@code avail_min} member of
         * {@code struct snd_pcm_sw_params}.
         * @return a C {@code unsigned long} value interpreted as a Java {@code long}
         * @see #frameLimitForWakeup(long)
         */
        public long frameLimitForWakeup() {
            return buffer.get(ValueLayout.JAVA_LONG, AVAIL_MIN_OFFSET);
        }

        /**
         * Set the minimum number of available frames before wakeup happens.
         * The specified value can be adjust by the kernel.  Values
         * below the {@linkplain IntervalIndex#PERIOD_SIZE period size}
         * may be increased to the period size.  Actual wakeup granularity
         * depends on hardware support, too.
         * @param value a non-negative integer;
         *   it should be less than {@link Integer#MAX_VALUE} for maximum
         *   portability
         * @return this object
         * @see #frameLimitForWakeup()
         */
        public SoftwareParams frameLimitForWakeup(long value) {
            return setPlatformLong(AVAIL_MIN_OFFSET, value);
        }

        /**
         * Returns the amount of buffered frames at which playback
         * automatically starts.
         * Corresponds to the {@code start_threshold} member of
         * {@code struct snd_pcm_sw_params}.
         * @return a C {@code unsigned long} value interpreted as a Java {@code long}
         * @see #startThreshold(long)
         */
        public long startThreshold() {
            return buffer.get(ValueLayout.JAVA_LONG, START_THRESHOLD_OFFSET);
        }

        /**
         * Sets the amount of buffered frames at which playback
         * automatically starts.
         * @param value the frame count, should be less than
         *   {@link Integer#MAX_VALUE} for maximum *   portability
         * @return this object
         * @see #startThreshold
         */
        public SoftwareParams startThreshold(long value) {
            return setPlatformLong(START_THRESHOLD_OFFSET, value);
        }

        /**
         * Returns the number of frames at which playback
         * or capture stops.
         * <ul>
         * <li>For playback, the stream stops when the available space the
         * application may write without blocking reaches this number of frames.
         * <li>For capture, the stream stops when the number matches the frame
         * count that the application still needs to read.
         * </ul>
         *
         * Upon stop, an underrun or overrun is reported by the kernel
         * as an {@link Errno#EPIPE} error.  This is typically reported as an
         * {@link UnderflowException}.
         *
         * <p>Corresponds to the {@code stop_threshold} member of
         * {@code struct snd_pcm_sw_params}.
         * @return a C {@code unsigned long} value interpreted as a Java {@code long}
         * @see #stopThreshold(long)
         * @see #boundary()
         */
        public long stopThreshold() {
            return buffer.get(ValueLayout.JAVA_LONG, STOP_THRESHOLD_OFFSET);
        }

        /**
         * Sets the distance (in frames) from the buffer start at which
         * playback or capture is stopped.
         * @param value the frame count, should be less than
         *   {@link Integer#MAX_VALUE} for maximum *   portability
         * @return this object
         */
        public SoftwareParams stopThreshold(long value) {
            return setPlatformLong(STOP_THRESHOLD_OFFSET, value);
        }

        /**
         * Returns the threshold (in frames) at which silence is
         * written into the playback buffer.
         * Corresponds to the {@code silence_threshold} member of
         * {@code struct snd_pcm_sw_params}.
         * @return a C {@code unsigned long} value interpreted as a Java {@code long}
         * @see #silenceSize()
         */
        public long silenceThreshold() {
            return buffer.get(ValueLayout.JAVA_LONG, SILENCE_THRESHOLD_OFFSET);
        }

        /**
         * Returns the amount of silence (in frames) upon the threshold condition.
         * Corresponds to the {@code silence_size} member of
         * {@code struct snd_pcm_sw_params}.
         * @return a C {@code unsigned long} value interpreted as a Java {@code long}
         * @see #silenceThreshold()
         */
        public long silenceSize() {
            return buffer.get(ValueLayout.JAVA_LONG, SILENCE_SIZE_OFFSET);
        }

        /**
         * Returns a value used for kernel-internal ring buffer management.
         * This value is read-only for applications.
         * Corresponds to the {@code boundary} member of
         * {@code struct snd_pcm_sw_params}.
         * @return a C {@code unsigned long} value
         */
        public long boundary() {
            return buffer.get(ValueLayout.JAVA_LONG, BOUNDARY_OFFSET);
        }

        /**
         * Returns the value of the version field.
         * @return the verison, an integer like {@code 0x2000e}
         * @see #version(int)
         */
        public int version() {
            return buffer.get(ValueLayout.JAVA_INT, PROTO_OFFSET);
        }

        /**
         * Sets the version to specified value.
         * @param value the new version
         * @return this object
         * @see #version()
         */
        public SoftwareParams version(int value) {
            buffer.set(ValueLayout.JAVA_INT, PROTO_OFFSET, value);
            return this;
        }

        /**
         * Returns the meaning of the timestamp values.
         * @return a {@link TimestampType} enum constant if available,
         *   or the raw integer if not, in the error case.
         */
        public TimestampType timestampType() {
            return TimestampType.T.ofInt(buffer.get(ValueLayout.JAVA_INT, TSTAMP_TYPE_OFFSET));
        }

        /**
         * Configures the type of timestamp values in error reports.
         * @param value the new timetamp type
         * @return this object
         * @throws NullPointerException if {@code value} is null
         */
        public SoftwareParams timestampType(TimestampType value) {
            buffer.set(ValueLayout.JAVA_INT, TSTAMP_TYPE_OFFSET, value.value());
            return this;
        }

        private final MemorySegment buffer;

        private static final int STEP_OFFSET = 4;
        private static final int SLEEP_MIN_OFFSET = 8; // sleep_min is unsed
        // Potential padding here.
        private static final int AVAIL_MIN_OFFSET =
            SLEEP_MIN_OFFSET + Params.LONG_SIZE;
        private static final int XFER_ALIGN_OFFSET = // Obsolete.
            SLEEP_MIN_OFFSET + 2 * Params.LONG_SIZE;
        private static final int START_THRESHOLD_OFFSET =
            SLEEP_MIN_OFFSET + 3 * Params.LONG_SIZE;
        private static final int STOP_THRESHOLD_OFFSET =
            SLEEP_MIN_OFFSET + 4 * Params.LONG_SIZE;
        private static final int SILENCE_THRESHOLD_OFFSET =
            SLEEP_MIN_OFFSET + 5 * Params.LONG_SIZE;
        private static final int SILENCE_SIZE_OFFSET =
            SLEEP_MIN_OFFSET + 6 * Params.LONG_SIZE;
        private static final int BOUNDARY_OFFSET =
            SLEEP_MIN_OFFSET + 7 * Params.LONG_SIZE;
        private static final int PROTO_OFFSET =
            SLEEP_MIN_OFFSET + 8 * Params.LONG_SIZE;
        private static final int TSTAMP_TYPE_OFFSET = PROTO_OFFSET + 4;

        private static final int SIZE = TSTAMP_TYPE_OFFSET + 4 + 56;

        private SoftwareParams setPlatformLong(int offset, long value) {
            if (Params.LONG_SIZE == 4 && value < 0)
                throw new IllegalArgumentException("value: " + value);
            buffer.set(ValueLayout.JAVA_LONG, offset, value);
            return this;
        }
    }

    /**
     * Applies the software parameters.
     * @param params the parameters to apply
     */
    public synchronized void set(SoftwareParams params) {
        Ioctl.ioctl(descriptor, Constants.SNDRV_PCM_IOCTL_SW_PARAMS,
            params.buffer);
    }

    /**
     * Prepares the device for playing back or capturing samples.
     * Calling this method also enables submitting more data via
     * {@link #writeInterleaved(ByteBuffer)} after an
     * {@link UnderflowException} has occurred.
     * @throws SyscallException if the kernel reports an error
     */
    public synchronized void prepare() {
        Ioctl.ioctl(descriptor, Constants.SNDRV_PCM_IOCTL_PREPARE,
            MemorySegment.NULL);
    }

    /**
     * Submits samples in a buffer to the PCM device.
     * This call may block if the buffer is already free.
     * @param buffer the buffer containg sample data
     * @throws NullPointerException if {@code buffer} is null
     * @throws IllegalArgumentException if {@code buffer} is not a
     *   {@linkplain ByteBuffer#isDirect() direct buffer}
     * @throws IllegalStateException if the frame size has not been
     *   set via {@link #refine(Params)} and {@link #prepare()}
     * @throws UnderflowException if the buffer has become empty
     *   during playback (because new data is not submitted fast enough
     *   by the application)
     * @throws SyscallException if an error occurs during playback
     *   (for example, if the device is unplugged or switched off)
     * @see Access#RW_INTERLEAVED
     */
    public synchronized void writeInterleaved(ByteBuffer buffer) {
        if (!buffer.isDirect())
            throw new IllegalArgumentException("Direct buffer required");
        if (frameSize == 0)
            throw new IllegalStateException(
                "Frame size has not been determined");
        var buffer0 = MemorySegment.ofBuffer(buffer);
        int remaining = Math.toIntExact(buffer0.byteSize());
        int frameCount = remaining / frameSize;
        if ((remaining != frameCount * frameSize))
            throw new IllegalArgumentException(String.format(
                    "Remaining buffer size %d"
                    + " is not a multiple of frame size %d",
                    remaining, frameSize));

        try (var arena = Arena.ofConfined()) {
            var buf = arena.allocate(Struct.snd_xferi);
            Struct.snd_xferi$buf.set(buf, 0L, buffer0);
            Struct.snd_xferi$frames.set(buf, 0L, frameCount);
            try {
                Ioctl.ioctl(
                    descriptor, Constants.SNDRV_PCM_IOCTL_WRITEI_FRAMES, buf);
            } catch (SyscallException e) {
                if (e.errno() == Errno.EPIPE)
                    throw new UnderflowException();
                throw e;
            }
            long ret = (long) Struct.snd_xferi$result.get(buf, 0L);
            if (ret < 0 || ret > Integer.MAX_VALUE)
                throw new InternalError("enyo.alsa.Descriptor.pcmWriteI");
            buffer.position(Math.addExact(buffer.position(),
                    Math.multiplyExact((int) ret, frameSize)));
        }
    }

    /**
     * Blocks until the currently queued samples have been drained.
     * @throws SyscallException if an ALSA-related error occurred
     */
    public synchronized void drain() {
        Ioctl.ioctl(descriptor, Constants.SNDRV_PCM_IOCTL_DRAIN,
            MemorySegment.NULL);
    }

    private final Device device;
    private final Descriptor descriptor;
    private int frameSize; // 0 means unavailable.

    private static final class Struct {
        static final StructLayout snd_xferi;
        static {
            var linker = Linker.nativeLinker();
            var c_long = linker.canonicalLayouts().get("long");
            snd_xferi = MemoryLayout.structLayout(
                c_long.withName("result"),
                ValueLayout.ADDRESS.withName("buf"),
                c_long.withName("frames"));
        }
        static final VarHandle snd_xferi$result =
            snd_xferi.varHandle(groupElement("result"));
        static final VarHandle snd_xferi$buf =
            snd_xferi.varHandle(groupElement("buf"));
        static final VarHandle snd_xferi$frames =
            snd_xferi.varHandle(groupElement("frames"));
    }
}
