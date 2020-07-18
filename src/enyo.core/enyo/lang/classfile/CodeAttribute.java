package enyo.lang.classfile;

import java.nio.ByteBuffer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Attribute on a method containing the bytecode and associated metadata.
 */
public final class CodeAttribute extends Attribute implements Annotated {
    /**
     * Returns the maximum stack depth in the bytecode.
     * @return the maximum stack depth, a non-negative integer;
     *   this corresponds to the {@code max_stack} member of the
     *   {@code Code_attribute} structure
     */
    public int maxStack() {
        return classFile.unsignedShortAt(offset);
    }

    /**
     * Returns the number of local variables references in the bytecode.
     * @return the number of local variables, a non-negative integer;
     *   this corresponds to the {@code max_locals} member of the
     *   {@code Code_attribute} structure
     */
    public int maxLocals() {
        return classFile.unsignedShortAt(offset + 2);
    }

    /**
     * Returns the bytecode of this code attribute.
     * @return a byte buffer covering the bytecode
     */
    public ByteBuffer code() {
        int codeLength = classFile.intAt(offset + 4);
        int codeStart = offset + 8;
        return classFile.bufferSlice(codeStart, codeStart + codeLength);
    }

    /**
     * Returns the attributes nested in this code attribute.
     * @return a stream of {@link Attribute attributes}
     */
    public Stream<Attribute> attributes() {
        int codeLength = classFile.intAt(offset + 4);
        int codeEnd = offset + 8 + codeLength;
        int exceptionTableLength = classFile.unsignedShortAt(codeEnd);
        int exceptionTableEnd = codeEnd + 2 + exceptionTableLength * 8;
        return StreamSupport.stream(
            new AttributeSpliterator(classFile, exceptionTableEnd), false);
    }

    @Override
    public Stream<Annotation> annotations() {
        return TypeAnnotationsAttribute.asAnnotationStream(attributes());
    }

    CodeAttribute(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
