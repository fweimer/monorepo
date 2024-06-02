package enyo.util.tests;

import enyo.testing.Test;
import enyo.testing.Testing;

import enyo.util.OpenEnum;

public final class TestOpenEnum {
    public static record ElfType(long value) implements OpenEnum {
        public ElfType {
            if (value < 0 || value > 65535)
                throw new IllegalArgumentException("value: " + value);
        }

        public static ElfType get(int value) {
            return OpenEnum.get(ElfType.class, value, ElfType::new);
        }

        public static ElfType get(String name) {
            return OpenEnum.get(ElfType.class, name, ElfType::new);
        }

        public static final ElfType ET_NONE = new ElfType(0);
        public static final ElfType ET_REL = new ElfType(1);
        public static final ElfType ET_EXEC = new ElfType(2);
        public static final ElfType ET_DYN = new ElfType(3);
        public static final ElfType ET_CORE = new ElfType(4);

        static {
            OpenEnum.builder(ElfType.class)
                .add("ET_NONE", ET_NONE)
                .add("ET_REL", ET_REL)
                .add("ET_EXEC", ET_EXEC)
                .add("ET_DYN", ET_DYN)
                .add("ET_CORE", ET_CORE)
                .finish();
        }

        @Override
        public String toString() {
            return name();
        }
    }

    @Test
    public static void testValue(Testing t) {
        t.examine(ElfType.ET_NONE).mapToLong(ElfType::value).is(0);
        t.examine(ElfType.ET_REL).mapToLong(ElfType::value).is(1);
        t.examine(ElfType.ET_EXEC).mapToLong(ElfType::value).is(2);
        t.examine(ElfType.ET_DYN).mapToLong(ElfType::value).is(3);
        t.examine(ElfType.ET_CORE).mapToLong(ElfType::value).is(4);
    }

    @Test
    public static void testName(Testing t) {
        t.examine(ElfType.ET_NONE).map(ElfType::name).is("ET_NONE");
        t.examine(ElfType.ET_REL).map(ElfType::name).is("ET_REL");
        t.examine(ElfType.ET_EXEC).map(ElfType::name).is("ET_EXEC");
        t.examine(ElfType.ET_DYN).map(ElfType::name).is("ET_DYN");
        t.examine(ElfType.ET_CORE).map(ElfType::name).is("ET_CORE");
    }

    @Test
    public static void testGetInt(Testing t) {
        t.examine(ElfType.get(0)).isSame(ElfType.ET_NONE);
        t.examine(ElfType.get(1)).isSame(ElfType.ET_REL);
        t.examine(ElfType.get(2)).isSame(ElfType.ET_EXEC);
        t.examine(ElfType.get(3)).isSame(ElfType.ET_DYN);
        t.examine(ElfType.get(4)).isSame(ElfType.ET_CORE);
        t.examine(ElfType.get(5)).is(new ElfType(5));
        t.examine(ElfType.get(65535)).is(new ElfType(65535));
        t.expect(() -> ElfType.get(-1), IllegalArgumentException.class);
        t.expect(() -> ElfType.get(65536), IllegalArgumentException.class);
    }

    @Test
    public static void testGetString(Testing t) {
        t.examine(ElfType.get("ET_NONE")).isSame(ElfType.ET_NONE);
        t.examine(ElfType.get("ET_REL")).isSame(ElfType.ET_REL);
        t.examine(ElfType.get("ET_EXEC")).isSame(ElfType.ET_EXEC);
        t.examine(ElfType.get("ET_DYN")).isSame(ElfType.ET_DYN);
        t.examine(ElfType.get("ET_CORE")).isSame(ElfType.ET_CORE);
        t.examine(ElfType.get("0")).isSame(ElfType.ET_NONE);
        t.examine(ElfType.get("1")).isSame(ElfType.ET_REL);
        t.examine(ElfType.get("2")).isSame(ElfType.ET_EXEC);
        t.examine(ElfType.get("3")).isSame(ElfType.ET_DYN);
        t.examine(ElfType.get("4")).isSame(ElfType.ET_CORE);
        t.examine(ElfType.get("0x0")).isSame(ElfType.ET_NONE);
        t.examine(ElfType.get("0x1")).isSame(ElfType.ET_REL);
        t.examine(ElfType.get("0x2")).isSame(ElfType.ET_EXEC);
        t.examine(ElfType.get("0x3")).isSame(ElfType.ET_DYN);
        t.examine(ElfType.get("0x4")).isSame(ElfType.ET_CORE);
        t.examine(ElfType.get("+1")).isSame(ElfType.ET_REL);
        t.expect(() -> ElfType.get("0x+1"), IllegalArgumentException.class);
        t.expect(() -> ElfType.get("-1"), IllegalArgumentException.class);
        t.expect(() -> ElfType.get("65536"), IllegalArgumentException.class);
    }
}
