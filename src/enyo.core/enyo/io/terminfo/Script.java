package enyo.io.terminfo;

import java.util.Arrays;
import java.io.ByteArrayOutputStream;

final class Script {
    Script(byte[] script) {
        this.script = script;
    }

    byte[] run(Object[] args) {
        stackDepth = 0;
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        while (pos < script.length) {
            byte ch = script[pos];
            ++pos;

            if (ch != '%' || pos == script.length) {
                result.write(ch);
                continue;
            }

            ch = script[pos];
            ++pos;

            switch (ch) {
            case '%':
                result.write('%');
                break;
            case 'c':
                result.write(popInt());
                break;
            case 's':
                // Call method that does not throw IOException.
                byte[] tmp = popBytes();
                result.write(tmp, 0, tmp.length);
                break;
            case ':':
            case '#':
            case ' ':
                handlePrintf(ch);
                break;
            case 'p':
                if (pos == script.length) {
                    result.write((byte) '%');
                    result.write((byte) 'p');
                } else {
                    ch = script[pos];
                    ++pos;
                    if ('0' <= ch && ch <= '9')
                        push(args[ch - '0']);
                    else
                        throw new IllegalArgumentException(
                            "Invalid argument to %p: " + ch);
                }
                break;
            case 'P':
                setVar(script[pos++], popInt());
                break;
            case 'g':
                if (pos == script.length) {
                    result.write((byte) '%');
                    result.write((byte) 'g');
                } else
                    push(var(script[pos++]));
                break;
            case 'l':
                push(popBytes().length);
                break;
            case '\\':
                // FIXME:;
                break;
            case '{':
                break;
            case '+':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left + right);
                }
                break;
            case '-':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left - right);
                }
                break;
            case '*':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left * right);
                }
                break;
            case '/':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left / right);
                }
                break;
            case 'm':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left % right);
                }
                break;
            case '&':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left & right);
                }
                break;
            case '|':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left | right);
                }
                break;
            case '^':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left ^ right);
                }
                break;
            case '=':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left == right);
                }
                break;
            case '<':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left < right);
                }
                break;
            case '>':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left > right);
                }
                break;
            case 'A':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left != 0 && right != 0);
                }
                break;
            case 'O':
                {
                    int left = popInt();
                    int right = popInt();
                    push(left !=0 || right != 0);
                }
                break;
            case '!':
                push(popInt() == 0);
                break;
            case '~':
                push(~popInt());
                break;
            case 'i':
                args[0] = (int) args[0] + 1;
                args[1] = (int) args[1] + 1;
                break;
            case '?':
                break;
            default:
                throw new IllegalArgumentException(
                    "Invalid % directive: " + ch);
            }
        }

        return result.toByteArray();

    }

    private final byte[] script;
    private int pos;
    private Object[] stack;
    private int stackDepth;
    private int[] variables;

    private static int varIndex(byte ch) {
        if ('A' <= ch && ch <= 'Z')
            return ch - 'A';
        else if ('a' <= ch && ch <= 'z')
            return 26 + ch - 'a';
        else
            throw new IllegalArgumentException("Variable reference: " + ch);
    }

    private void setVar(byte ch, int value) {
        if (variables == null)
            variables = new int[2 * 26];
        variables[varIndex(ch)] = value;
    }

    private int var(byte ch) {
        if (variables == null)
            return 0;
        return variables[varIndex(ch)];
    }

    private void push(Object o) {
        if (stack == null)
            stack = new Object[4];
        else if (stackDepth == stack.length)
            stack = Arrays.copyOf(stack, stack.length * 2);
        stack[stackDepth++] = o;
    }

    private int popInt() {
        if (stackDepth == 0)
            throw new IllegalStateException("Stack underflow");
        return (int) stack[--stackDepth];
    }

    private byte[] popBytes() {
        if (stackDepth == 0)
            throw new IllegalStateException("Stack underflow");
        return (byte[]) stack[--stackDepth];
    }

    private void handlePrintf(byte ch) {
    }
}
