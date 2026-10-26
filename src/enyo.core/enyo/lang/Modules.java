package enyo.lang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.module.ModuleReference;
import java.nio.ByteBuffer;

/**
 * Facilities for processing JDK modules.
 */
public final class Modules {
    private Modules() {} // No instances.

    /**
     * Returns true if the referenced module needs native access.
     *
     * A module needs native access if it has a {@link NeedsNativeAccess}
     * annotation.
     * @param ref the module to check
     * @return whether the specified module needs native access
     * @throws NullPointerException {@code ref} is null
     * @see NeedsNativeAccess
     */
    public static boolean needsNativeAccess(ModuleReference ref) {
        byte[] blob;
        try (var reader = ref.open();
            var in = reader.open("module-info.class").orElseThrow(
                () -> {
                    String name = ref.descriptor().name();
                    String location = ref.location()
                        .map(s -> " <" + s + ">")
                        .orElse("");
                    return new IOException(String.format(
                            "Module descriptor for %s%s not found",
                            name, location));
                })) {
            blob = in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        var cls = ClassFile.of().parse(blob);
        var expectedName =
            NeedsNativeAccess.class.describeConstable().orElseThrow();
        return cls.findAttribute(Attributes.runtimeInvisibleAnnotations())
            .map(attr -> attr.annotations().stream()
                .anyMatch(ann -> ann.classSymbol().equals(expectedName)))
            .orElse(false);
    }
}
