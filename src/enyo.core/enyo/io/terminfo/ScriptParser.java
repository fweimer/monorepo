package enyo.io.terminfo;

import java.util.Objects;

/**
 * Parsing of terminal scripts for visiting them.
 *
 * Terminal scripts are {@linkplain StringCapability string capabilities}
 * which contain embedded {@code %} directives.  The parser recognizes
 * them in the {@link #accept(ScriptVisitor,byte[])} and
 * {@link #accept(ScriptVisitor,byte[],int,int)} methods and invokes
 * the appropriate {@link ScriptVisitor} methods.
 *
 * <p>Parsing of <code>$&lt;&#x2026;&gt;</code> directives is handled by
 * the {@link TerminalWriter} class.
 *
 * @see ScriptVisitor
 * @see <a href="https://manpages.debian.org/terminfo.5">terminfo(5)
 *   manual page</a>
 */
public final class ScriptParser {
    /**
     * Creates a new terminal script parser.
     */
    public ScriptParser() {
    }

    /**
     * Returns the position of the construct that was visited
     * last.  This is the construct that corresponds to the {@link
     * ScriptVisitor} or {@link ScriptVisitor.IfVisitor} visitor method being
     * called.
     * @return the array index of the currently parsed construct
     *   in the script
     */
    public int lastOffset() {
        return offset;
    }

    /**
     * Parsers a terminal script.
     * Equivalent to {@code accept(visitor, script, 0, script.length)}.
     * @param visitor the visitor to invoke
     * @param script the terminal script to parse
     */
    public void accept(ScriptVisitor visitor, byte[] script) {
        accept(visitor, script, 0, script.length);
    }

    /**
     * Parsers a terminal script.
     *
     * @param visitor the visitor to invoke
     * @param script the terminal script to parse
     * @param fromIndex index of the first byte
     *   in {@code script} to process
     * @param toIndex one plus the index of the last byte
     *   in {@code script} to process
     */
    public void accept(ScriptVisitor visitor,
        byte[] script, int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, script.length);

        // Try a limited parser first, to deal with scripts that are
        // not actually scripts and contain unquoted %+ and %!.
        fromIndex = preAccept(visitor, script, fromIndex, toIndex);

        for (int index = fromIndex; index < toIndex; ) {
            index = accept0(visitor, script, fromIndex, toIndex,
                null, (byte) 0);
        }
    }

    // Limited parser, to recognize non-script literal strings which
    // only contain %+ and %! as directives.
    private int preAccept(ScriptVisitor visitor,
        byte[] script, int fromIndex, int toIndex) {

        for (int index = fromIndex; index < toIndex; ) {
            byte ch = script[index];
            int nextIndex = index + 1;
            if (ch == '%') {
                if (nextIndex == toIndex)
                    // Some string capabilities contain trailing '%'s,
                    // so do not report an error.
                    break;
                byte nextCh = script[nextIndex];
                if (nextCh == '+' || nextCh == '!') {
                    // Some scripts contain unquoted %+ and %!.  If
                    // there are no output directives, we can treat
                    // them as literal.
                    index = nextIndex + 1;
                    continue;
                } else
                    // Perhaps a real (output) directive.  Process the
                    // script using the full parser.
                    return fromIndex;
            }
            ++index;
        }

        // Everything points towards a literal string.  Process it
        // directly and tell accept() to skip the full parsing.
        for (int index = fromIndex; index < toIndex; ++index)
            visitor.visit(script[index]);
        return toIndex;
    }

    private int accept0(ScriptVisitor visitor,
        byte[] script, int index, int toIndex,
        ScriptVisitor.IfVisitor ifVisitor, byte ifToken) {

        while (index < toIndex) {
            // Store the offset for the lastOffset() method.
            offset = index;

            byte ch = script[index];
            ++index;
            if (ch != '%') {
                // Not a directive.
                visitor.visit(ch);
                continue;
            }

            if (index == toIndex) {
                // Some terminal descriptions contain trailing '%'s.
                // Treat it as a literal character.
                visitor.visit(ch);
                break;
            }

            // This is a % directive.

            ch = script[index];
            // Handle printf-style format strings separately.  %+ and
            // %- are arithmetic operations handled below.
            if (ch == ':' || ch == '#' || ch == ' ' // Flags.
                || (ch >= '0' && ch <= '9') // Zero flag, or position.
                || ch == '.'                // Width.
                || ch == 'd' || ch == 'o' || ch == 'x' || ch == 'X'
                || ch == 's') {
                index = handleFormat(visitor, script, index, toIndex);
                continue;
            }

            ++index;
            switch (ch) {
            case '%':
                visitor.visit(ch);
                break;

            case 'c':
                visitor.visit(ScriptVisitor.FormatOp.PLAIN_OP[
                        ScriptVisitor.FormatOp.CHAR.ordinal()]);
                break;

            case '!':
                visitor.visit(ScriptVisitor.UnaryOp.NEGATE);
                break;

            case '~':
                visitor.visit(ScriptVisitor.UnaryOp.BIT_COMPLEMENT);
                break;

            case 'l':
                visitor.visit(ScriptVisitor.UnaryOp.STRING_LENGTH);
                break;

            case '+':
                visitor.visit(ScriptVisitor.BinaryOp.ADD);
                break;

            case '-':
                visitor.visit(ScriptVisitor.BinaryOp.SUBTRACT);
                break;

            case '*':
                visitor.visit(ScriptVisitor.BinaryOp.MULTIPLY);
                break;

            case '/':
                visitor.visit(ScriptVisitor.BinaryOp.DIVIDE);
                break;

            case 'm':
                visitor.visit(ScriptVisitor.BinaryOp.MODULO);
                break;

            case '&':
                visitor.visit(ScriptVisitor.BinaryOp.BIT_AND);
                break;

            case '|':
                visitor.visit(ScriptVisitor.BinaryOp.BIT_OR);
                break;

            case '^':
                visitor.visit(ScriptVisitor.BinaryOp.BIT_XOR);
                break;

            case '=':
                visitor.visit(ScriptVisitor.BinaryOp.EQUAL);
                break;

            case '<':
                visitor.visit(ScriptVisitor.BinaryOp.LESS_THAN);
                break;

            case '>':
                visitor.visit(ScriptVisitor.BinaryOp.GREATER_THAN);
                break;

            case 'A':
                visitor.visit(ScriptVisitor.BinaryOp.LOGICAL_AND);
                break;

            case 'O':
                visitor.visit(ScriptVisitor.BinaryOp.LOGICAL_OR);
                break;

            case 'i':
                visitor.visitIncrement();
                break;

            case 'p':
                if (index == toIndex)
                    throw new TerminalScriptException(
                        "Truncated %p directive");
                ch = script[index];
                if (ch < '1' || ch > '9')
                    throw new TerminalScriptException(
                        "Invalid %p parameter index: " + ch);
                ++index;
                visitor.visitPushParameter(ch - '0');
                break;

            case 'g':
                if (index == toIndex)
                    throw new TerminalScriptException(
                        "Truncated %g directive");
                ch = script[index];
                if (!validVariable(ch))
                    throw new TerminalScriptException(
                        "Invalid %g variable index: " + ch);
                ++index;
                visitor.visitPushVariable(ch);
                break;

            case 'P':
                if (index == toIndex)
                    throw new TerminalScriptException(
                        "Truncated %P directive");
                ch = script[index];
                if (!validVariable(ch))
                    throw new TerminalScriptException(
                        "Invalid %P variable index: " + ch);
                ++index;
                visitor.visitSetVariable(ch);
                break;

            case '\'':
                if (toIndex - index < 2)
                    throw new TerminalScriptException(
                        "Truncated %' directive");
                ch = script[index];
                ++index;
                if (script[index] != '\'')
                    throw new TerminalScriptException(
                        "Invalid %' directive: %' "
                        + ch + ' ' + script[index]);
                ++index;
                visitor.visitPushConstant(Byte.toUnsignedInt(ch));
                break;

            case '{':
                int i;
                int value = 0;
                for (i = index; i < toIndex; ++i) {
                    ch = script[i];
                    if (ch == '}')
                        break;
                    if (ch < '0' || ch > '9')
                        throw malformedBrace(script, index, i + 1);
                    try {
                        value = Math.addExact(
                            Math.multiplyExact(value, 10),
                            (ch - '0'));
                    } catch (ArithmeticException e) {
                        throw new TerminalScriptException(
                            "Integer overflow in %{...} constant", e);
                    }
                }
                if (i == toIndex)
                    throw malformedBrace(script, index, toIndex);
                index = i + 1;
                visitor.visitPushConstant(value);
                break;

            case '?':
                ScriptVisitor nestedVisitor = null;
                ScriptVisitor.IfVisitor nestedIfVisitor;
                nestedIfVisitor = visitor.visitIf();
                if (nestedIfVisitor != null)
                    nestedVisitor = nestedIfVisitor.visitIf();
                if (nestedVisitor == null)
                    nestedVisitor = NoopVisitor.INSTANCE;
                index = accept0(nestedVisitor, script, index, toIndex,
                    nestedIfVisitor, (byte) 't');
                break;

            case 't':
                checkIfToken(index, ch, ifToken);
                if (ifVisitor != null)
                    visitor = ifVisitor.visitThen();
                if (visitor == null)
                    visitor = NoopVisitor.INSTANCE;
                ifToken = 'e';
                break;

            case 'e':
                checkIfToken(index, ch, ifToken);
                if (ifVisitor != null)
                    visitor = ifVisitor.visitElse();
                if (visitor == null)
                    visitor = NoopVisitor.INSTANCE;
                ifToken = ';';
                break;

            case ';':
                checkIfToken(index, ch, ifToken);
                if (ifVisitor != null)
                    ifVisitor.visitEnd();
                return index;

            default:
                if (ch < ' ')
                    throw new TerminalScriptException(String.format(
                            "Invalid directive: %%\\%03o",
                            Byte.toUnsignedInt(ch)));
                else
                    throw new TerminalScriptException(
                        "Invalid directive: %" + (char) ch);
            }
        }

        return toIndex;
    }

    private int offset;

    private int handleFormat(ScriptVisitor visitor, byte[] script,
        int index, int toIndex) {
        int initialOffset = offset;
        byte ch = script[index];
        ++index;
        if (ch == ':') {
            // ':' is used to disambiguate %+ and %-.
            if (index == toIndex)
                throw new TerminalScriptException(
                    "Truncated formatting directive: %:");
            ch = script[index];
            ++index;
        }

        // Process flags.
        int flags = 0;
        boolean sharpSeen = false;
        SCAN_FLAGS: while (true) {
            ScriptVisitor.FormatFlag flag;
            switch (ch) {
            case '0':
                flag = ScriptVisitor.FormatFlag.ZERO;
                break;
            case '+':
                flag = ScriptVisitor.FormatFlag.PLUS;
                break;
            case '-':
                flag = ScriptVisitor.FormatFlag.MINUS;
                break;
            case '#':
                sharpSeen = true;
                flag = ScriptVisitor.FormatFlag.SHARP;
                break;
            case ' ':
                flag = ScriptVisitor.FormatFlag.SPACE;
                break;
            default:
                break SCAN_FLAGS;
            }
            flags |= 1 << flag.ordinal();
            if (index == toIndex)
                throw new TerminalScriptException(
                    "Truncated formatting directive");
            ch = script[index];
            ++index;
        }

        int width;
        if ('0' <= ch && ch <= '9') {
            width = 0;
            while (true) {
                if ('0' <= ch && ch <= '9') {
                    width = width * 10 + (ch - '0');
                    // Guard against large string creation during
                    // script rendering.
                    if (width >= 1000)
                        throw new TerminalScriptException("% width too large");
                } else
                    break;
                if (index == toIndex)
                    throw new TerminalScriptException(
                        "Truncated formatting directive");
                ch = script[index];
                ++index;
            }
        } else
            width = -1;

        int precision;
        if (ch == '.') {
            precision = 0;
            if (index == toIndex)
                throw new TerminalScriptException(
                    "Truncated formatting directive");
            ch = script[index];
            ++index;
            while (true) {
                if ('0' <= ch && ch <= '9') {
                    precision = precision * 10 + (ch - '0');
                    // Guard against large string creation during
                    // script rendering.
                    if (precision >= 1000)
                        throw new TerminalScriptException(
                            "% precision too large");
                } else
                    break;
                if (index == toIndex)
                    throw new TerminalScriptException(
                        "Truncated formatting directive");
                ch = script[index];
                ++index;
            }
        } else
            precision = -1;

        ScriptVisitor.FormatOp op;
        boolean sharpIsError = false;
        switch (ch) {
        case 'd':
            op = ScriptVisitor.FormatOp.DECIMAL;
            sharpIsError = true;
            break;
        case 'o':
            op = ScriptVisitor.FormatOp.OCTAL;
            break;
        case 'x':
            op = ScriptVisitor.FormatOp.HEXADECIMAL_LOWERCASE;
            break;
        case 'X':
            op = ScriptVisitor.FormatOp.HEXADECIMAL_UPPERCASE;
            break;
        case 's':
            op = ScriptVisitor.FormatOp.STRING;
            sharpIsError = true;
            break;
        default:
            throw new TerminalScriptException(
                "Invalid formatting directive: " + ch);
        }

        if (sharpSeen && sharpIsError)
            throw new TerminalScriptException(String.format(
                    "%%%c directive at offset %d does not support # flag",
                    ch, initialOffset));

        ScriptVisitor.Format format =
            new ScriptVisitor.Format(flags, width, precision, op);
        visitor.visit(format);

        return index;
    }

    private static boolean validVariable(byte var) {
        return ('A' <= var && var <= 'Z')
            || ('a' <= var && var <= 'z');
    }

    private static TerminalScriptException malformedBrace(
        byte[] script, int fromIndex, int toIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append("Malformed %{ directive: %{");
        for (int i = fromIndex; i < toIndex; ++i) {
            sb.append(' ');
            sb.append((int) script[i]);
        }
        return new TerminalScriptException(sb.toString());
    }

    private static void checkIfToken(int offset, byte ch, byte ifToken) {
        if (ch == ifToken)
            return;
        if (ch == ';' && ifToken == 'e')
            // Omitted else part.
            return;
        if (ch == 't' && ifToken == ';')
            // Else-if chaining.
            return;
        offset -= 2;        // Adjust for caller's advance.
        throw new TerminalScriptException(
            "Unexpected directive in %?: %" + (char) ch
            + " instead of %" + (char) ifToken
            + " at offset " + offset);
    }
}
