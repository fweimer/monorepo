package enyo.io.terminfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * The state of the terminal.
 * This keeps track of the state of the terminal
 * across various terminal script executions.
 */
public final class TerminalState {
    /**
     * Creates a new terminal state for the specified writer.
     * @param writer output is sent to this writer
     */
    public TerminalState(TerminalWriter writer) {
        this.writer = Objects.requireNonNull(writer);
    }

    /**
     * Executes the specified terminal script.
     * @param script the terminal script to run, typically from a
     *   {@linkplain StringCapability string capability}
     * @throws NullPointerException if {@code script} is null
     * @throws TerminalScriptException if {@code script} is malformed
     * @throws UncheckedIOException if an output error occurs
     */
    public void runScript(byte[] script) {
        Arrays.fill(intParameters, 0);
        Arrays.fill(stringParameters, null);
        parser.accept(visitor, script);
    }

    /**
     * Executes the specified terminal script.
     * @param script the terminal script to run, typically from a
     *   {@linkplain StringCapability string capability}
     * @param argument the string argument to pass to the script
     * @throws NullPointerException if {@code script} is null
     * @throws TerminalScriptException if {@code script} is malformed
     * @throws UncheckedIOException if an output error occurs
     */
    public void runScript(byte[] script, byte[] argument) {
        Arrays.fill(intParameters, 0);
        stringParameters[0] = argument;
        Arrays.fill(stringParameters, 1, stringParameters.length, null);
        parser.accept(visitor, script);
        stringParameters[0] = null;
    }

    /**
     * Executes the specified terminal script, with arguments.
     * @param script the terminal script to run, typically from a
     *   {@linkplain StringCapability string capability}
     * @param args arguments to pass to the script
     * @throws IllegalArgumentException if {@code args} has more
     *   than 9 elements
     * @throws NullPointerException if any argument is null
     * @throws TerminalScriptException if {@code script} is malformed
     * @throws UncheckedIOException if an output error occurs
     */
    public void runScript(byte[] script, int... args) {
        if (args.length > 9)
            throw new IllegalArgumentException("args: " + args.length);
        System.arraycopy(args, 0, intParameters, 0, args.length);
        Arrays.fill(intParameters, args.length, intParameters.length, 0);
        Arrays.fill(stringParameters, null);
        parser.accept(visitor, script);
    }

    private final TerminalWriter writer;
    private final TerminalStack stack = new TerminalStack();
    private final ScriptParser parser = new ScriptParser();
    private final Visitor visitor = new Visitor();
    private final IfVisitor ifVisitor = new IfVisitor();
    private final int[] intParameters = new int[9];
    private final byte[][] stringParameters = new byte[9][];
    private int[] variables;

    // This is a stack of the outcome of the IfVisitor checks.  If
    // ifResults[ifLevel] is true, all further processing needs to be
    // skipped.
    private boolean[] ifResults;
    private int ifLevel = -1;

    private static int variableIndex(int var) {
        if ('A' <= var && var <= 'Z')
            return var - 'A';
        else if ('a' <= var && var <= 'z')
            return 26 + var - 'a';
        else
            throw new IllegalArgumentException("Variable reference: " + var);
    }

    private void setVariable(int var, int value) {
        int index = variableIndex(var);
        if (variables == null)
            variables = new int[index + 1];
        else if (variables.length <= index)
            variables = Arrays.copyOf(variables, index + 1);
        variables[index] = value;
    }

    private int getVariable(int var) {
        if (variables == null)
            return 0;
        int index = variableIndex(var);
        if (index < variables.length)
            return variables[index];
        else
            return 0;
    }

    private void write(int b) {
        writer.write(b);
    }

    private void write(byte[] string) {
        for (byte b : string)
            write(b);
    }

    private void pad(byte b, int repeat) {
        for (int i = 0; i < repeat; ++i)
            write(b);
    }

    private final class Visitor implements ScriptVisitor {
        @Override
        public void visit(UnaryOp op) {
            switch (op) {
            case NEGATE:
                if (stack.popInt() != 0)
                    stack.pushInt(0);
                else
                    stack.pushInt(1);
                break;
            case BIT_COMPLEMENT:
                stack.pushInt(~stack.popInt());
                break;
            case STRING_LENGTH:
                stack.pushInt(stack.popString().length);
            }
        }

        @Override
        public void visit(BinaryOp op) {
            int right = stack.popInt();
            int left = stack.popInt();
            stack.pushInt(op.apply(left, right));
        }

        @Override
        public void visit(Format op) {
            if (op.op() == FormatOp.CHAR)
                write(stack.popInt());
            else if (op.op() == FormatOp.STRING) {
                byte[] str = stack.popString();
                if (op.precision() >= 0
                    && str.length > op.precision())
                    str = Arrays.copyOf(str, op.precision());
                int pad = Math.max(op.width() - str.length, 0);
                boolean padRight = op.flags().contains(FormatFlag.MINUS);
                if (!padRight)
                    pad((byte) ' ', pad);
                write(str);
                if (padRight)
                    pad((byte) ' ', pad);
            } else {
                int value = stack.popInt();
                if (op.flags().isEmpty()
                    && op.width() < 0 && op.precision() < 0) {
                    if (value >= 0 && value < 8) {
                        // All number formats agree here.
                        write('0' + value);
                        return;
                    }
                    if (value >= 0 && value < 100
                        && op.op() == FormatOp.DECIMAL) {
                        // Common special case of a small decimal number.
                        write('0' + (value / 10));
                        write('0' + (value % 10));
                        return;
                    }
                }
                String formatted = op.format(value);
                write(formatted.getBytes(StandardCharsets.ISO_8859_1));
            }
        }

        @Override
        public void visitIncrement() {
            ++intParameters[0];
            ++intParameters[1];
        }

        @Override
        public void visitPushParameter(int index) {
            --index;
            if (stringParameters[index] != null)
                stack.pushString(stringParameters[index]);
            else
                stack.pushInt(intParameters[index]);
        }

        @Override
        public void visitPushVariable(int var) {
            stack.pushInt(getVariable(var));
        }

        @Override
        public void visitSetVariable(int var) {
            int value = stack.popInt();
            setVariable(var, value);
        }

        @Override
        public void visitPushConstant(int value) {
            stack.pushInt(value);
        }

        @Override
        public void visit(byte value) {
            write(value);
        }

        @Override
        public ScriptVisitor.IfVisitor visitIf() {
            return ifVisitor;
        }
    }

    private final class IfVisitor implements ScriptVisitor.IfVisitor {
        @Override
        public ScriptVisitor visitIf() {
            ++ifLevel;
            if (ifResults == null)
                ifResults = new boolean[1];
            else if (ifLevel >= ifResults.length)
                ifResults = Arrays.copyOf(ifResults, ifLevel + 1);
            ifResults[ifLevel] = false;
            return visitor;
        }

        @Override
        public ScriptVisitor visitThen() {
            if (ifResults[ifLevel])
                // Previous true branch has already been taken.
                return null;
            boolean result = stack.popInt() != 0;
            if (result) {
                ifResults[ifLevel] = true;
                return visitor;
            } else
                return null;
        }

        @Override
        public ScriptVisitor visitElse() {
            if (ifResults[ifLevel])
                return null;
            else
                return visitor;
        }

        @Override
        public void visitEnd() {
            --ifLevel;
        }
    }
}
