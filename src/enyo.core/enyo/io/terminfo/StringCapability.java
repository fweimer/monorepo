package enyo.io.terminfo;

import java.util.List;

/**
 * A string-value terminfo capability.
 *
 * String capabilities that correspond to terminal keys are collected
 * under {@link KeyCapability}.
 *
 * @see TermFile#get(StringCapability)
 */
public interface StringCapability {
    /**
     * The long name of the string capability.
     * @return an upper-case identifier
     */
    String name();

    /**
     * Returns the index of the capability in the terminal file.
     * @return a non-negative integer
     */
    int index();

    /**
     * Returns the expected number of parameters.
     * @return a non-negative integer
     */
    int parameters();

    /**
     * Returns the name that is used in terminfo definitions.
     * @return a non-empty string
     */
    String terminfoName();

    /**
     * Returns a list of the defined string capabilities.
     * {@linkplain KeyCapability Key capabilities} are not included.
     * @return an immutable list
     */
    static List<StringCapability> values() {
        return UntypedStringCapability.VALUES;
    }

    /**
     * Returns the string capability for the specified name.
     * The name must be the {@linkplain #name() upper-case long name}.
     * @param name the name of the capability
     * @return the string capability
     * @throws IllegalArgumentException if the argument is not a recognized
     *   string capability identifier
     * @throws NullPointerException if the argument is null
     */
    static StringCapability of(String name) {
        try {
            return UntypedStringCapability.valueOf(name);
        } catch (IllegalArgumentException e) {
        }
        try {
            return IntStr.valueOf(name);
        } catch (IllegalArgumentException e) {
        }
        try {
            return KeyCapability.valueOf(name);
        } catch (IllegalArgumentException e) {
        }
        throw new IllegalArgumentException(name);
    }

    /**
     * String capabilities which accept an integer and a string
     * parameter.
     */
    enum IntStr implements StringCapability {
        /**
         */
        PKEY_KEY(115, "pfkey"),

        /**
         */
        PKEY_LOCAL(116, "pfloc"),

        /**
         */
        PKEY_XMIT(117, "pfx"),

        /**
         */
        PLAB_NORM(147, "pln"),
        ;

        @Override
        public int index() {
            return index;
        }

        @Override
        public int parameters() {
            return 2;
        }

        @Override
        public String terminfoName() {
            return terminfoName;
        }

        private final int index;
        private final String terminfoName;

        private IntStr(int index, String terminfoName) {
            this.index = index;
            this.terminfoName = terminfoName;
        }
    }
}
