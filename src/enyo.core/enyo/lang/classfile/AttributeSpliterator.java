package enyo.lang.classfile;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.Objects;

final class AttributeSpliterator extends SizedSpliterator<Attribute> {
    AttributeSpliterator(ClassFile classFile, int offset) {
        super(classFile, offset);
    }

    @Override
    Attribute create(int offset) {
        switch (classFile.constantUtf8At(offset)) {
        case Constants.STRING_AnnotationDefault:
            return new AnnotationDefaultAttribute(classFile, offset);
        case Constants.STRING_Code:
            return new CodeAttribute(classFile, offset);
        case Constants.STRING_ConstantValue:
            return new ConstantValueAttribute(classFile, offset);
        case Constants.STRING_RuntimeInvisibleAnnotations:
        case Constants.STRING_RuntimeVisibleAnnotations:
            return new AnnotationsAttribute(classFile, offset);
        case Constants.STRING_SourceFile:
            return new SourceFileAttribute(classFile, offset);
        default:
            return new OtherAttribute(classFile, offset);
        }
    }

    @Override
    int skip(int offset) {
        return 6 + classFile.intAt(offset + 2);
    }
}
