package enyo.lang.classfile;

final class MethodSpliterator extends MemberSpliterator<Method> {
    Method create(int offset) {
        return new Method(classFile, offset);
    }

    MethodSpliterator(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
