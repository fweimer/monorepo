package enyo.build;

import enyo.lang.classfile.Descriptor;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.TestModule;

/**
 * Descriptors for test-related types.
 */
final class Descriptors {
    final static String Test = Descriptor.descriptor(Test.class);
    final static String Testing = Descriptor.descriptor(Testing.class);
    final static String TestModule = Descriptor.descriptor(TestModule.class);

    private Descriptors() { } // No instances.
}
