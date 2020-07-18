package enyo.lang.classfile;

/**
 * An unrecognized attribute in a class file.
 */
public final class OtherAttribute extends Attribute {
    OtherAttribute(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
