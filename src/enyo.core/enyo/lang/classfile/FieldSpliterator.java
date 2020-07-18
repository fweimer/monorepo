package enyo.lang.classfile;

final class FieldSpliterator extends MemberSpliterator<Field> {
    Field create(int offset) {
        return new Field(classFile, offset);
    }

    FieldSpliterator(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
