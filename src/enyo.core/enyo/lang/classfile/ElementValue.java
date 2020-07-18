package enyo.lang.classfile;

/**
 * A value stored in a class file.
 *
 * Such values occur as part of {@linkplain Annotation annotations} and type
 * annotations, and as part of the annotation types to, specify the
 * {@linkplain AnnotationDefaultAttribute annotation defaults}.
 *
 * @see NamedElementValue
 */
public final class ElementValue extends SizedCached {
    /**
     * Returns a run-time object for the specified value.
     * @return a primitive value, string, {@link Descriptor},
     *   {@link EnumConstant}, {@link Annotation}, or an array
     *   of such objects
     */
    public Object object() {
        return object(classFile, offset);
    }

    public String toString() {
        int offset1 = offset + 1;
        byte tag = classFile.byteAt(offset);
        switch (tag) {
        case 'B':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
            return classFile.constantObjectAt(offset1).toString();
        case 'C':
            // FIXME: Quote characters.
            return Character.toString(
                (char) classFile.constantIntegerAt(offset1));
        case 'Z':
            return Boolean.toString(classFile.constantIntegerAt(offset1) != 0);
        case 's':
            // FIXME: Quote strings.
            return classFile.constantUtf8At(offset1);
        case 'c':
            return classFile.constantUtf8At(offset1);
        case 'e':
            return classFile.constantUtf8At(offset1)
                + '.' + classFile.constantUtf8At(offset + 3);
        case '@':
            return '@' + new Annotation(classFile, offset1).descriptor();
        case '[':
            return "Array,  length " + classFile.unsignedShortAt(offset + 1);
        default:
            throw new ClassFileException("Invalid element value tag " + tag);
        }
    }

    ElementValue(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    int computeSize() {
        return computeSize(classFile, offset);
    }

    static int computeSize(ClassFile classFile, int startOffset) {
        byte tag = classFile.byteAt(startOffset);
        switch (tag) {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':
        case 's':
        case 'c':
            // Tag and two-byte constant pool index.
            return 3;
        case 'e':
            // Tag and two constant pool indices.
            return 5;
        case '@':
            return 1 + Annotation.computeSize(classFile, startOffset + 1);
        case '[':
            int count = classFile.unsignedShortAt(startOffset + 1);
            int offset = startOffset + 3;
            for (int i = 0; i < count; ++i)
                offset += computeSize(classFile, offset);
            return offset - startOffset;
        default:
            throw new ClassFileException("Invalid element value tag: " + tag);
        }
    }

    static Object object(ClassFile classFile, int offset) {
        int offset1 = offset + 1;
        byte tag = classFile.byteAt(offset);
        switch (tag) {
        case 'B':
            return (byte) classFile.constantIntegerAt(offset1);
        case 'C':
            return (char) classFile.constantIntegerAt(offset1);
        case 'D':
            return classFile.constantDoubleAt(offset1);
        case 'F':
            return classFile.constantFloatAt(offset1);
        case 'I':
            return classFile.constantIntegerAt(offset1);
        case 'J':
            return classFile.constantLongAt(offset1);
        case 'S':
            return (short) classFile.constantIntegerAt(offset1);
        case 'Z':
            return classFile.constantIntegerAt(offset1) != 0;
        case 's':
            return classFile.constantUtf8At(offset1);
        case 'c':
            return Descriptor.of(classFile.constantUtf8At(offset1));
        case 'e':
            return new EnumConstant(classFile, offset1);
        case '@':
            return new Annotation(classFile, offset1);
        case '[':
            return newArray(classFile, offset1);
        default:
            throw new ClassFileException("Invalid element value tag " + tag);
        }
    }

    static Object[] newArray(ClassFile classFile, int offset) {
        Object[] result = new Object[classFile.unsignedShortAt(offset)];
        offset += 2;
        for (int i = 0; i < result.length; ++i) {
            result[i] = object(classFile, offset);
            offset += computeSize(classFile, offset);
        }
        return result;
    }
}
