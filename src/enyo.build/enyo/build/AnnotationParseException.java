package enyo.build;

/**
 * Error during parsing of testing annotations.
 */
final class AnnotationParseException extends RuntimeException {
    AnnotationParseException(String message) {
        super(message);
    }

    AnnotationParseException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 0x40ca4364061b54e0L;
}
