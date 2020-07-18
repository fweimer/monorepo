import enyo.testing.TestModule;

@TestModule
module enyo.core.tests {
    exports enyo.io.function.tests;
    exports enyo.io.tests;
    exports enyo.lang.classfile.tests;
    exports enyo.testing.tests;
    exports enyo.util.tests;

    requires transitive enyo.core;
    requires transitive enyo.testing;
}
