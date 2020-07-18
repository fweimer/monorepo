package enyo.lang.classfile;

/**
 * An {@code AnnotationDefault} attribute.
 *
 * This attribute specifies the default values of elements of
 * annotation types.
 */
public final class AnnotationDefaultAttribute extends Attribute {
    /**
     * Returns the default value of the annoation.
     * @return the default value
     * @see #object()
     */
    public ElementValue value() {
        return new ElementValue(classFile, offset + 6);
    }

    /**
     * Returns the default value of the annotation as Java object.
     * @return an object corresponding to the {@link ElementValue} for
     *  the default
     * @see #value()
     * @see ElementValue#object()
     */
    public Object object() {
        return ElementValue.object(classFile, offset + 6);
    }

    @Override
    public String toString() {
        return value().toString();
    }

    AnnotationDefaultAttribute(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
