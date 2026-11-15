package enyo.sound.flac.tests;

import enyo.io.BufferInput;
import enyo.sound.flac.BitInput;
import enyo.sound.flac.FlacInput;
import enyo.sound.flac.FlacException;
import enyo.sound.flac.FrameHeader;
import enyo.sound.flac.LPCParameters;
import enyo.sound.flac.MetadataInput;
import enyo.sound.flac.Residual;
import enyo.sound.flac.Streaminfo;
import enyo.sound.flac.SubframeHeader;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public final class DumpFile {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.format("usage: %s PATH%n", DumpFile.class);
            System.exit(1);
        }

        try (var in0 = FileChannel.open(Path.of(args[0]));
            var in1 = BufferInput.wrap(in0);
            var in = new FlacInput(in1)) {
            dumpStreaminfo(in.streaminfo());
            while (true) {
                MetadataInput stream = in.nextMetadata();
                if (stream == null)
                    break;
                System.out.format("Metadata: %s (%d bytes)%n",
                    stream.type(), stream.length());
            }
            System.out.println();

            var bits = new BitInput(in1);
            while (true) {
                FrameHeader hdr;
                try {
                    hdr = new FrameHeader(bits, in.streaminfo());
                } catch (EOFException e) {
                    break; // FIXME: check for file truncation
                }
                dumpFrameHeader(hdr);
                processFrame(bits, hdr);
            }
        }
    }

    private static void dumpStreaminfo(Streaminfo info) {
        if (info.minimumBlockSize() == info.maximumBlockSize())
            System.out.format("block size: %d samples%n",
                info.minimumBlockSize());
        else {
            System.out.format("minimum block size: %d samples%n",
                info.minimumBlockSize());
            System.out.format("maximum block size: %d samples%n",
                info.maximumBlockSize());
        }

        if (info.minimumFrameSize() != 0)
            System.out.format("minimum frame size: %d bytes%n",
                info.minimumFrameSize());
        if (info.maximumFrameSize() != 0)
            System.out.format("maximum frame size: %d bytes%n",
                info.maximumFrameSize());

        System.out.format("sample rate: %d Hz%n", info.sampleRate());
        System.out.format("channels: %d%n", info.channels());
        System.out.format("bits per sample: %d bits%n", info.bitsPerSample());
        System.out.format("file length: %d samples%n", info.totalSamples());

        System.out.print("digest: ");
        for (byte b : info.digest())
            System.out.format("%02x", Byte.toUnsignedInt(b));
        System.out.println();
    }

    private static void dumpFrameHeader(FrameHeader frameHeader)
        throws IOException {
        if (!frameHeader.variableBlockSize())
            System.out.format("Frame number %d:%n",
                frameHeader.sampleOrFrameNumber());
        else
            System.out.println("Frame:");
        System.out.format("  variable sized blocks: %s%n",
            frameHeader.variableBlockSize());
        System.out.format("  block size: %d samples%n",
            frameHeader.blockSize());
        System.out.format("  sample rate: %d Hz%n", frameHeader.sampleRate());
        System.out.format("  channel assignment: 0x%x%n",
            frameHeader.channelAssignment());
        System.out.format("  bits per sample: %d bits%n",
            frameHeader.bitsPerSample());
        if (frameHeader.variableBlockSize())
            System.out.format("  sample number: %d%n",
                frameHeader.sampleOrFrameNumber());
    }

    private static void dumpSubframeHeader(
        FrameHeader hdr, SubframeHeader shdr) {
        System.out.format("  Subframe type: %s%n", shdr.type());
        if (shdr.type().hasOrder())
            System.out.format("    order: %d%n", shdr.order());
        if (shdr.sampleShift() != 0)
            System.out.format("    sample left shift: %d%n",
                shdr.sampleShift());
        System.out.format("    bits per sample: %d%n",
            shdr.bitsPerSample(hdr));
    }

    private static void processFrame(BitInput bits, FrameHeader hdr)
        throws IOException {
        int channelCount = hdr.channelAssignment();
        if (channelCount <= 7)
            ++channelCount;
        else if (channelCount >= 8 && channelCount <= 10)
            channelCount = 2;
        else
            throw new FlacException("channelAssignment: " + channelCount);

        for (int channel = 0; channel < channelCount; ++channel) {
            SubframeHeader shdr = new SubframeHeader(channel, bits);
            dumpSubframeHeader(hdr, shdr);

            int bitsPerSample = shdr.bitsPerSample(hdr);
            switch (shdr.type()) {
            case CONSTANT:
                // One sample.
                bits.getBits(bitsPerSample);
                break;
            case VERBATIM:
                for (int i = 0; i < hdr.blockSize(); ++i)
                    bits.getBits(bitsPerSample);
                break;
            case FIXED:
            case LPC:
                for (int i = 0; i < shdr.order(); ++i)
                    System.out.format("      warmup %d: %d%n",
                        i, bits.getSignedBits(bitsPerSample));

                if (shdr.type().equals(SubframeHeader.Type.LPC)) {
                    LPCParameters lpc = new LPCParameters(bits);
                    System.out.format("    LPC coefficient precision: %d%n",
                        lpc.precision());
                    System.out.format("    LPC coefficient shift: %d%n",
                        lpc.shift());
                    for (int i = 0; i < shdr.order(); ++i)
                        System.out.format("      coefficient %d: %d%n",
                            i, lpc.readCoefficient(bits));
                }

                Residual res = new Residual(bits);
                if (res.order() > 0)
                    System.out.format(
                        "    residual (type %s, order %d, %d partitions):%n",
                        res.type(), res.order(), res.partitions());
                else
                    System.out.format(
                        "    residual (type %s, order 0, 1 partition)%n",
                        res.type());
                for (int i = 0; i < res.partitions(); ++i) {
                    int count = res.partitionLength(
                        hdr, shdr, i);
                    System.out.format("        partition length: %d%n",
                        count);
                    var param = new Residual.Parameter(res, bits);
                    System.out.format("          parameter: %d%n",
                        param.parameter());
                    System.out.format("          unencoded: %s%n",
                        param.isUnencoded());
                    if (param.isUnencoded())
                        for (int j = 0; j < count; ++j)
                            bits.getBits(param.parameter());
                    else
                        for (int j = 0; j < count; ++j)
                            bits.getRice(param.parameter());
                }
            }
        }
        bits.getFooter(); // The CRC.
    }
}
