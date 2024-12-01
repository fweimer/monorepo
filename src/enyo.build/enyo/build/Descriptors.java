package enyo.build;

import enyo.testing.Cost;
import enyo.testing.ExitDuringTest;
import enyo.testing.OnlyTests;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.TestModule;
import enyo.testing.Testing;

import java.lang.constant.ClassDesc;

/**
 * Descriptors for test-related types.
 */
final class Descriptors {
    final static ClassDesc Cost = Cost.class.describeConstable().get();
    final static ClassDesc ExitDuringTest =
        ExitDuringTest.class.describeConstable().get();
    final static ClassDesc OnlyTests =
        OnlyTests.class.describeConstable().get();
    final static ClassDesc Resources =
        Resources.class.describeConstable().get();
    final static ClassDesc Test = Test.class.describeConstable().get();
    final static ClassDesc Testing = Testing.class.describeConstable().get();
    final static ClassDesc TestModule =
        TestModule.class.describeConstable().get();

    private Descriptors() { } // No instances.
}
