package enyo.build;

import enyo.lang.classfile.Descriptor;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.TestModule;
import enyo.testing.Testing;

/**
 * Descriptors for test-related types.
 */
final class Descriptors {
    final static String Resources = Descriptor.descriptor(Resources.class);
    final static String Test = Descriptor.descriptor(Test.class);
    final static String Testing = Descriptor.descriptor(Testing.class);
    final static String TestModule = Descriptor.descriptor(TestModule.class);

    private Descriptors() { } // No instances.
}
