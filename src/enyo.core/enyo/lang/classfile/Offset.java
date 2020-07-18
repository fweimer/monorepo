package enyo.lang.classfile;

abstract class Offset {
    final ClassFile classFile;
    final int offset;

    Offset(ClassFile classFile, int offset) {
        this.classFile = classFile;
        this.offset = offset;
    }
}
