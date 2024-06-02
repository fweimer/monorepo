package enyo.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal class to provide metadata for {@link OpenEnum} implementations.
 */
final class OpenEnumInfo {
    static <T extends OpenEnum> OpenEnumInfo get(Class<T> cls) {
        return CLASS_VALUES.get(cls);
    }

    String valueToName(long value) {
        String name = valueToName.get(value);
        if (name == null)
            return "0x" + Long.toUnsignedString(value, 16);
        else
            return name;
    }

    OpenEnum fromValue(long value) {
        return enumByValue.get(value);
    }

    OpenEnum fromName(String name) {
        return enumByName.get(name);
    }

    static <T extends OpenEnum> void install(Class<T> cls,
        Map<String, T> nameToEnum,
        Map<Long, String> valueToName) {
        var info = new OpenEnumInfo(cls, nameToEnum, valueToName);
        synchronized (CLASS_MAP) {
            var previous = CLASS_MAP.put(cls, info);
            if (previous != null)
                throw new IllegalStateException(
                    "Already registered: " + cls.getName());
        }
    }

    private OpenEnumInfo(Class<?> cls,
        Map<String, ? extends OpenEnum> nameToEnum,
        Map<Long, String> valueToName) {
        this.valueToName = Map.copyOf(valueToName);
        this.enumByName = Map.copyOf(nameToEnum);

        var enumByValue = new HashMap<Long, OpenEnum>();
        for (var entry : valueToName.entrySet()) {
            var e = nameToEnum.get(entry.getValue());
            if (e == null)
                throw new IllegalArgumentException(String.format(
                    "Unknown enum %s name \"%s\" for value %d",
                    cls.getName(), entry.getValue(), entry.getKey()));
            enumByValue.put(entry.getKey(), e);
        }
        this.enumByValue = Map.copyOf(enumByValue);
    }

    private final Map<Long, String> valueToName;
    private final Map<String, OpenEnum> enumByName;
    private final Map<Long, OpenEnum> enumByValue;

    private static Map<Class<?>, OpenEnumInfo> CLASS_MAP =
        Collections.synchronizedMap(new HashMap<>());

    private static ClassValue<OpenEnumInfo> CLASS_VALUES =
        new ClassValue<OpenEnumInfo>() {
            public OpenEnumInfo computeValue(Class<?> cls) {
                var info = CLASS_MAP.get(cls);
                if (info == null)
                    throw new IllegalArgumentException(cls.getName());
                return info;
            }
        };
}
