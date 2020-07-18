package enyo.lang.classfile;

abstract class SizedCached extends Sized {
    final int size() {
        int sizeLocal = size;
        if (sizeLocal > 0)
            return sizeLocal;
        sizeLocal = computeSize();
        size = sizeLocal;
        return sizeLocal;
    }

    /**
     * Compute the size of this structure.  The default implementation
     * throws {@link UnsupportedOperationException}. It is assumed
     * that {@link size} is set directly in this case.
     *
     * @return the size in bytes, a positive value
     */
    int computeSize() {
        throw new UnsupportedOperationException();
    }

    int size;

    SizedCached(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
