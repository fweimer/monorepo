package enyo.util;

import java.lang.invoke.MethodHandle;
import java.lang.classfile.ClassFile;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.constant.MethodTypeDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.ClassDesc;

class OpenEnumImpl {
    private OpenEnumImpl() { } // No instances.

    static MethodHandle constructor(MethodHandles.Lookup lookup,
        Class<?> interfaceType, Class<?> intType, Class<?> intBoxed) {
        ClassDesc CD_this = ClassDesc.of(interfaceType.getName() + "$Value");
        ClassDesc CD_base = interfaceType.describeConstable()
            .orElseThrow(() -> new IllegalArgumentException(
                    "Cannot create symbolic reference to " + interfaceType));

        MethodType MT_intType = MethodType.methodType(intType);
        ClassDesc CD_intType = intType.describeConstable().get();
        ClassDesc CD_intBoxed = intBoxed.describeConstable().get();
        MethodType MT_init = MethodType.methodType(void.class, intType);
        MethodTypeDesc MTD_init = MT_init.describeConstable().get();
        MethodTypeDesc MTD_value =
            MethodType.methodType(intType).describeConstable().get();
        MethodTypeDesc MTD_int_toString = MethodType.methodType(
            String.class, intType).describeConstable().get();
        MethodTypeDesc MTD_int_hashCode = MethodType.methodType(
            int.class, intType).describeConstable().get();
        byte[] classBlob = ClassFile.of().build(CD_this, clb -> clb
            .withInterfaceSymbols(CD_base)
            .withField("value", ConstantDescs.CD_int, fib -> fib
                .withFlags(ClassFile.ACC_PRIVATE))
            .withMethodBody(ConstantDescs.INIT_NAME, MTD_init,
                ClassFile.ACC_PRIVATE, cob -> cob
                .aload(0)
                .invokespecial(ConstantDescs.CD_Object,
                    ConstantDescs.INIT_NAME, ConstantDescs.MTD_void)
                .aload(0)
                .iload(1)
                .putfield(CD_this, "value", CD_intType)
                .return_())
            .withMethodBody("value", MTD_value, ClassFile.ACC_PUBLIC, cob -> cob
                .aload(0)
                .getfield(CD_this, "value", CD_intType)
                .return_(TypeKind.from(CD_intType)))
            .withMethodBody("equals", MTD_OpenEnum_equals, ClassFile.ACC_PUBLIC,
                cob -> {
                    Label lblFalse = cob.newLabel();
                    cob
                        .aload(1)
                        .instanceOf(CD_this)
                        .ifeq(lblFalse)
                        .aload(1)
                        .checkcast(CD_this)
                        .getfield(CD_this, "value", CD_intType)
                        .aload(0)
                        .getfield(CD_this, "value", CD_intType);
                    if (intType != long.class)
                        cob.if_icmpne(lblFalse);
                    else
                        cob
                            .lcmp()
                            .ifne(lblFalse);
                    cob
                        .iconst_1()
                        .return_(TypeKind.BOOLEAN)
                        .labelBinding(lblFalse)
                        .iconst_0()
                        .return_(TypeKind.BOOLEAN);
                })
            .withMethodBody("hashCode", MTD_OpenEnum_hashCode, ClassFile.ACC_PUBLIC,
                cob -> {
                    cob
                        .aload(0)
                        .getfield(CD_this, "value", CD_intType);
                    if (intType == long.class)
                        cob.invokestatic(CD_intBoxed, "hashCode",
                            MTD_int_hashCode);
                    cob.return_(TypeKind.from(ConstantDescs.CD_int));
                })
            // Always return null because this object is only constructed
            // when there is no name.
            .withMethodBody("name", MTD_OpenEnum_toString, ClassFile.ACC_PUBLIC,
                cob -> cob
                .aconst_null()
                .return_(TypeKind.REFERENCE))
            .withMethodBody("toString", MTD_OpenEnum_toString, ClassFile.ACC_PUBLIC, cob -> cob
                .aload(0)
                .getfield(CD_this, "value", CD_intType)
                .invokestatic(CD_intBoxed, "toString", MTD_int_toString)
                .return_(TypeKind.REFERENCE)));
        try {
            var newLookup = lookup.defineHiddenClass(classBlob, true);
            return newLookup.findConstructor(newLookup.lookupClass(), MT_init);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new InternalError(e);
        }
    }

    private static final MethodTypeDesc MTD_OpenEnum_equals =
        MethodType.methodType(boolean.class, Object.class).describeConstable().get();
    private static final MethodTypeDesc MTD_OpenEnum_hashCode =
        MethodType.methodType(int.class).describeConstable().get();
    private static final MethodTypeDesc MTD_OpenEnum_toString =
        MethodType.methodType(String.class).describeConstable().get();
}
