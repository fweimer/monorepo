package enyo.io.terminfo.tests;

import enyo.io.TempDirectory;
import enyo.io.function.IOConsumer;
import enyo.io.terminfo.BooleanCapability;
import enyo.io.terminfo.NumericCapability;
import enyo.io.terminfo.StringCapability;
import enyo.io.terminfo.TermFile;
import enyo.lang.RelaunchVM;
import enyo.testing.Test;
import enyo.testing.TestFailure;
import enyo.testing.Testing;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

public final class TestTermFile {
    @Test
    public static void testCurrent() {
        TermFile term = TermFile.current();
        for (BooleanCapability c : BooleanCapability.values())
            term.get(c);
        for (NumericCapability c : NumericCapability.values())
            term.get(c);
        for (StringCapability c : StringCapability.values())
            term.get(c);
    }

    @Test
    public static void testTerminfoTerminals(Testing t) throws IOException {
        terminfoFiles(t).forEach(IOConsumer.of(path -> {
                        byte[] blob = Files.readAllBytes(path);
                        try {
                            TermFile.of(blob);
                        } catch (Throwable e) {
                            throw new TestFailure(path.toString(), e);
                        }
                }));
    }

    @Test
    public static void testCurrentHomeDirectory(Testing t) throws Exception {
        try (TempDirectory temporaryHomeDirectory = TempDirectory.withPrefix(
                "TestTermFile-")) {
            Path terminfo = temporaryHomeDirectory.resolve(".terminfo");
            Files.createDirectory(terminfo);
            Path subdirectory = terminfo.resolve("T");
            Files.createDirectory(subdirectory);
            Path file = subdirectory.resolve("TestTermFileTerminal");
            Files.write(file, terminalBlob(), StandardOpenOption.CREATE);
            ProcessBuilder builder = RelaunchVM.processBuilder(
                List.of("-Duser.home=" + temporaryHomeDirectory),
                TestCurrentHomeDirectory.class, List.of());
            builder.environment()
                .put("TERM", "TestTermFileTerminal");
            builder.inheritIO();
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = builder.start();
            try {
                t.examine(process.waitFor()).is(0);
            } finally {
                process.destroy();
            }
        }
    }

    @Test
    public void testOf(Testing t) {
        TermFile empty = TermFile.of();
        for (BooleanCapability c : BooleanCapability.values())
            t.examine(c.toString()).testNot(String::isEmpty);
        for (NumericCapability c : NumericCapability.values())
            t.examine(empty.get(c))
                .context(c.toString())
                .is(-1);
        for (StringCapability c : StringCapability.values())
            t.examine(empty.get(c))
                .context(c.toString())
                .isNull();
    }

    @Test
    public static void testOfByteArray() {
        checkTestTerminal(TermFile.of(terminalBlob()));
    }

    @Test
    public void testIsTerminal(Testing t)
        throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("tty");
        pb.environment().clear();
        pb.redirectErrorStream();
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process proc = pb.start();
        byte[] outputBytes = proc.getInputStream().readAllBytes();
        int status = proc.waitFor();
        if (status != 0)
            t.examine(status).is(1);
        String output = new String(outputBytes, StandardCharsets.US_ASCII)
            .trim();
        if (status != 0)
            t.examine("not a tty").is(output);

        t.examine(TermFile.isTerminal(FileDescriptor.in)).is(status == 0);
    }

    /**
     * Helper class for {@link TestTermFile#testCurrentHomeDirectory()}.
     */
    public static class TestCurrentHomeDirectory {
        /**
         * Asserts that the current terminal is the test terminal
         *
         * @param args ignored
         */
        public static void main(String[] args) throws IOException {
            checkTestTerminal(TermFile.current());
        }
    }

    static Stream<Path> terminfoFiles(Testing t) throws IOException {
        Path terminfo = Path.of("/usr/share/terminfo");
        if (!Files.isDirectory(terminfo)) {
            t.unsupported("System terminfo directory does not exist");
            return Stream.of();
        } else
            return Files.walk(terminfo)
                .filter(
                    path -> Files.isRegularFile(
                        path, LinkOption.NOFOLLOW_LINKS));
    }

    static byte[] terminalBlob() {
        return Base64.getDecoder().decode(
            "GgEwAAIAAQCCAAgAVGVzdFRlcm1GaWxlVGVybWluYWx8ZW55byBsaWJyYXJ5"
            + "IHRlc3QgdGVybWluYWwAAAFQAP//AAACAP////////////////////8EAP//"
            + "////////////////////////////////////////////////////////////"
            + "////////////////////////////////////////////////////////////"
            + "////////////////////////////////////////////////////////////"
            + "////////////////////////////////////////////////////////////"
            + "////////////////////////////////////////////////////////////"
            + "/////////wYABwANAAoACgA=");
    }

    private static void checkTestTerminal(TermFile term) {
        // Cannot use Testing here because this is called from
        // TestCurrentHomeDirectory#main(String[])
        if (!term.names().equals(
                List.of("TestTermFileTerminal",
                    "enyo library test terminal"))) {
                throw new TestFailure(term.names().toString());
        }
        for (BooleanCapability cap : BooleanCapability.values())
            if (term.get(cap) != (cap == BooleanCapability.AUTO_RIGHT_MARGIN))
                throw new TestFailure(cap.toString());
    }
}
