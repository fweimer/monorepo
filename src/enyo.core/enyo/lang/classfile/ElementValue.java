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

    /**
     * Returns this value as an annoation.
     * @return the annotation
     * @throws ClassFileException if the value is not a boolean
     */
    public Annotation asAnnotation() {
        byte tag = classFile.byteAt(offset);
        if (tag == '@')
            return new Annotation(classFile, offset + 1);
        else
            throw new ClassFileException("Not a char: " + toString());
    }

    /**
     * Returns this value as a boolean.
     * @return the value
     * @throws ClassFileException if the value is not a boolean
     */
    public boolean asBoolean() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'Z')
            return classFile.constantIntegerAt(offset + 1) != 0;
        else
            throw new ClassFileException("Not a char: " + toString());
    }

    /**
     * Returns this value as a byte.
     * @return the value
     * @throws ClassFileException if the value is not a byte
     */
    public byte asByte() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'B')
            return (byte) classFile.constantIntegerAt(offset + 1);
        else
            throw new ClassFileException("Not a byte: " + toString());
    }

    /**
     * Returns this value as a character.
     * @return the value
     * @throws ClassFileException if the value is not a character
     */
    public char asChar() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'C')
            return (char) classFile.constantIntegerAt(offset + 1);
        else
            throw new ClassFileException("Not a char: " + toString());
    }

    /**
     * Returns this value as a class name
     * @return the class name, as as string
     * @throws ClassFileException if the value is not a descriptor
     */
    public String asClass() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'c')
            return classFile.constantUtf8At(offset + 1);
        else
            throw new ClassFileException("Not a class value: " + toString());
    }

    /**
     * Returns this value as a double.
     * @return the value
     * @throws ClassFileException if the value is not a double
     */
    public double asDouble() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'D')
            return classFile.constantDoubleAt(offset + 1);
        else
            throw new ClassFileException("Not a double: " + toString());
    }

    /**
     * Returns this value as an enum constant.
     * @return the enum constant
     * @throws ClassFileException if the value is not a float
     */
    public EnumConstant asEnum() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'e')
            return new EnumConstant(classFile, offset + 1);
        else
            throw new ClassFileException("Not a float: " + toString());
    }

    /**
     * Returns this value as a float.
     * @return the value
     * @throws ClassFileException if the value is not a float
     */
    public float asFloat() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'F')
            return classFile.constantFloatAt(offset + 1);
        else
            throw new ClassFileException("Not a float: " + toString());
    }

    /**
     * Returns this value as an int.
     * @return the value
     * @throws ClassFileException if the value is not an int
     */
    public int asInt() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'I')
            return classFile.constantIntegerAt(offset + 1);
        else
            throw new ClassFileException("Not an int: " + toString());
    }

    /**
     * Returns this value as a long.
     * @return the value
     * @throws ClassFileException if the value is not a long
     */
    public long asLong() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'J')
            return classFile.constantLongAt(offset + 1);
        else
            throw new ClassFileException("Not a long: " + toString());
    }

    /**
     * Returns this value as a short.
     * @return the value
     * @throws ClassFileException if the value is not a short
     */
    public short asShort() {
        byte tag = classFile.byteAt(offset);
        if (tag == 'S')
            return (short) classFile.constantIntegerAt(offset + 1);
        else
            throw new ClassFileException("Not a short: " + toString());
    }

    /**
     * Returns this value as a string
     * @return the value
     * @throws ClassFileException if the value is not a string
     */
    public String asString() {
        byte tag = classFile.byteAt(offset);
        if (tag == 's')
            return classFile.constantUtf8At(offset + 1);
        else
            throw new ClassFileException("Not a short: " + toString());
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
