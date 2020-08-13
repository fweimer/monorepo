/**
 * Runtime components for the Enyo testing framework.
 *
 * Tests do not require this module.  They should require the
 * <a href="{@docRoot}/enyo.testing/module-summary.html">{@code enyo.testing}</a>
 * module instead.
 */
module enyo.testing.runtime {
    exports enyo.testing.runtime;

    requires transitive enyo.core;
    requires transitive enyo.testing;
}
