package enyo.linux.alsa.tests;

import enyo.linux.Errno;
import enyo.linux.SyscallException;
import enyo.linux.alsa.Control;

import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.List;

public final class TestControl {

    public static void testAvailableControls(Testing t) {
        List<Integer> available = Control.availableControls();
        if (available.isEmpty()) {
            t.unsupported("no sound controls available");
            return;
        }

        for (int number : available)
            try (Control control = new Control(number)) {
                t.examine(control.number()).is(number);
                t.examine(control.version())
                    .check(Checks.greaterThanOrEquals(0x20006));

                Control.CardInfo info = control.info();
                t.examine(info.card()).is(number);
                t.examine(info.name()).testNot(String::isEmpty);
                t.examine(info.longName()).testNot(String::isEmpty);

                for (Control.Element element : control.elements()) {
                    t.examine(element.id())
                        .check(Checks.greaterThan(0));
                    t.examine(element.interfaceType())
                        .isExactClass(Control.Element.Interface.E.class);
                    t.examine(element.name()).testNot(String::isEmpty);

                    t.examine(element)
                        .map(control::elementInfo)
                        .inspect(info1 -> elementInfo(t, control, info1));
                }
            }
    }

    private static void elementInfo(Testing t, Control control,
        Control.ElementInfo info) {
        if (info.type() instanceof Control.ElementInfo.Type.E type) {
            switch (type) {
            case INTEGER:
            case INTEGER64:
            case ENUMERATED:
            case BOOLEAN:
                t.examine(info.min())
                    .check(Checks.lessThan(info.max()));
            }

            switch (type) {
            case INTEGER:
            case INTEGER64:
                long[] values = control.longValues(info);
                t.examine(values)
                    .mapToInt(v -> v.length,
                        e -> e.is(info.count()));
                for (long val : values)
                    t.examine(val)
                        .check(Checks.inRangeClosed(
                                info.min(), info.max()));
                break;
            }
        }
    }

    @Test
    public static void testInvalidControl(Testing t) {
        t.expect(() -> new Control(-1), IllegalArgumentException.class);
        t.expect(() -> new Control(Integer.MAX_VALUE),
            SyscallException.class)
            .test(
                c -> List.of(Errno.EACCES,
                    Errno.ENOENT,
                    Errno.EPERM)
                .contains(c.errno()));
    }

    @Test
    public static void testElementInfoAccess(Testing t) {
        // Check that the unused bits are at the expected positions.
        var values = Control.ElementInfo.Access.values();
        t.examine(values.length).is(32);
        boolean found = false;
        for (int i = 0; i < 32; ++i) {
            Control.ElementInfo.Access value;
            try {
                value = Control.ElementInfo.Access.valueOf("UNUSED_BIT_" + i);
            } catch (IllegalArgumentException e) {
                continue;
            }
            found = true;
            t.examine(value)
                .mapToInt(v -> v.ordinal())
                .is(i);
        }
        t.assertTrue(found);
    }
}
