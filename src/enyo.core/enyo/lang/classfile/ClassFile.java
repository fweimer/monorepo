package enyo.lang.classfile;

import enyo.util.StringFilter;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Parser for Java class file.
 */
public final class ClassFile implements Annotated {
    /**
     * Creates a new class file parser which uses data from the
     * specified buffer.
     * <p><b>Note:</b> The buffer is not copied.
     * @param buffer byte buffer with binary class data
     * @return a new class file parser
     * @throws ClassFileException if the class file is malformed and this
     *   can be detected immediately
     * @throws NullPointerException if {@code buffer} is null
     */
    public static ClassFile of(ByteBuffer buffer) {
        buffer = buffer.slice();
        if (!buffer.isReadOnly())
            buffer = buffer.asReadOnlyBuffer();
        return new ClassFile(buffer);
    }

    /**
     * Returns the binary name of the class.
     *
     * The return name is not a descriptor.  It is not surrounded by
     * {@code L} and {@code ;}.
     *
     * @return the binary class name, using {@code /} as the package separator
     *   (not {@code .} as in the Java language)
     * @throws ClassFileException if the class file is malformed
     */
    public String className() {
        return constantUtf8(constantClass(thisClass));
    }

    /**
     * A stream supplying the methods of this class.
     * @return a stream of methods
     * @throws ClassFileException if the class file is malformed and this
     *   can be detected immediately
     */
    public Stream<Method> methods() {
        return StreamSupport.stream(
            new MethodSpliterator(this, methodsOffset), false);
    }

    /**
     * A stream supplying the the fields of class.
     * @return a stream of fields
     * @throws ClassFileException if the class file is malformed and this
     *   can be detected immediately
     */
    public Stream<Field> fields() {
        return StreamSupport.stream(
            new FieldSpliterator(this, fieldsOffset), false);
    }

    /**
     * A stream supplying the attributes directly on the class type.
     * @return a stream of attributes
     * @throws ClassFileException if the class file is malformed and this
     *   can be detected immediately
     */
    public Stream<Attribute> attributes() {
        return StreamSupport.stream(
            new AttributeSpliterator(this, attributesOffset), false);
    }

    /**
     * Returns a stream of strings that are present in the class file.
     *
     * Note that this generally not faster than parsing the class file
     * for what information is needed because all the strings have to
     * be copied.  {@link #utf8MatchesAny(StringFilter)} avoids this
     * issue.
     *
     * @return a stream of UTF-8 strings from the class file
     * @see #utf8MatchesAny(StringFilter)
     * @throws ClassFileException if the class file is malformed and this
     *   can be detected immediately
     */
    public Stream<String> utf8Strings() {
        return IntStream.range(0, constantPoolOffsets.length)
            .filter(idx -> {
                    int offset = constantPoolOffsets[idx];
                    if (false)
                    System.out.format("  %d => %d (%d)%n",
                        idx, constantPoolOffsets[idx],
                        offset >= 0 ? constantPoolOffsets[idx] : -1);
                    return offset >= 0 && byteAt(offset)
                        == Constants.CONSTANT_Utf8;
                })
            .mapToObj(idx -> constantUtf8(idx + 1));
    }

    /**
     * Returns true if any of the strings in the filter are present in
     * the class.
     *
     * This can be used to implement a quick check if a particular
     * class file references certain annotation classes by name.  If
     * there is no reference to the class name as a string, there
     * cannot be an annotation with that type.
     *
     * @param filter the filter against which to check for present strings
     * @return if the class's constant pool contains a string in {@code filter}
     * @throws ClassFileException if the class file is malformed
     * @see #utf8Strings()
     */
    public boolean utf8MatchesAny(StringFilter filter) {
        for (int idx = 0; idx < constantPoolOffsets.length; ++idx) {
            int offset = constantPoolOffsets[idx];
            if (offset >= 0 && byteAt(offset) == Constants.CONSTANT_Utf8) {
                ++offset;
                // See constantUtf8(int) below.
                int bytes = unsignedShortAt(offset);
                int startOffset = offset + 2;
                int endOffset = startOffset + bytes;
                if (filter.contains(buffer, startOffset, endOffset))
                    return true;
            }
        }
        return false;
    }

    /**
     * A stream supplying the annotations directly on the class type.
     * @return a stream of annotations
     * @throws ClassFileException if the class file is malformed and this
     *   can be detected immediately
     */
    @Override
    public Stream<Annotation> annotations() {
        return AnnotationsAttribute.asAnnotationStream(attributes());
    }


    private final ByteBuffer buffer;
    private final short minorVersion;
    private final short majorVersion;
    private final int[] constantPoolOffsets;
    private final Object[] constantPoolObjects; // Lazily populated.
    private final short accessFlags;
    private final short thisClass;
    private final short superClass;
    private final int fieldsOffset;
    private final int methodsOffset;
    private final int attributesOffset;

    // Used to avoid allocations for strings that are typically
    // encountered in a class.
    private static final StringFilter STRING_INTERNER = StringFilter.of(
        "java/lang/Object", "<init>", "<clinit>",
        "B", "C", "D", "F", "I", "J", "S", "Z",
        "()B", "()C", "()D", "()F", "()I", "()J", "()S", "()V", "()Z",
        Constants.STRING_AnnotationDefault,
        Constants.STRING_BootstrapMethods,
        Constants.STRING_Code,
        Constants.STRING_ConstantValue,
        Constants.STRING_Deprecated,
        Constants.STRING_EnclosingMethod,
        Constants.STRING_Exceptions,
        Constants.STRING_InnerClasses,
        Constants.STRING_LineNumberTable,
        Constants.STRING_LocalVariableTable,
        Constants.STRING_LocalVariableTypeTable,
        Constants.STRING_MethodParameters,
        Constants.STRING_Module,
        Constants.STRING_ModuleMainClass,
        Constants.STRING_ModulePackages,
        Constants.STRING_NestHost,
        Constants.STRING_NestMembers,
        Constants.STRING_RuntimeInvisibleAnnotations,
        Constants.STRING_RuntimeInvisibleParameterAnnotations,
        Constants.STRING_RuntimeInvisibleTypeAnnotations,
        Constants.STRING_RuntimeVisibleAnnotations,
        Constants.STRING_RuntimeVisibleParameterAnnotations,
        Constants.STRING_RuntimeVisibleTypeAnnotations,
        Constants.STRING_Signature,
        Constants.STRING_SourceDebugExtension,
        Constants.STRING_SourceFile,
        Constants.STRING_StackMapTable,
        Constants.STRING_Synthetic);

    private ClassFile(ByteBuffer buffer) {
        this.buffer = buffer;

        int magic = buffer.getInt();
        if (magic != 0xCAFEBABE)
            throw new ClassFileException(
                "Invalid magic number: 0x" + Integer.toHexString(magic));
        minorVersion = buffer.getShort();
        majorVersion = buffer.getShort();

        {
            // An open question is whether we should validate the
            // entries at the time of loading the class.  In order to
            // skip validation for subsequent lookups, we would have
            // to make a deep copy of the byte buffer, though.

            int constantPoolCount = Short.toUnsignedInt(buffer.getShort());
            if (constantPoolCount == 0)
                throw new ClassFileException("Constant pool count is zero");
            --constantPoolCount;
            constantPoolOffsets = new int[constantPoolCount];
            constantPoolObjects = new Object[constantPoolCount];
            for (int i = 0; i < constantPoolCount; ++i) {
                constantPoolOffsets[i] = buffer.position();
                int tag = Byte.toUnsignedInt(buffer.get());
                int skip; // Additional bytes to skip.
                switch (tag) {
                case Constants.CONSTANT_Class:
                case Constants.CONSTANT_String:
                case Constants.CONSTANT_MethodType:
                case Constants.CONSTANT_Module:
                case Constants.CONSTANT_Package:
                    skip = 2;
                    break;
                case Constants.CONSTANT_Fieldref:
                case Constants.CONSTANT_Float:
                case Constants.CONSTANT_Integer:
                case Constants.CONSTANT_InterfaceMethodref:
                case Constants.CONSTANT_Methodref:
                case Constants.CONSTANT_NameAndType:
                case Constants.CONSTANT_Dynamic:
                case Constants.CONSTANT_InvokeDynamic:
                    skip = 4;
                    break;
                case Constants.CONSTANT_Long:
                case Constants.CONSTANT_Double:
                    skip = 8;
                    // These constants occupy two indices in the pool.
                    // The second entry is unusable.
                    ++i;
                    constantPoolOffsets[i] = -1;
                    break;
                case Constants.CONSTANT_Utf8:
                    skip = Short.toUnsignedInt(buffer.getShort());
                    break;
                case Constants.CONSTANT_MethodHandle:
                    skip = 3;
                    break;

                default:
                    throw new ClassFileException(String.format(
                            "Unknown constant tag %d at pool index %d",
                            tag, i + 1));
                }

                buffer.position(buffer.position() + skip);
            }
        }

        accessFlags = buffer.getShort();

        // TODO: Validate the CONSTANT_Class entries?
        thisClass = buffer.getShort();
        constantPoolIndexToOffset(thisClass, Constants.CONSTANT_Class);
        superClass = buffer.getShort();
        if (superClass != 0)
            constantPoolIndexToOffset(superClass, Constants.CONSTANT_Class);

        {
            // TODO: Validate the CONSTANT_Class entries?
            int interfacesCount = Short.toUnsignedInt(buffer.getShort());
            for (int i = 0; i < interfacesCount; ++i)
                try {
                    constantPoolIndexToOffset(
                        buffer.getShort(), Constants.CONSTANT_Class);
                } catch (BufferUnderflowException e) {
                    throw new ClassFileException(
                        "Truncated interfacte at " + i, e);
                }
        }
        {
            fieldsOffset = buffer.position();
            int fieldsCount = Short.toUnsignedInt(buffer.getShort());
            for (int i = 0; i < fieldsCount; ++i)
                try {
                    // Access flags, name, descriptor.
                    buffer.position(buffer.position() + 6);
                    skipAttributes(Short.toUnsignedInt(buffer.getShort()));
                } catch (BufferUnderflowException e) {
                    throw new ClassFileException(
                        "Truncated field " + i, e);
                } catch (ClassFileException e) {
                    throw new ClassFileException("Field " + i, e);
                }
        }
        {
            methodsOffset = buffer.position();
            int methodsCount = Short.toUnsignedInt(buffer.getShort());
            for (int i = 0; i < methodsCount; ++i)
                try {
                    // Access flags, name, descriptor.
                    buffer.position(buffer.position() + 6);
                    skipAttributes(Short.toUnsignedInt(buffer.getShort()));
                } catch (BufferUnderflowException e) {
                    throw new ClassFileException(
                        "Truncated method " + i, e);
                } catch (ClassFileException e) {
                    throw new ClassFileException("Method " + i, e);
                }
        }
        try {
            attributesOffset = buffer.position();
            int attributesCount = Short.toUnsignedInt(buffer.getShort());
            skipAttributes(attributesCount);
        } catch (ClassFileException e) {
            throw new ClassFileException("Invalid class attributes", e);
        }
    }

    private int constantPoolIndexToOffset(short index, int type) {
        return  constantPoolIndexToOffset(Short.toUnsignedInt(index), type);
    }

    ByteBuffer bufferSlice(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, buffer.limit());
        return buffer.slice(fromIndex, toIndex - fromIndex);
    }

    byte byteAt(int offset) {
        return buffer.get(offset);
    }

    int intAt(int offset) {
        return buffer.getInt(offset);
    }

    int unsignedShortAt(int offset) {
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    // Returns the offset after the tag byte.
    private int constantPoolIndexToOffset(int index, int type) {
        if (index < 1)
            throw new ClassFileException(String.format(
                    "Invalid constant pool index %d of type %d",
                    index, type));
        if (index - 1 > constantPoolOffsets.length)
            throw new ClassFileException(String.format(
                    "Constant pool index %d of type %d out of range"
                    + " (larger than %d)",
                    index, type, constantPoolOffsets.length));
        int offset = constantPoolOffsets[index - 1];
        if (offset < 0)
            // Second half of long or double constants.
            throw new ClassFileException(String.format(
                    "Constant pool index %d unusable (type %d expected)",
                    index, type));
        int typeAtOffset = Byte.toUnsignedInt(buffer.get(offset));
        if (typeAtOffset != type)
            throw new ClassFileException(String.format(
                    "Invalid type %d for constant pool index %d (expected %d)",
                    typeAtOffset, index, type));
        return offset + 1;
    }

    private void skipAttributes(int attributesCount) {
        for (int i = 0; i < attributesCount; ++i) {
            try {
                buffer.getShort();  // Attribute name.
                int length = buffer.getInt();
                if (length < 0)
                    throw new ClassFileException(String.format(
                            "Attribute %d has invalid length %d",
                            i, length));
                buffer.position(buffer.position() + length);
            } catch (BufferUnderflowException e) {
                throw new ClassFileException(
                    "Truncated attribute " + i, e);
            }
        }
    }

    // Returns the Utf8 constant pool index for the class name.
    int constantClass(int index) {
        return unsignedShortAt(constantPoolIndexToOffset(
                index, Constants.CONSTANT_Class));
    }

    // Returns the UTf8 constant pool index for the class name whose
    // constant pool is stored at offset.
    int constantClassAt(int offset) {
        return constantClass(Short.toUnsignedInt(buffer.getShort(offset)));
    }

    // Decodes Modified UTF-8 encoding.
    String constantUtf8(int index) {
        int offset = constantPoolIndexToOffset(
            index, Constants.CONSTANT_Utf8);

        // Check if this string has already been processed.
        {
            Object candidate = constantPoolObjects[index - 1];
            if (candidate != null)
                return (String) candidate;
        }

        int bytes = unsignedShortAt(offset);
        int startOffset = offset + 2;
        int endOffset = startOffset + bytes;

        // Check if the string is a well-known string that is
        // pre-allocated.
        {
            String candidate = STRING_INTERNER.lookup(
                buffer, startOffset, endOffset);
            if (candidate != null) {
                constantPoolObjects[index - 1] = candidate;
                return candidate;
            }
        }

        int resultLength = 0;
        for (int i = offset + 2; i < endOffset; ++i) {
            int mask = buffer.get(i) & 0b1100_0000;
            if (mask != 0b1000_0000)
                // Not a follower byte.
                ++resultLength;
        }
        char[] result = new char[resultLength];
        int resultPos = 0;
        // FIXME: This could have more validation (string length,
        // bit pattern of follower bytes).
        for (int i = offset + 2; i < endOffset; ) {
            byte b = buffer.get(i);
            if (b > 0) {
                result[resultPos] = (char) b;
                ++i;
            } else if ((b & 0b1110_0000) == 0b1100_0000) {
                // Two bytes.
                byte b2 = buffer.get(i + 1);
                result[resultPos] = (char) (((b & 0x1f) << 6) + (b2 & 0x3f));
                i += 2;
            } else if ((b & 0b1111_0000) == 0b1110_0000) {
                // Three bytes.
                byte b2 = buffer.get(i + 1);
                byte b3 = buffer.get(i + 2);
                result[resultPos] = (char) (((b & 0xf) << 12)
                    + ((b2 & 0x3f) << 6) + (b3 & 0x3f));
                i += 3;
            } else
                throw new ClassFileException(String.format(
                        "Invalid UTF-8 byte 0x%02x at offset %d for index %d"
                        + " (start %d, length %d)",
                        Byte.toUnsignedInt(b), i, index, offset + 2, bytes));
            ++resultPos;
        }
        String finalResult = String.valueOf(result);
        constantPoolObjects[index - 1] = finalResult;
        return finalResult;
    }

    String constantUtf8At(int offset) {
        return constantUtf8(unsignedShortAt(offset));
    }

    Object constantObjectAt(int offset) {
        int index = unsignedShortAt(offset);
        if (index < 1)
            throw new ClassFileException(
                "Invalid constant pool index " + index
                + " at offset " + offset);
        if (index - 1 > constantPoolOffsets.length)
            throw new ClassFileException(String.format(
                    "Constant pool index %d out of range (larger than %d)",
                    index, constantPoolOffsets.length));

        int poolOffset = constantPoolOffsets[index - 1];
        if (poolOffset < 0)
            // Second half of long or double constants.
            throw new ClassFileException(
                    "Constant pool index " + index + " unusable");
        int typeAtOffset = Byte.toUnsignedInt(buffer.get(poolOffset));

        // Check if the object has been already decoded.
        {
            Object candidate = constantPoolObjects[index - 1];
            if (candidate != null)
                switch (typeAtOffset) {
                case Constants.CONSTANT_Integer:
                case Constants.CONSTANT_Float:
                case Constants.CONSTANT_Double:
                case Constants.CONSTANT_String:
                    return candidate;
                }
            // Code below handles new allocation and error reporting.
        }

        Object result;
        switch (typeAtOffset) {
        case Constants.CONSTANT_Integer:
            result = intAt(poolOffset + 1);
            break;
        case Constants.CONSTANT_Float:
            result = buffer.getFloat(poolOffset + 1);
            break;
        case Constants.CONSTANT_Double:
            result = buffer.getDouble(poolOffset + 1);
            break;
        case Constants.CONSTANT_Long:
            result = buffer.getLong(poolOffset + 1);
            break;
        case Constants.CONSTANT_String:
            return constantUtf8At(poolOffset + 1);
        default:
            throw new ClassFileException(String.format(
                    "Constant pool index %d (from offset %d)"
                    + " has non-constant type %d",
                    index, offset, typeAtOffset));
        }
        constantPoolObjects[index - 1] = result;
        return result;
    }

    int constantIntegerAt(int offset) {
        return intAt(constantPoolIndexToOffset(
            unsignedShortAt(offset), Constants.CONSTANT_Integer));
    }

    float constantFloatAt(int offset) {
        return buffer.getFloat(constantPoolIndexToOffset(
            unsignedShortAt(offset), Constants.CONSTANT_Float));
    }

    double constantDoubleAt(int offset) {
        return buffer.getDouble(constantPoolIndexToOffset(
            unsignedShortAt(offset), Constants.CONSTANT_Double));
    }

    long constantLongAt(int offset) {
        return buffer.getLong(constantPoolIndexToOffset(
            unsignedShortAt(offset), Constants.CONSTANT_Long));
    }
}
