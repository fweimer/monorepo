package enyo.util.argparse.tests;

import enyo.util.argparse.ArgParser;
import enyo.util.argparse.ConfigurationException;
import enyo.util.argparse.Default;
import enyo.util.argparse.Help;
import enyo.util.argparse.MetaVar;
import enyo.util.argparse.Name;
import enyo.util.argparse.Positional;

import enyo.testing.Test;
import enyo.testing.Testing;

import static java.lang.invoke.MethodHandles.lookup;

public final class TestBadConfiguration {
    // Avoid nested records for predictable naming.
    record Unsupported(TestBadConfiguration unsupported) {}

    @Test
    public static void testUnsupportedType(Testing t) {
        expect(t, Unsupported.class,
            "Field unsupported of class enyo.util.argparse.tests.TestBadConfiguration$Unsupported has unsupported type class enyo.util.argparse.tests.TestBadConfiguration");
    }

    record OptionalPositionalBeforeRequired(
        @Positional String optional, int required) {}

    @Test
    public static void testOptionalPositionalBeforeRequired(Testing t) {
        expect(t, OptionalPositionalBeforeRequired.class,
            "Required positional field required in class enyo.util.argparse.tests.TestBadConfiguration$OptionalPositionalBeforeRequired after optional field optional");
    }

    record SameFieldName1(int first, @Name("--first") int second) {}
    record SameFieldName2(@
        Name("--first") int initial, @Name("--first") int second) {}

    @Test
    public static void testSameFieldName(Testing t) {
        expect(t, SameFieldName1.class,
            "Fields first and second of class enyo.util.argparse.tests.TestBadConfiguration$SameFieldName1 have the same option name --first");
        expect(t, SameFieldName2.class,
            "Fields initial and second of class enyo.util.argparse.tests.TestBadConfiguration$SameFieldName2 have the same option name --first");
    }

    record PositionalWithDash(@Name("--option") @Positional int positional) {}

    @Test
    public static void testPositionalWithDash(Testing t) {
        expect(t, PositionalWithDash.class,
            "Positional argument field positional of class enyo.util.argparse.tests.TestBadConfiguration$PositionalWithDash has name starting with '-': --option");
    }

    record NonPositionalWithoutDash(@Name("non-option") int option) {}

    @Test
    public static void testNonPositionaloutWithDash(Testing t) {
        expect(t, NonPositionalWithoutDash.class,
            "Option field option of class enyo.util.argparse.tests.TestBadConfiguration$NonPositionalWithoutDash has name not starting with '-': non-option");
    }

    record EmptyName(@Name("") int positional) {}
    record EmptyNamePositional(@Name("") @Positional int positional) {}

    @Test
    public static void testEmptyName(Testing t) {
        expect(t, EmptyName.class,
            "Option field positional of class enyo.util.argparse.tests.TestBadConfiguration$EmptyName has has empty name");
        expect(t, EmptyNamePositional.class,
            "Positional argument field positional of class enyo.util.argparse.tests.TestBadConfiguration$EmptyNamePositional has empty name");
    }

    record EmptyHelp(@Help("") int option) {}

    @Test
    public static void testEmptyHelp(Testing t) {
        expect(t, EmptyHelp.class,
            "Field option of class enyo.util.argparse.tests.TestBadConfiguration$EmptyHelp has empty help string");
    }

    record PositionalMetaVar(@MetaVar("VAL") @Positional int positional) {}

    @Test
    public static void testPositionalMetaVar(Testing t) {
        expect(t, PositionalMetaVar.class,
            "Positional argument field positional of class enyo.util.argparse.tests.TestBadConfiguration$PositionalMetaVar has meta-variable name: VAL");
    }

    record BadDefault(@Default("x") int value) {}

    @Test
    public static void testBadDefault(Testing t) {
        expect(t, BadDefault.class,
            "Field value of class enyo.util.argparse.tests.TestBadConfiguration$BadDefault has invalid default value: x");
    }

    record BooleanPositional(@Positional boolean flag) {}


    @Test
    public static void testBooleanPositional(Testing t) {
        expect(t, BooleanPositional.class,
            "Positional argument field flag of class enyo.util.argparse.tests.TestBadConfiguration$BooleanPositional has type boolean");
    }

    private static void expect(
        Testing t, Class<? extends Record> cls, String message) {
        t.expect(() -> new ArgParser<>(lookup(), cls),
            ConfigurationException.class)
	    .map(Exception::getMessage)
            .is(message);
    }
}
