package enyo.lang.classfile;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class members (methods and fields).
 */
public abstract class Member extends SizedCached implements Annotated {
    /**
     * Returns the access flags for this class member.
     * @return the {@code access_flags} of the {@code field_info}
     *  or {@code method_info} structure
     */
    public final int accessFlags() {
        return classFile.unsignedShortAt(offset);
    }

    /**
     * Returns the binary name of this member.
     * @return the name, a string; derived from the {@code name_index}
     *   member of the {@code field_info} or {@code method_info} structure
     */
    public final String name() {
        return classFile.constantUtf8At(offset + 2);
    }

    /**
     * Returns the descriptor for this member.  In case of a method,
     * the descriptor describes the method type (the type of its
     * arguments and its return type).  For a field, this is the type
     * of the field.
     * @return the descriptor, a string; derived from the {@code name_index}
     *   member of the {@code field_info} or {@code method_info} structure
     */
    public final String descriptor() {
        return classFile.constantUtf8At(offset + 4);
    }

    /**
     * Returns a stream of attributes for this class member.
     * @return a stream of attributes derived from the {@code attributes}
     *   member of the {@code field_info} or {@code method_info} structure
     */
    public final Stream<Attribute> attributes() {
        return StreamSupport.stream(
            new AttributeSpliterator(classFile, offset + 6), false);
    }

    /**
     * Returns a stream of annotations directly on this member
     * declaration.
     * @return a stream of annotations; derived from the annotations
     *   among the {@linkplain #attributes() attributes}
     */
    public final Stream<Annotation> annotations() {
        return AnnotationsAttribute.asAnnotationStream(attributes());
    }

    Member(ClassFile classFile, int offset) {
        super(classFile, offset);
        if (false)
        System.out.format("Member at %d: %s%n", offset, name());
    }

    final int computeSize() {
        return Attribute.skipArray(classFile, offset + 6) - offset;
    }

    final String toString(String type) {
        return type + ' ' + name() + ' ' + descriptor();
    }
}
