package enyo.util.tests;

import enyo.util.StringFilter;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class TestStringFilter {
    @Test
    public static void test1(Testing t) {
        String stringA = "a";
        String stringAB = "ab";
        String stringABC = "ac";
        StringFilter si = StringFilter.of(stringA, stringAB, stringABC);
        checkFound(t, si, stringA);
        checkFound(t, si, stringAB);
        checkFound(t, si, stringABC);
    }

    private static void checkFound(Testing t, StringFilter si, String ref) {
        t.assertSame(ref, si.lookup(ref));
        t.assertSame(ref, si.lookup(new String(ref)));
        t.assertTrue(si.contains(ref));
        t.assertTrue(si.contains(new String(ref)));

        byte[] bytes = ref.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        t.assertSame(ref, si.lookup(buffer, 0, bytes.length));
        t.assertTrue(si.contains(buffer, 0, bytes.length));

        buffer = ByteBuffer.wrap(Arrays.copyOf(bytes, bytes.length + 1));
        t.assertSame(ref, si.lookup(buffer, 0, bytes.length));
        t.assertTrue(si.contains(buffer, 0, bytes.length));

        byte[] padded = new byte[3 + bytes.length + 5];
        Arrays.fill(padded, (byte) 1);
        System.arraycopy(bytes, 0, padded, 3, bytes.length);
        buffer = ByteBuffer.wrap(padded);
        t.assertSame(ref, si.lookup(buffer, 3, 3 + bytes.length));
        t.assertTrue(si.contains(buffer, 3, 3 + bytes.length));
    }
}
