/**
 * The Enyo build automation tool.
 *
 * The entry point for performing build cycles is {@link enyo.build.Main}.
 * A test suite can be run using {@link enyo.build.RunTests}.
 */
module enyo.build {
    exports enyo.build;

    requires enyo.core;
    requires enyo.testing;
    requires enyo.testing.runtime;
    requires java.compiler;
}
