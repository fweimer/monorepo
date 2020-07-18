package enyo.lang.classfile;

abstract class Sized extends Offset {
    abstract int size();

    Sized(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
