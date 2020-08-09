package enyo.lang.tests;

import enyo.lang.RelaunchVM;
import enyo.lang.SystemExit;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.List;

public final class TestSystemExit {
    @Test
    public static void test0(Testing t) throws Exception {
        testStatus(t, 0);
    }

    @Test
    public static void test1(Testing t) throws Exception  {
        testStatus(t, 1);
    }

    @Test
    public static void test17(Testing t) throws Exception  {
        testStatus(t, 17);
    }

    private static void testStatus(Testing t, int expectedStatus)
        throws Exception {
        ProcessBuilder builder = RelaunchVM.processBuilder(
            TestSystemExit.class,
            List.of(Integer.toString(expectedStatus)));
        builder.redirectErrorStream(true);
        Process process = builder.start();
        int actual;
        try {
            System.out.write(process.getInputStream().readAllBytes());
            actual = process.waitFor();
        } finally {
            process.destroy();
        }
        t.examine(actual)
            .is(expectedStatus);
    }

    /**
     * Entry point for the test cases.
     * @param args contains the requested exit status
     */
    public static void main(String[] args) {
        SystemExit.run(() -> {
                int status = Integer.parseInt(args[0]);
                throw new SystemExit(status);
            });
    }
}
