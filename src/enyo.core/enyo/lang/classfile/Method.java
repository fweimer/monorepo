package enyo.lang.classfile;

/**
 * Method declaration in a class.
 */
public final class Method extends Member {
    public String toString() {
        return toString("Method");
    }

    Method(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
