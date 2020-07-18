package enyo.lang.classfile;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

/**
 * Base class for attributes stored in a class file.
 *
 * Attributes are the primary extension mechanism for class files.
 */
public abstract class Attribute extends Sized {
    /**
     * Returns the attribute name.
     * The name of the attribute determines its semantics.
     * @return the attribute name
     */
    public String name() {
        return classFile.constantUtf8At(offset);
    }

    /**
     * Returns the size of the attribute, in bytes.
     * @return the size, a non-negative byte count
     */
    public final int size() {
        return size(classFile, offset);
    }

    /**
     * Returns a byte buffer containing the raw data in the attribute.
     * @return a new byte buffer
     */
    public ByteBuffer buffer() {
        return classFile.bufferSlice(offset + 6, size());
    }

    public String toString() {
        return String.format("Attribute %s (%d bytes)", name(), size());
    }

    Attribute(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    static int size(ClassFile classFile, int offset) {
        return 6 + classFile.intAt(offset + 2);
    }

    static int skipArray(ClassFile classFile, int offset) {
        int count = classFile.unsignedShortAt(offset);
        offset += 2;
        for (int i = 0; i < count; ++i)
            offset += size(classFile, offset);
        return offset;
    }
}
