package enyo.io.terminfo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Visitor for terminal scripts.
 * @see ScriptParser
 */
public interface ScriptVisitor {
    /**
     * A {@code %} directive in a terminal script.
     */
    public interface Directive {
        /**
         * Returns the operation-specific byte that follows the
         * percent sign {@code '%'}.
         * @return the byte that identifies this operation in the script
         */
        public byte directive();
    }

    /**
     * Stack operations that replace the top of the stack with a
     * different value.
     */
    public enum UnaryOp implements Directive {
        /**
         * Negates the top of the stack.  If the top of the stack is
         * zero, replaces it with one.  Otherwise, replaces it with
         * zero.  The top of the stack must be an integer value.
         * The corresponding directive is {@code %!}.
         */
        NEGATE,

        /**
         * Replaces the top of the stack with its bit-wise complement.
         * The top of the stack must be an integer value.
         * The corresponding directive is {@code %~}.
         */
        BIT_COMPLEMENT,

        /**
         * Replaces the top of the strack with its string length.
         * This turns a string at the top of the stack into an
         * integer.  The corresponding directive is {@code %l}.
         */
        STRING_LENGTH,
        ;

        @Override
        public byte directive() {
            return (byte) DIRECTIVES.charAt(ordinal());
        }

        @Override
        public String toString() {
            return "%" + DIRECTIVES.charAt(ordinal());
        }

        // The byte following the % character.
        private final static String DIRECTIVES = "!~l";
    }

    /**
     * Called for an unary stack operation in the script.
     * @param op the type of operation
     */
    public void visit(UnaryOp op);

    /**
     * Stack operations which replace two integers at the top with one
     * integer.
     */
    public enum BinaryOp implements Directive {
        /**
         * Removes two values from the stack and pushes their sum.
         * The corresponding directive is {@code %+}.
         */
        ADD {
            @Override
            public int apply(int left, int right) {
                return left + right;
            }
        },

        /**
         * Removes two values from the stack and pushes their difference.
         * The corresponding directive is {@code %-}.
         */
        SUBTRACT {
            @Override
            public int apply(int left, int right) {
                return left - right;
            }
        },

        /**
         * Removes two values from the stack and pushes their product.
         * The corresponding directive is {@code %*}.
         */
        MULTIPLY {
            @Override
            public int apply(int left, int right) {
                return left * right;
            }
        },

        /**
         * Removes two values from the stack and pushes their quotient.
         * The corresponding directive is {@code %/}.
         */
        DIVIDE {
            @Override
            public int apply(int left, int right) {
                return left / right;
            }
        },

        /**
         * Removes two values from the stack and pushes their remainder.
         * The corresponding directive is {@code %m}.
         * ({@code %%} stands for a literal {@code %}.)
         */
        MODULO {
            @Override
            public int apply(int left, int right) {
                return left % right;
            }
        },

        /**
         * Removes two values form the stack, applies a bit-wise and
         * operation, and pushes the result.
         * The corresponding directive is {@code %&}.
         */
        BIT_AND {
            @Override
            public int apply(int left, int right) {
                return left & right;
            }
        },

        /**
         * Removes two values form the stack, applies a bit-wise or
         * operation, and pushes the result.
         * The corresponding directive is {@code %|}.
         */
        BIT_OR {
            @Override
            public int apply(int left, int right) {
                return left | right;
            }
        },

        /**
         * Removes two values form the stack, applies a bit-wise
         * exclusive or operation, and pushes the result.
         * The corresponding directive is {@code %^}.
         */
        BIT_XOR {
            @Override
            public int apply(int left, int right) {
                return left ^ right;
            }
        },

        /**
         * Removes two values form the stack and pushes one if the
         * values are equal, zero otherwise.
         * The corresponding directive is {@code %=}.
         */
        EQUAL {
            @Override
            public int apply(int left, int right) {
                return intValue(left == right);
            }
        },

        /**
         * Removes two values form the stack and pushes one if the
         * first one is less than the second one, zero otherwise.
         * The corresponding directive is {@code %<}.
         */
        LESS_THAN {
            @Override
            public int apply(int left, int right) {
                return intValue(left < right);
            }
        },

        /**
         * Removes two values form the stack and pushes one if the
         * first one is greater than the second one, zero otherwise.
         * The corresponding directive is {@code %>}.
         */
        GREATER_THAN {
            @Override
            public int apply(int left, int right) {
                return intValue(left > right);
            }
        },

        /**
         * Removes two values form the stack and pushes one if both
         * values are non-zero, zero otherwise.
         * The corresponding directive is {@code %A}.
         */
        LOGICAL_AND {
            @Override
            public int apply(int left, int right) {
                return intValue(left != 0 && right != 0);
            }
        },

        /**
         * Removes two values form the stack and pushes one if at
         * least one value is non-zero, zero otherwise.
         * The corresponding directive is {@code %O}.
         */
        LOGICAL_OR {
            @Override
            public int apply(int left, int right) {
                return intValue(left != 0 || right != 0);
            }
        },
        ;

        @Override
        public byte directive() {
            return (byte) DIRECTIVES.charAt(ordinal());
        }

        /**
         * Applies this binary operation to two integer values.
         * @param left the first argument
         * @param right the second argument
         * @return the result of combining both arguments using this
         *   operation
         * @throws ArithmeticException if a division by zero is attempted
         *   (for {@link #DIVIDE} and {@link #MODULO})
         */
        public abstract int apply(int left, int right);

        @Override
        public String toString() {
            return "%" + DIRECTIVES.charAt(ordinal());
        }

        private static final int intValue(boolean flag) {
            if (flag)
                return 1;
            else
                return 0;
        }

        // The byte following the % character.
        private final static String DIRECTIVES = "+-*/m&|^=<>AO";
    }

    /**
     * Called for a binary stack operation in the script.
     * @param op the type of operation
     */
    public void visit(BinaryOp op);

    /**
     * The formatting directive in a formatting operation.
     * @see Format
     */
    public enum FormatOp implements Directive {
        /**
         * A {@code %c} directive.  This directive removes an integer
         * value from the stack and writes its lower 8 bits as a
         * literal byte.  The {@code %c} directive does not accept any
         * flags or other parameters.
         */
        CHAR,

        /**
         * A {@code %d} directive.
         * This directive removes an integer value from the stack
         * and outputs it in decimal.
         */
        DECIMAL,

        /**
         * A {@code %o} directive.
         * This directive removes an integer value from the stack
         * and outputs it in octal.
         */
        OCTAL,

        /**
         * A {@code %x} directive.
         * This directive removes an integer value from the stack
         * and outputs it in hexadecimal, using lower-case letters.
         */
        HEXADECIMAL_LOWERCASE,

        /**
         * A {@code %X} directive.
         * This directive removes an integer value from the stack
         * and outputs it in hexadecimal, using uper-case letters.
         */
        HEXADECIMAL_UPPERCASE,

        /**
         * A {@code %s} directive.
         * This directives removes a string value from the stack
         * and outputs it.
         */
        STRING;

        public byte directive() {
            return (byte) DIRECTIVES.charAt(ordinal());
        }

        // The byte following the % character.
        private final static String DIRECTIVES = "cdoxXs";

        static final Format[] PLAIN_OP;
        static {
            FormatOp[] values = values();
            PLAIN_OP = new Format[values.length];
            for (int i = 0; i < values.length; ++i)
                PLAIN_OP[i] = new Format(0, -1, -1, values[i]);
        }
    }

    /**
     * A flag in a a formatting directive.
     * @see Format
     */
    public enum FormatFlag {
        /**
         * The flag {@code -}.
         */
        MINUS,

        /**
         * The flag {@code +}.
         */
        PLUS,

        /**
         * The flag {@code #}.
         * It indicates an alternate format.
         * Available with {@link FormatOp#OCTAL %o},
         * {@link FormatOp#OCTAL %x}, and {@link FormatOp#OCTAL %X},
         * where it outputs prefixes of {@code 0}, {@code 0x},
         * and {@code 0X}.
         */
        SHARP,

        /**
         * The flag {@code ' '} (space).
         */
        SPACE,

        /**
         * The flag {@code 0}.
         */
        ZERO,
        ;

        final static String FLAGS = "-+# 0";
        final static List<Set<FormatFlag>> SETS;
        static {
            FormatFlag[] values = values();
            int setsCount = 1 << values.length;
            ArrayList<Set<FormatFlag>> sets = new ArrayList<>(setsCount);
            EnumSet<FormatFlag> set = EnumSet.noneOf(FormatFlag.class);
            for (int i = 0; i < setsCount; ++i) {
                set.clear();
                for (int j = 0; j < values.length; ++j)
                    if ((i & (1 << j)) != 0)
                        set.add(values[j]);
                sets.add(Set.copyOf(set));
            }
            SETS = List.copyOf(sets);
        }
    }

    /**
     * A formatting operation.  This combines flags, widths,
     * and the actual {@linkplain FormatOp formatting operation}.
     * @see ScriptVisitor#visit(Format)
     */
    public static class Format {
        /**
         * Returns the flags in this formatting operation.
         * @return an immutable set
         */
        public Set<FormatFlag> flags() {
            return FormatFlag.SETS.get(flags);
        }

        /**
         * Returns the width of this formatting operation.
         * @return the width (a non-negative value) if available, or -1
         *   if it was not specified
         */
        public int width() {
            return width;
        }

        /**
         * Returns the precision of this formatting operation.
         * @return the precision (a non-negative value) if available, or -1
         *   if it was not specified
         */
        public int precision() {
            return precision;
        }

        /**
         * Return the actual formatting directive.  This is indicates
         * whether an integer or string is popped off the stack by the
         * formatting operation.
         * @return a formatting operation
         */
        public FormatOp op() {
            return op;
        }

        /**
         * Formats the integer according to this formatter.
         * @param value the number to format
         * @return the formatted string
         * @throws IllegalStateException if this format is for a string
         */
        public String format(int value) {
            if (op == FormatOp.STRING)
                throw new IllegalStateException();
            return String.format(Locale.ROOT, toString(false), value);
        }

        @Override
        public String toString() {
            return toString(true);
        }

        Format(int flags, int width, int precision, FormatOp op) {
            this.flags = flags;
            this.width = width;
            this.precision = precision;
            this.op = op;
        }

        // escape indicates whether %: should be used to avoid the %+, %-
        // ambiguity.
        private String toString(boolean escape) {
            StringBuilder sb = new StringBuilder();
            sb.append('%');
            toStringFlags(sb, FormatFlag.SHARP, false);
            toStringFlags(sb, FormatFlag.PLUS, escape);
            toStringFlags(sb, FormatFlag.MINUS, escape);
            toStringFlags(sb, FormatFlag.SPACE, false);
            toStringFlags(sb, FormatFlag.ZERO, false);
            if (width >= 0)
                sb.append(width);
            if (precision >= 0) {
                sb.append('.');
                sb.append(precision);
            }
            sb.append((char) op.directive());
            return sb.toString();
        }

        private void toStringFlags(StringBuilder sb, FormatFlag flag,
                                   boolean escape) {
            if ((flags & (1 << flag.ordinal())) != 0) {
                if (sb.length() == 1 && escape)
                    // Avoid clash with %+ and %-.
                    sb.append(':');
                sb.append(FormatFlag.FLAGS.charAt(flag.ordinal()));
            }
        }

        private final int flags;
        private final int width;
        private final int precision;
        private final FormatOp op;
    }

    /**
     * Called for a format operation in the script.  The operation
     * consums the top of the stack.  Depending on the actual
     * formatting operation, this is either an integer or a string
     * (for {@link FormatOp#STRING}).  The formatting result is sent
     * to the termal.
     * @param op the description of the requested form
     */
    public void visit(Format op);

    /**
     * Called for the parameter increment directive {@code %i}.  The
     * values of the first two parameters should be increased by one.
     * The stack is not changed, and nothing is sent to the terminal.
     */
    public void visitIncrement();

    /**
     * Called to push a script parameter onto the stack.
     * Corresponds to the {@code %p} directive.
     * @param param the parameter index, from 1 to 9 (as an integer,
     *   not a character)
     */
    public void visitPushParameter(int param);

    /**
     * Called to push the value of a variable onto the stack.
     * Corresponds to the {@code %g} directive.
     * @param var the index of the variable, in the range
     *   {@code 'A'} to {@code 'Z'} or {@code 'a'} to {@code 'z'}
     *   (inclusive).
     */
    public void visitPushVariable(int var);

    /**
     * Called to push the value of a variable onto the stack.
     * @param var the index of the variable, in the range
     *   {@code 'A'} to {@code 'Z'} or {@code 'a'} to {@code 'z'}
     *   (inclusive).
     */

    /**
     * Called to assign a variable.  Removes the top of the stack and
     * assigns it to the specified variable.  Corresponds to the
     * {@code %P} directive.
     * @param var the index of the variable, in the range
     *   {@code 'A'} to {@code 'Z'} or {@code 'a'} to {@code 'z'}
     *   (inclusive).
     */
    public void visitSetVariable(int var);

    /**
     * Called topush an integer value onto the stack.
     * This corresponds to the {@code %''} and {@code %{}} directives.
     * (The byte is not sent to the terminal immediately.)
     * @param value the value to push, a non-negative integer
     */
    public void visitPushConstant(int value);

    /**
     * Called to send a literal byte to the terminal.
     * @param value the byte to output
     */
    public void visit(byte value);

    /**
     * A visitor for a {@code %?} conditional directive.
     *
     * The methods may return the original
     * {@linkplain ScriptVisitor script visitor} that was passed to the
     * {@link ScriptParser#accept(ScriptVisitor,byte[])} or
     * {@link ScriptParser#accept(ScriptVisitor,byte[],int,int)} methods.
     */
    public interface IfVisitor {
        /**
         * Returns the script visitor for the condition part.
         * This method is called right after the {@code IfVisitor}
         * has been obtained using {@link ScriptVisitor#visitIf()}.
         * This call is followed by a {@link #visitThen()} call, after
         * the nested part has been visited.
         * @return a script visitor, or null if the part between
         *   {@code %?} and {@code %t} should not be visited
         */
        public ScriptVisitor visitIf();

        /**
         * Returns the script visitor for the true part of the condition.
         * This method is called to obtain the nested script visitor
         * once a {@code %t} directive is encountered.  This can happen
         * after a {@code %?} directive, or a {@code %e} directive in
         * case of a nested else-if construct.  This call is followed by
         * a {@link #visitElse()} or {@link #visitEnd()} call, after
         * the nested part has been visited.
         * @return a script visitor, or null if the part between
         *   {@code %t} and {@code %e} (or {@code %;}) should not be visited
         */
        public ScriptVisitor visitThen();

        /**
         * Returns the script visitor for the false part of the condition.
         * This method is called to obtain the nested script visitor
         * once a {@code %e} directive is encountered.  This call
         * is followed by a {@link #visitEnd()} call, or
         * another {@link #visitThen()} call (in case of a nested else-if
         * construct), in each case after the nested part has been visited.
         * @return a script visitor, or null if the part between
         *   {@code %e} and {@code %;} (or {@code %t}) should not be visited
         */
        public ScriptVisitor visitElse();

        /**
         * Called once the conditional directive ends.
         * This corresponds to a {@code %;} directive.
         * The visiting operation continues with the original visitor
         * on which {@link ScriptVisitor#visitIf()} was invoked.
         */
        public void visitEnd();
    }

    /**
     * Called once a conditional directive is encountered.
     * This method is called for each {@code %?} directive.
     * @return a visitor, or null if the directive should not be visited
     */
    public IfVisitor visitIf();
}
