package enyo.util.argparse.tests;

import enyo.lang.RelaunchVM;
import enyo.io.ProcessOutput;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Arrays;
import java.util.List;

public final class TestRun {
    @Test
    public void test(Testing t) throws Exception {
        check(t, new String[] {"from-file", "to-file"}, 0,
            """
            progress: false
            from: from-file
            to: to-file
            """); // ");
        check(t, new String[] {"--progress", "from-file", "to-file"}, 0,
            """
            progress: true
            from: from-file
            to: to-file
            """); // ");
        check(t, new String[] {"from-file", "to-file", "-p"}, 0,
            """
            progress: true
            from: from-file
            to: to-file
            """); // ");
        check(t, new String[] {}, 1,
            """
            error: argument from is missing
            error: argument to is missing
            """); // ");

        check(t, new String[] {"--not-known"}, 1,
            "error: unknown option: --not-known\n");
        check(t, new String[] {"first", "second", "third"}, 1,
            "error: unrecognized argument: \"third\"\012");

        String help = """
usage: enyo.tests/enyo.util.argparse.tests.MainCopy [options...] from to

Options:

  --progress, -p
    display a progress bar

Arguments:

  from
    path to the source file
  to
    path to the destination file
"""; // ";
        check(t, new String[] {"--help"}, 0, help);
    }

    private static void check(Testing t, String[] args, int status,
        String expected) throws Exception {
        String context = Arrays.asList(args).toString();
        var pb = RelaunchVM.processBuilder(List.of(),
            MainCopy.class, List.of(args));
        var queue = new LinkedBlockingQueue<ProcessOutput.Data>();
        int ends = 0;
        var stream = (status == 0 ? ProcessOutput.Source.OUTPUT
            : ProcessOutput.Source.ERROR);
        var out = new ByteArrayOutputStream();
        try (var proc = pb.start();
            @SuppressWarnings("try")
            var data = new ProcessOutput(proc, queue)) {
            while (true) {
                var e = queue.take();
                if (e.isLastForStream())
                    ++ends;
                if (e.data() != null) {
                    t.examine(e)
                        .context(context)
                        .map(ProcessOutput.Data::source)
                        .is(stream);
                    out.write(e.data());
                }
                if (ends == 2)
                    break;
            }
            t.examine(proc.waitFor()).is(status);
            t.examine(new String(out.toByteArray())).is(expected);
        }
    }
}
