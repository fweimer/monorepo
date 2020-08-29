package enyo.io.terminfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analysis of terminal scripts using static interpretation.
 */
public final class ScriptAnalysis {
    /**
     * Creates a new analysis of a terminal script.
     * @param script the terminal script, usually from a
     *   {@linkplain TermFile#get(StringCapability) string capability}
     */
    public ScriptAnalysis(byte[] script) {
        ScriptParser parser = new ScriptParser();

        Visitor visitor = new Visitor(parser);
        try {
            parser.accept(visitor, script);
        } catch (AnalysisException e) {
            visitor.errors.add(e.getMessage());
        }
        if (visitor.errors.isEmpty() && visitor.depth != 0)
            visitor.errors.add(
                "Stack depth at end of script: " + visitor.depth);
        parameterTypes = ParameterStatus.map(visitor.params);
        maxStackDepth = visitor.maxDepth;
        modifiedVariables = Set.copyOf(visitor.modifiedVariables);
        readVariables = Set.copyOf(visitor.readVariables);
        legacyStackMode = visitor.stackMode.legacyMode;
        errors = List.copyOf(visitor.errors);
    }

    /**
     * The inferred type of a script parameter.
     */
    public enum ParameterType {
        /**
         * The parameter is used as an integer in the script.
         */
        INTEGER,

        /**
         * The parameter is used as a string in the script.
         */
        STRING,
        ;
    }

    /**
     * Returns the inferred types of the script parameters.  If the
     * script does not reference a parameter, it is not present in the
     * returned map.
     * @return an immutable map, with parameter indices as the key
     *   (the type for parameter {@code %p1} is at key 1 if present,
     *   and so on)
     */
    public Map<Integer, ParameterType> parameterTypes() {
        return parameterTypes;
    }

    /**
     * Returns the maximum stack depth needed for executing the script.
     * @return the maximum stack depth, a non-negative value
     */
    public int maxStackDepth() {
        return maxStackDepth;
    }

    /**
     * Returns the variables that are potentially modified by the script.
     * @return an immutable set of variable names
     */
    public Set<Character> modifiedVariables() {
        return modifiedVariables;
    }

    /**
     * Returns the variables that are potentially read by the script.
     * @return an immutable set of variable names
     */
    public Set<Character> readVariables() {
        return readVariables;
    }

    /**
     * Returns true if the terminal uses legacy stack mode.  In legacy
     * stack mode, the first two parameters are pushed to the stack at
     * the start of the terminal script.
     * @return true if legacy stack mode is active, false if not
     */
    public boolean legacyStackMode() {
        return legacyStackMode;
    }

    /**
     * Returns errors encountered during the analysis.
     * @return an immutable list of error message; if empty, there were
     *   no errors
     */
    public List<String> errors() {
        return errors;
    }

    private final Map<Integer, ParameterType> parameterTypes;
    private final int maxStackDepth;
    private final Set<Character> modifiedVariables;
    private final Set<Character> readVariables;
    private final boolean legacyStackMode;
    private final List<String> errors;

    private interface StackValue {
        boolean isError();
    }

    private enum ComputedValue implements StackValue {
        INTEGER, STRING,

        /**
         * Used to suppress the reporting of cascading errors.
         */
        ERROR,
        ;

        @Override
        public boolean isError() {
            return this == ERROR;
        }

        ParameterType type() {
            switch (this) {
            case INTEGER:
                return ParameterType.INTEGER;
            case STRING:
                return ParameterType.STRING;
            case ERROR:
                break;
            }
            throw new IllegalStateException(toString());
        }
    }

    private static final class ParameterStatus implements StackValue {
        final int index;
        private ParameterType type;
        private int typeOffset = -1; // Where the type is fixed.
        private boolean error;

        // %i fixes the type of the parameter, but does not actually
        // reference it.  Some one-parameter scripts use %i, but they
        // still use only one parameter.
        private boolean used;

        ParameterStatus(int index) {
            this.index = index;
        }

        @Override
        public boolean isError() {
            return error;
        }

        static Map<Integer, ParameterType> map(ParameterStatus[] params) {
            HashMap<Integer, ParameterType> result = new HashMap<>();
            // If higher parameter indices are used, lower ones are as
            // well if their type is known.
            boolean used = false;
            for (int i = params.length; i >= 1; --i) {
                ParameterStatus param = params[i - 1];
                used = used || param.used;
                if (used && param.type != null && !param.error)
                    result.put(i, param.type);
            }
            return Map.copyOf(result);
        }
    }

    /**
     * Records the state of the legacy stack mode.  In legacy stack
     * mode, the first two parameters are pushed onto the stack at the
     * start of the script.
     */
    enum LegacyStackMode {
        /**
         * No stack operation happened yet.  Legacy stack mode maybe
         * active.
         */
        UNDECIDED(false),

        /**
         * A stack operation happened that prevents legacy stack mode.
         */
        PROHIBITED(false),

        /**
         * Legacy stack mode was used.  The first parameter was
         * pushed.  The second parameter can still be pushed.
         */
        ACTIVE_PUSHED_1(true),

        /**
         * Legacy stack mode was used.  Both parameters have been
         * pushed.
         */
        ACTIVE_PUSHED_2(true),
        ;

        // If true, a parameter has been implicitly pushed onto the stack.
        final boolean legacyMode;

        private LegacyStackMode(boolean legacyMode) {
            this.legacyMode = legacyMode;
        }
    }

    private static final class Visitor implements ScriptVisitor {
        private final ScriptParser parser;

        // %p1 is at index zero, %p2 at index 1, and so on.
        private final ParameterStatus[] params;

        // Parameter references push elements of params onto the
        // stack.  Other values are computed as null.
        private StackValue[] stack;
        private int depth;
        private int maxDepth;
        private LegacyStackMode stackMode = LegacyStackMode.UNDECIDED;

        private final HashSet<Character> modifiedVariables = new HashSet<>();
        private final HashSet<Character> readVariables = new HashSet<>();
        private final ArrayList<String> errors = new ArrayList<>();

        Visitor(ScriptParser parser) {
            this.parser = parser;
            params = new ParameterStatus[9];
            for (int i = 0; i < 9; ++i)
                params[i] = new ParameterStatus(i);
        }

        @Override
        public void visit(UnaryOp op) {
            switch (op) {
            case NEGATE:
            case BIT_COMPLEMENT:
                popInt();
                break;
            case STRING_LENGTH:
                popString();
                break;
            }
            push(ComputedValue.INTEGER);
        }

        @Override
        public void visit(BinaryOp op) {
            popInt();
            popInt();
            push(ComputedValue.INTEGER);
        }

        @Override
        public void visit(Format format) {
            switch (format.op()) {
            case CHAR:
            case DECIMAL:
            case OCTAL:
            case HEXADECIMAL_LOWERCASE:
            case HEXADECIMAL_UPPERCASE:
                popInt();
                break;
            case STRING:
                popString();
                break;
            }
        }

        @Override
        public void visitIncrement() {
            blessWithType(params[0], ParameterType.INTEGER, false);
            blessWithType(params[1], ParameterType.INTEGER, false);
        }

        @Override
        public void visitPushParameter(int index) {
            ParameterStatus param = params[index - 1];
            if (param.error) {
                push(ComputedValue.ERROR);
                return;
            }
            if (stackMode.legacyMode) {
                errors.add(String.format(
                        "Parameter %d pushed in legacy stack mode"
                        + " at offet %d",
                        index, parser.lastOffset()));
                param.error = true;
                push(ComputedValue.ERROR);
                return;
            }
            push(param);
        }

        @Override
        public void visitPushVariable(int var) {
            readVariables.add((char) var);
            push(ComputedValue.INTEGER);
        }

        @Override
        public void visitSetVariable(int var) {
            modifiedVariables.add((char) var);
            popInt();
        }

        @Override
        public void visitPushConstant(int var) {
            push(ComputedValue.INTEGER);
        }

        @Override
        public void visit(byte value) {
            // Terminal output is not tracked.
        }

        @Override
        public IfVisitor visitIf() {
            return new IfVisitor() {
                private int thenOffset;
                private StackValue[] stackAtThen;
                private int elseOffset;
                private StackValue[] stackAtElse;


                @Override
                public ScriptVisitor visitIf() {
                    return Visitor.this;
                }

                @Override
                public ScriptVisitor visitThen() {
                    popInt();
                    thenOffset = parser.lastOffset();
                    stackAtThen = cloneStack();
                    return Visitor.this;
                }

                @Override
                public ScriptVisitor visitElse() {
                    elseOffset = parser.lastOffset();
                    stackAtElse = cloneStack();
                    restoreStack(stackAtThen);
                    return Visitor.this;
                }

                @Override
                public void visitEnd() {
                    if (stackAtElse == null) {
                        if (!consolidateStacks(
                                stackAtThen, stackAtThen.length,
                                stack, depth))
                            throw new AnalysisException(String.format(
                                    "Stack mismatch between %%t (offset %d)"
                                    + " and %%; (offset %d): %s != %s",
                                    thenOffset, parser.lastOffset(),
                                    formatStack(stackAtThen,
                                        stackAtThen.length),
                                    formatStack(stack, depth)));
                    } else {
                        if (!consolidateStacks(
                                stackAtElse, stackAtElse.length,
                                stack, depth))
                            throw new AnalysisException(String.format(
                                    "Stack mismatch between %%t (offset %d)"
                                    + " and %%e (offset %d): %s != %s",
                                    thenOffset, parser.lastOffset(),
                                    formatStack(stackAtElse,
                                        stackAtElse.length),
                                    formatStack(stack, depth)));
                    }
                }
            };
        }

        private StackValue[] cloneStack() {
            return Arrays.copyOf(stack, depth);
        }

        private void restoreStack(StackValue[] clonedStack) {
            stack = clonedStack;
            depth = clonedStack.length;
        }

        private boolean consolidateStacks(StackValue[] left, int leftDepth,
            StackValue[] right, int rightDepth) {
            boolean ok = true;
            if (leftDepth != rightDepth)
                return ok = false;
            int limit = Math.min(leftDepth, rightDepth);
            for (int i = 0; i < limit; ++i)
                if (!consolidateStackSlot(left[i], right[i]))
                    ok = false;
            return ok;
        }

        private boolean consolidateStackSlot(
            StackValue left, StackValue right) {
            if (left == right || left.isError() || right.isError())
                return true;
            if (left.getClass() == ParameterStatus.class
                && right.getClass() == ParameterStatus.class) {
                ParameterStatus l = (ParameterStatus) left;
                ParameterStatus r = (ParameterStatus) right;
                if (l.type == null && r.type == null) {
                    // The implementation currently does not type
                    // equivalence of different parameters.
                    l.error = true;
                    r.error = true;
                    return false;
                }

                // Propagate type information if possible.
                if (l.type == null) {
                    l.type = r.type;
                    l.typeOffset = parser.lastOffset();
                }
                if (r.type == null) {
                    r.type = l.type;
                    r.typeOffset = parser.lastOffset();
                }

                if (l.type != r.type) {
                    // Type mismatch of already-fixed types.
                    l.error = true;
                    r.error = true;
                    return false;
                }
                return true;
            }
            if (left instanceof ComputedValue
                && right instanceof ComputedValue)
                // Different enum values and not an error among them:
                // This is a mismatch.
                return false;

            // Remaining case: Class mismatch.
            ParameterStatus param;
            ComputedValue computed;
            if (left.getClass() == ParameterStatus.class) {
                param = (ParameterStatus) left;
                computed = (ComputedValue) right;
            } else {
                param = (ParameterStatus) right;
                computed = (ComputedValue) left;
            }

            // Determine the matching parameter type for the computed value.
            ParameterType computedType = computed.type();

            if (param.type == null) {
                blessWithType0(param, computedType, true);
                return true;
            }

            if (param.type != computedType) {
                // Type mismatch of already fixed parameter type.
                param.error = true;
                return false;
            }

            return true;
        }

        private static String formatStack(StackValue[] stack, int depth) {
            char[] result = new char[depth + 2];
            result[0] = '[';
            result[depth + 1] = ']';
            for (int i = depth - 1; i >= 0; --i) {
                if (stack[i] == ComputedValue.INTEGER)
                    result[1 + i] = 'I';
                else if (stack[i] == ComputedValue.STRING)
                    result[1 + i] = 'S';
                else if (stack[i] == ComputedValue.ERROR)
                    result[1 + i] = 'E';
                else
                    result[1 + i] =
                        (char) ('1' + ((ParameterStatus) stack[i]).index);
            }
            return String.valueOf(result);
        }

        private void push(StackValue value) {
            java.util.Objects.requireNonNull(value);
            int idx = depth;
            int depth1 = idx + 1;
            if (stack == null) {
                stack = new StackValue[1];
                maxDepth = Math.max(maxDepth, 1);
            } else if (idx == stack.length) {
                stack = Arrays.copyOf(stack, depth1);
                maxDepth = Math.max(maxDepth, depth1);
            }
            stack[idx] = value;
            depth = depth1;
        }

        private void popInt() {
            StackValue top = pop();
            if (top.getClass() == ParameterStatus.class)
                use((ParameterStatus) top, ParameterType.INTEGER);
            else if (top == ComputedValue.STRING)
                throw new AnalysisException(
                    "Attempt to use integer as string: "
                    + formatStack(stack, depth));
        }

        private void popString() {
            StackValue top = pop();
            if (top.getClass() == ParameterStatus.class)
                use((ParameterStatus) top, ParameterType.STRING);
            else if (top == ComputedValue.INTEGER)
                throw new AnalysisException(
                    "Attempt to use string as integer: "
                    + formatStack(stack, depth));
        }

        private StackValue pop() {
            if (depth == 0) {
                switch (stackMode) {
                case UNDECIDED:
                    // Pretend that the first argument has been pushed
                    // on to the stack all along.
                    ++maxDepth;
                    stackMode = LegacyStackMode.ACTIVE_PUSHED_1;
                    // Assume that the first parameter is pushed last.
                    return params[0];
                case ACTIVE_PUSHED_1:
                    // Likewise pretend that the second argument has
                    // been pushed.
                    ++maxDepth;
                    stackMode = LegacyStackMode.ACTIVE_PUSHED_2;
                    return params[1];
                }
                throw new AnalysisException(
                    "Stack is empty at offset " + parser.lastOffset());
            }
            StackValue result = stack[depth - 1];
            stack[depth - 1] = null;
            --depth;
            return result;
        }

        private void use(ParameterStatus param, ParameterType type) {
            blessWithType(param, type, true);
        }

        private void blessWithType(ParameterStatus param, ParameterType type,
                                   boolean used) {
            if (param.error)
                return;
            if (param.type == null)
                blessWithType0(param, type, used);
            else if (param.type == type) {
                if (used)
                    param.used = true;
            } else if (param.type != type) {
                errors.add(String.format(
                        "Parameter %d used as %s (offset %d)"
                        + " and %s (offset %d)",
                        param.index + 1,
                        param.type.toString().toLowerCase(),
                        param.typeOffset,
                        type.toString().toLowerCase(),
                        parser.lastOffset()));
                param.error = true;
            }
        }

        private void blessWithType0(ParameterStatus param, ParameterType type,
                                    boolean used) {
            param.type = type;
            param.typeOffset = parser.lastOffset();
            param.used = used;
        }
    }

    /**
     * Internal class for reporting problems encountered during script
     * analysis.
     */
    private static final class AnalysisException extends RuntimeException {
        AnalysisException(String message) {
            super(message);
        }

        private static final long serialVersionUID = 0x4a0c90e84c9698c6L;
    }
}
