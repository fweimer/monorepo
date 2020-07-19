package enyo.lang.classfile;

import java.util.Set;

/**
 * Constants found in class files.
 */
public final class Constants {
    /**
     * Corresponds to the {@code public} keyword.
     */
    public static final int ACC_PUBLIC = 1;

    /**
     * Corresponds to the {@code private} keyword.
     */
    public static final int ACC_PRIVATE = 2;

    /**
     * Corresponds to the {@code protected} keyword.
     */
    public static final int ACC_PROTECTED = 4;

    /**
     * Corresponds to the {@code static} keyword.
     */
    public static final int ACC_STATIC = 8;

    /**
     * Corresponds to the {@code final} keyword.
     */
    public static final int ACC_FINAL = 16;

    /**
     * Historic constant, always set on classes.
     */
    public static final int ACC_SUPER = 32;

    /**
     * Corresponds to the {@code synchronized} keyword.
     */
    public static final int ACC_SYNCHRONIZED = 32;

    /**
     * Corresponds to the {@code volatile} keyword.
     */
    public static final int ACC_VOLATILE = 64;

    /**
     * Marks a compiler-generated bridge method.
     */
    public static final int ACC_BRIDGE = 64;

    /**
     * Corresponds to the {@code transient} keyword.
     */
    public static final int ACC_TRANSIENT = 128;


    /**
     * Marks a method as a varargs method.
     */
    public static final int ACC_VARARGS = 128;

    /**
     * Corresponds to the {@code native} keyword.
     */
    public static final int ACC_NATIVE = 256;

    /**
     * Used to mark interface types.
     */
    public static final int ACC_INTERFACE = 512;

    /**
     * Corresponds to the {@code abstract} keyword.
     */
    public static final int ACC_ABSTRACT = 1024;

    /**
     * Corresponds to the {@code strictfp} keyword.
     */
    public static final int ACC_STRICT = 2048;

    /**
     * Indicates that a construct was created by the compiler.  The
     * construct was not written by the program explicitly in the
     * source code.
     */
    public static final int ACC_SYNTHETIC = 4096;

    /**
     * Used to mark annotation types.
     */
    public static final int ACC_ANNOTATION = 8192;

    /**
     * Used to mark enum types.
     */
    public static final int ACC_ENUM = 16384;

    /**
     * Used to mark module types.
     */
    public static final int ACC_MODULE = 32768;

    /**
     * A constant pool entry containing a string in modified UTF-8
     * encoding.
     */
    public static final int CONSTANT_Utf8 = 1;

    /**
     * A constant pool entry containing a 32-bit signed integer.
     */
    public static final int CONSTANT_Integer = 3;

    /**
     * A constant pool entry containing a 32-bit floating point value.
     */
    public static final int CONSTANT_Float = 4;

    /**
     * A constant pool entry containing a 64-bit signed integer.
     */
    public static final int CONSTANT_Long = 5;

    /**
     * A constant pool entry containing a 64-bit floating point value.
     */
    public static final int CONSTANT_Double = 6;

    /**
     * A constant pool entry containing the index of a {@link #CONSTANT_Utf8}
     * entry with the class name.
     */
    public static final int CONSTANT_Class = 7;

    /**
     * A constant pool entry containing the index of a {@link #CONSTANT_Utf8}
     * entry with the string contents.
     */
    public static final int CONSTANT_String = 8;

    /**
     * A constant pool entry containing a field reference.
     */
    public static final int CONSTANT_Fieldref = 9;

    /**
     * A constant pool entry containing a reference to a class method.
     */
    public static final int CONSTANT_Methodref = 10;

    /**
     * A constant pool entry containing a reference to an interface method.
     */
    public static final int CONSTANT_InterfaceMethodref = 11;

    /**
     * A constant pool entry containing a name a long with a type.
     */
    public static final int CONSTANT_NameAndType = 12;

    /**
     * A constant pool entry containing method handle information.
     */
    public static final int CONSTANT_MethodHandle = 15;

    /**
     * A constant pool entry containing the index of a {@link #CONSTANT_Utf8}
     * entry with the descriptor string.
     */
    public static final int CONSTANT_MethodType = 16;

    /**
     * A constant pool entry describing how to compute this constant
     * at run time.
     */
    public static final int CONSTANT_Dynamic = 17;

    /**
     * A constant pool entry supporting the {@code invokedynamic}
     * bytecode instruction.
     */
    public static final int CONSTANT_InvokeDynamic = 18;

    /**
     * A constant pool entry containing the index of a {@link #CONSTANT_Utf8}
     * entry with the module name.
     */
    public static final int CONSTANT_Module = 19;

    /**
     * A constant pool entry containing the index of a {@link #CONSTANT_Utf8}
     * entry with the package name.
     */
    public static final int CONSTANT_Package = 20;

    static final String STRING_AnnotationDefault = "AnnotationDefault";
    static final String STRING_BootstrapMethods = "BootstrapMethods";
    static final String STRING_Code = "Code";
    static final String STRING_ConstantValue = "ConstantValue";
    static final String STRING_Deprecated = "Deprecated";
    static final String STRING_EnclosingMethod = "EnclosingMethod";
    static final String STRING_Exceptions = "Exceptions";
    static final String STRING_InnerClasses = "InnerClasses";
    static final String STRING_LineNumberTable = "LineNumberTable";
    static final String STRING_LocalVariableTable =
        "LocalVariableTable";
    static final String STRING_LocalVariableTypeTable =
        "LocalVariableTypeTable";
    static final String STRING_MethodParameters = "MethodParameters";
    static final String STRING_Module = "Module";
    static final String STRING_ModuleMainClass = "ModuleMainClass";
    static final String STRING_ModulePackages = "ModulePackages";
    static final String STRING_NestHost = "NestHost";
    static final String STRING_NestMembers = "NestMembers";
    static final String STRING_RuntimeInvisibleAnnotations =
        "RuntimeInvisibleAnnotations";
    static final String STRING_RuntimeInvisibleParameterAnnotations =
        "RuntimeInvisibleParameterAnnotations";
    static final String STRING_RuntimeInvisibleTypeAnnotations =
        "RuntimeInvisibleTypeAnnotations";
    static final String STRING_RuntimeVisibleAnnotations =
        "RuntimeVisibleAnnotations";
    static final String STRING_RuntimeVisibleParameterAnnotations =
        "RuntimeVisibleParameterAnnotations";
    static final String STRING_RuntimeVisibleTypeAnnotations =
        "RuntimeVisibleTypeAnnotations";
    static final String STRING_Signature = "Signature";
    static final String STRING_SourceDebugExtension =
        "SourceDebugExtension";
    static final String STRING_SourceFile = "SourceFile";
    static final String STRING_StackMapTable = "StackMapTable";
    static final String STRING_Synthetic = "Synthetic";

    static final Set<String> ANNOTATION_STRINGS = Set.of(
        STRING_RuntimeInvisibleAnnotations,
        STRING_RuntimeInvisibleParameterAnnotations,
        STRING_RuntimeInvisibleTypeAnnotations,
        STRING_RuntimeVisibleAnnotations,
        STRING_RuntimeVisibleParameterAnnotations,
        STRING_RuntimeVisibleTypeAnnotations);

    private Constants() { } // No instances.
}
