package enyo.lang.classfile;

/**
 * A malformed class file.
 *
 * This exception can occur while parsing or analyzing a class file.
 *
 * @see ClassFile
 */
public final class ClassFileException extends RuntimeException {
    /**
     * Creates a new class file parsing exception.
     * @param message the exception message
     */
    public ClassFileException(String message) {
        super(message);
    }

    /**
     * Creates a new class file parsing exception.
     * @param message the exception message
     * @param cause the cause of this exception
     */
    public ClassFileException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 0x321d80d0b892785L;
}
