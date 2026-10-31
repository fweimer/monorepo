package enyo.util.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import enyo.util.OpenEnum;

import java.lang.invoke.MethodHandles;

public final class TestOpenEnum {
    public interface ElfType extends OpenEnum.OfInt {
        enum E implements ElfType, OpenEnum.OfInt.Direct {
            ET_NONE,
            ET_REL,
            ET_EXEC,
            ET_DYN,
            ET_CORE,
        }
        static final OpenEnum.OfInt.Type<ElfType> T =
            OpenEnum.predicatedIntType(MethodHandles.lookup(),
                ElfType.class, E.class, i -> i >= 0 && i <= 65535);

    }

    @Test
    @SuppressWarnings({"raw", "unchecked"})
    public static void testTypes(Testing t) {
        t.examine(ElfType.T.baseType()).isSame((Class) ElfType.class);
        t.examine(ElfType.T.enumType()).isSame((Class) ElfType.E.class);
        t.examine(ElfType.T.openType())
            .isnt((Class) ElfType.class)
            .isnt((Class) ElfType.E.class);
        t.assertTrue(ElfType.T.openType().isHidden());
        t.assertTrue(ElfType.T.baseType().isAssignableFrom(ElfType.T.enumType()));
        t.assertTrue(ElfType.T.baseType().isAssignableFrom(ElfType.T.openType()));
        t.examine(ElfType.T.ofInt(0))
            .map(Object::getClass)
            .isSameObject(ElfType.T.enumType());
        t.examine(ElfType.T.ofInt(5))
            .map(Object::getClass)
            .isSameObject(ElfType.T.openType());
    }

    @Test
    public static void testValue(Testing t) {
        t.examine(ElfType.E.ET_NONE).mapToLong(ElfType::value).is(0);
        t.examine(ElfType.E.ET_REL).mapToLong(ElfType::value).is(1);
        t.examine(ElfType.E.ET_EXEC).mapToLong(ElfType::value).is(2);
        t.examine(ElfType.E.ET_DYN).mapToLong(ElfType::value).is(3);
        t.examine(ElfType.E.ET_CORE).mapToLong(ElfType::value).is(4);
    }

    @Test
    public static void testName(Testing t) {
        t.examine(ElfType.E.ET_NONE).map(ElfType::name).is("ET_NONE");
        t.examine(ElfType.E.ET_REL).map(ElfType::name).is("ET_REL");
        t.examine(ElfType.E.ET_EXEC).map(ElfType::name).is("ET_EXEC");
        t.examine(ElfType.E.ET_DYN).map(ElfType::name).is("ET_DYN");
        t.examine(ElfType.E.ET_CORE).map(ElfType::name).is("ET_CORE");
        t.examine(ElfType.T.ofInt(5)).map(ElfType::name).isNull();
        t.examine(ElfType.T.name(0)).is("ET_NONE");
        t.examine(ElfType.T.name(5)).isNull();
    }

    @Test
    public static void testToString(Testing t) {
        t.examine(ElfType.E.ET_NONE).map(ElfType::toString).is("ET_NONE");
        t.examine(ElfType.E.ET_REL).map(ElfType::toString).is("ET_REL");
        t.examine(ElfType.E.ET_EXEC).map(ElfType::toString).is("ET_EXEC");
        t.examine(ElfType.E.ET_DYN).map(ElfType::toString).is("ET_DYN");
        t.examine(ElfType.E.ET_CORE).map(ElfType::toString).is("ET_CORE");
        t.examine(ElfType.T.ofInt(5)).map(ElfType::toString).is("5");
        t.examine(ElfType.T.toString(0)).is("ET_NONE");
        t.examine(ElfType.T.toString(5)).is("5");
    }

    @Test
    public static void testGetInt(Testing t) {
        t.examine(ElfType.T.ofInt(0)).isSame(ElfType.E.ET_NONE);
        t.examine(ElfType.T.ofInt(1)).isSame(ElfType.E.ET_REL);
        t.examine(ElfType.T.ofInt(2)).isSame(ElfType.E.ET_EXEC);
        t.examine(ElfType.T.ofInt(3)).isSame(ElfType.E.ET_DYN);
        t.examine(ElfType.T.ofInt(4)).isSame(ElfType.E.ET_CORE);
        t.examine(ElfType.T.ofInt(5)).is(ElfType.T.ofInt(5));
        t.examine(ElfType.T.ofInt(65535)).is(ElfType.T.ofInt(65535));
        t.expect(() -> ElfType.T.ofInt(-1), IllegalArgumentException.class);
        t.expect(() -> ElfType.T.ofInt(65536), IllegalArgumentException.class);
    }

    @Test
    public static void testGetString(Testing t) {
        t.examine(ElfType.T.of("ET_NONE")).isSame(ElfType.E.ET_NONE);
        t.examine(ElfType.T.of("ET_REL")).isSame(ElfType.E.ET_REL);
        t.examine(ElfType.T.of("ET_EXEC")).isSame(ElfType.E.ET_EXEC);
        t.examine(ElfType.T.of("ET_DYN")).isSame(ElfType.E.ET_DYN);
        t.examine(ElfType.T.of("ET_CORE")).isSame(ElfType.E.ET_CORE);
        t.examine(ElfType.T.of("0")).isSame(ElfType.E.ET_NONE);
        t.examine(ElfType.T.of("1")).isSame(ElfType.E.ET_REL);
        t.examine(ElfType.T.of("2")).isSame(ElfType.E.ET_EXEC);
        t.examine(ElfType.T.of("3")).isSame(ElfType.E.ET_DYN);
        t.examine(ElfType.T.of("4")).isSame(ElfType.E.ET_CORE);
        t.examine(ElfType.T.of("+1")).isSame(ElfType.E.ET_REL);
        t.expect(() -> ElfType.T.of("0x+1"), IllegalArgumentException.class);
        t.expect(() -> ElfType.T.of("-1"), IllegalArgumentException.class);
        t.expect(() -> ElfType.T.of("65536"), IllegalArgumentException.class);
    }

    @Test
    public static void testOrdering(Testing t) {
        var order = new Order<ElfType>();
        order.group()
            .add(ElfType.E.ET_NONE)
            .add(ElfType.T.ofInt(0))
            .add(ElfType.T.ofInt(0))
            .close();
        order.group()
            .add(ElfType.E.ET_REL)
            .add(ElfType.T.ofInt(1))
            .add(ElfType.T.ofInt(1))
            .close();
        order.group()
            .add(ElfType.E.ET_EXEC)
            .add(ElfType.T.ofInt(2))
            .add(ElfType.T.ofInt(2))
            .close();
        order.group()
            .add(ElfType.E.ET_DYN)
            .add(ElfType.T.ofInt(3))
            .add(ElfType.T.ofInt(3))
            .close();
        order.group()
            .add(ElfType.E.ET_CORE)
            .add(ElfType.T.ofInt(4))
            .add(ElfType.T.ofInt(4))
            .close();
        order.group()
            .add(ElfType.T.ofInt(5))
            .add(ElfType.T.ofInt(5))
            .close();
        order.check(ElfType.T.comparator());
    }

    @Test
    public void testDuplicate(Testing t) {
        interface T extends OpenEnum.OfInt {
            enum E implements T {
                A, B;
                @Override
                public int value() {
                    return 1;
                }
            }
        }
        t.expect(() -> OpenEnum.intType(MethodHandles.lookup().in(T.class),
                T.class, T.E.class),
            IllegalArgumentException.class)
            .map(Throwable::getMessage)
            .is("Enum constants A and B have same value 1");
    }
}
