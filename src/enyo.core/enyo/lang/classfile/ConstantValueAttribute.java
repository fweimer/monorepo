package enyo.lang.classfile;

/**
 * A {@code ConstantValue} attribute.
 *
 * Such an attribute associates a constant value with a
 * {@linkplain Field field}.
 */
public final class ConstantValueAttribute extends Attribute {
    /**
     * Returns the value as an object.
     * @return the object corresponding to the constant; a primitive
     *   type or a @{linkplain String string}
     */
    public Object value() {
        return classFile.constantObjectAt(offset + 6);
    }

    ConstantValueAttribute(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
