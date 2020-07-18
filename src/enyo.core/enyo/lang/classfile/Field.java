package enyo.lang.classfile;

/**
 * Field declaration in a class.
 */
public final class Field extends Member {
    public String toString() {
        return toString("Field");
    }

    Field(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
