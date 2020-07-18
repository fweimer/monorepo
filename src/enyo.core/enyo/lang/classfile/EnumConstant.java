package enyo.lang.classfile;

/**
 * An enumeration constant.
 *
 * Such constants are used in {@linkplain ElementValue element values}.
 * @see ElementValue
 */
public final class EnumConstant extends Offset {
    /**
     * Returns the type of this constant.
     * @return the binary name of the enumeration for this constant
     */
    public String type() {
        return classFile.constantUtf8At(offset);
    }

    /**
     * Returns the enumeration value.
     * @return the name of the enumeration value
     */
    public String name() {
        return classFile.constantUtf8At(offset + 2);
    }

    @Override
    public String toString() {
        return type() + '.' + name();
    }

    EnumConstant(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
