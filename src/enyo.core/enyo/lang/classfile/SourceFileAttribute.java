package enyo.lang.classfile;

/**
 * The source file of a class.
 */
public final class SourceFileAttribute extends Attribute {
    /**
     * Returns the source file name.  Note that the Java compiler does
     * not include any path information in this attribute.
     * @return the name of the file containing the source code
     */
    public String sourceFile() {
        return classFile.constantUtf8At(offset + 6);
    }

    SourceFileAttribute(ClassFile classFile, int offset) {
        super(classFile, offset);
    }
}
