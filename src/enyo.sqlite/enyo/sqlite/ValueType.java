package enyo.sqlite;

/**
 * The manifested type of an SQLite value.
 *
 * @see Statement#columnType(int)
 */
public enum ValueType {
    /**
     * An SQLite integer value, represented as a Java {@code long}.
     */
    INTEGER,
    /**
     * An SQLite floating-point value, represented as a Java {@code double}.
     */
    FLOAT,
    /**
     * An SQLite text value, represented as a Java {@link String}.
     * (Other representations are available.)
     */
    TEXT,
    /**
     * An SQLite array-of-bytes value, represented as a Java {@code byte[]}.
     * (Other representations are available.)
     */
    BLOB,
    /**
     * An SQLite floating-point value, represented as a Java null reference.
     */
    NULL;

    private static final ValueType[] cached = values();

    static ValueType fromConstant(int type) {
        if (type <= 0 || type > 5)
            throw new UnsupportedOperationException(
                "Unsupported SQLite value type: " + type); 
        return cached[type - 1];
    }
}
