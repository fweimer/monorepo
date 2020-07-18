package enyo.lang.classfile;

/**
 * A pair of a name and an @{linkplain ElementValue element value}.
 *
 * These pairs occur as the {@code element_value_pairs} in the
 * {@code annotation} and {@code type_annotation} structures.
 */
public final class NamedElementValue extends SizedCached {
    /**
     * Returns the name of this pair.
     * @return the name; a string
     */
    public String name() {
        return classFile.constantUtf8At(offset);
    }

    /**
     * Returns the element value associated with this pair.
     * @return the element value
     */
    public ElementValue value() {
        return new ElementValue(classFile, offset + 2);
    }

    /**
     * Returns the element value as a Java object.
     *
     * @return the value as an object; equivalent to {@code value().object()}
     * @see ElementValue#object()
     */
    public Object object() {
        return ElementValue.object(classFile, offset + 2);
    }

    @Override
    public String toString() {
        return name() + '=' + value();
    }

    NamedElementValue(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    int computeSize() {
        return computeSize(classFile, offset);
    }

    static int computeSize(ClassFile classFile, int offset) {
        return 2 + ElementValue.computeSize(classFile, offset + 2);
    }
}
