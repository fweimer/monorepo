package enyo.io.terminfo.tests;

import enyo.io.terminfo.TerminalOutput;
import enyo.io.terminfo.TerminalWriter;
import enyo.io.terminfo.TerminalState;
import enyo.io.terminfo.StringCapability;

public final class Tput {
    public static void main(String[] args) {
        TerminalOutput tout = new TerminalOutput();
        TerminalState tstate = new TerminalState(
            new TerminalWriter(tout, tout.terminal()));

        StringCapability cap = StringCapability.valueOf(
            args[0].toUpperCase());
        byte[] capBlob = tout.terminal().get(cap);
        if (capBlob == null) {
            System.err.format("error: capability %s not supported%n", cap);
            System.exit(1);
        }
        int[] intArgs = new int[args.length - 1];
        for (int i = 0; i < intArgs.length; ++i)
            intArgs[i] = Integer.parseInt(args[1 + i]);
        tstate.runScript(capBlob, intArgs);
    }
}
