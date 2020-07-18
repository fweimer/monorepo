package enyo.lang.classfile;

import java.util.Set;

final class Constants {

    public static final int CONSTANT_Utf8 = 1;
    public static final int CONSTANT_Integer = 3;
    public static final int CONSTANT_Float = 4;
    public static final int CONSTANT_Long = 5;
    public static final int CONSTANT_Double = 6;
    public static final int CONSTANT_Class = 7;
    public static final int CONSTANT_String = 8;
    public static final int CONSTANT_Fieldref = 9;
    public static final int CONSTANT_Methodref = 10;
    public static final int CONSTANT_InterfaceMethodref = 11;
    public static final int CONSTANT_NameAndType = 12;
    public static final int CONSTANT_MethodHandle = 15;
    public static final int CONSTANT_MethodType = 16;
    public static final int CONSTANT_Dynamic = 17;
    public static final int CONSTANT_InvokeDynamic = 18;
    public static final int CONSTANT_Module = 19;
    public static final int CONSTANT_Package = 20;

    public static final String STRING_AnnotationDefault = "AnnotationDefault";
    public static final String STRING_BootstrapMethods = "BootstrapMethods";
    public static final String STRING_Code = "Code";
    public static final String STRING_ConstantValue = "ConstantValue";
    public static final String STRING_Deprecated = "Deprecated";
    public static final String STRING_EnclosingMethod = "EnclosingMethod";
    public static final String STRING_Exceptions = "Exceptions";
    public static final String STRING_InnerClasses = "InnerClasses";
    public static final String STRING_LineNumberTable = "LineNumberTable";
    public static final String STRING_LocalVariableTable =
        "LocalVariableTable";
    public static final String STRING_LocalVariableTypeTable =
        "LocalVariableTypeTable";
    public static final String STRING_MethodParameters = "MethodParameters";
    public static final String STRING_Module = "Module";
    public static final String STRING_ModuleMainClass = "ModuleMainClass";
    public static final String STRING_ModulePackages = "ModulePackages";
    public static final String STRING_NestHost = "NestHost";
    public static final String STRING_NestMembers = "NestMembers";
    public static final String STRING_RuntimeInvisibleAnnotations =
        "RuntimeInvisibleAnnotations";
    public static final String STRING_RuntimeInvisibleParameterAnnotations =
        "RuntimeInvisibleParameterAnnotations";
    public static final String STRING_RuntimeInvisibleTypeAnnotations =
        "RuntimeInvisibleTypeAnnotations";
    public static final String STRING_RuntimeVisibleAnnotations =
        "RuntimeVisibleAnnotations";
    public static final String STRING_RuntimeVisibleParameterAnnotations =
        "RuntimeVisibleParameterAnnotations";
    public static final String STRING_RuntimeVisibleTypeAnnotations =
        "RuntimeVisibleTypeAnnotations";
    public static final String STRING_Signature = "Signature";
    public static final String STRING_SourceDebugExtension =
        "SourceDebugExtension";
    public static final String STRING_SourceFile = "SourceFile";
    public static final String STRING_StackMapTable = "StackMapTable";
    public static final String STRING_Synthetic = "Synthetic";

    public static final Set<String> ANNOTATION_STRINGS = Set.of(
        STRING_RuntimeInvisibleAnnotations,
        STRING_RuntimeInvisibleParameterAnnotations,
        STRING_RuntimeInvisibleTypeAnnotations,
        STRING_RuntimeVisibleAnnotations,
        STRING_RuntimeVisibleParameterAnnotations,
        STRING_RuntimeVisibleTypeAnnotations);

    private Constants() { } // No instances.
}
