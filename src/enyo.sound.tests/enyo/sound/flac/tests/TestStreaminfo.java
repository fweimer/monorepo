package enyo.sound.flac.tests;

import enyo.io.BufferInput;
import enyo.sound.flac.Streaminfo;

import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.IOException;
import java.io.ByteArrayInputStream;

public final class TestStreaminfo {
    @Test
    public static void testBasic(Testing t) throws IOException {
        byte[] header = {
            16, 0, 16, 0, 0, 2, -22, 0, 27, 124, 10, -60, 66, -16, 0, -77,
            35, 104, 101, 102, 103, 104, 105, 106, 107, 108, 109,
            110, 111, 112, 113, 114, 115, 116
        };
        t.examine(header.length).is(34);
        var in = BufferInput.wrap(header);
        Streaminfo info = new Streaminfo(in);
        t.examine(in.fillBuffer(0).remaining()).is(0);

        t.examine(info.minimumBlockSize()).is(4096);
        t.examine(info.maximumBlockSize()).is(4096);
        t.examine(info.minimumFrameSize()).is(5968 / 8);
        t.examine(info.maximumFrameSize()).is(56288 / 8);
        t.examine(info.sampleRate()).is(44100);
        t.examine(info.channels()).is(2);
        t.examine(info.bitsPerSample()).is(16);
        t.examine(info.totalSamples()).is(11740008);
        t.examine(info.digest()).check(
            Checks.byteArrayEquals(new byte[] {
                    101, 102, 103, 104, 105, 106, 107, 108, 109,
                    110, 111, 112, 113, 114, 115, 116
                }));
    }
}
