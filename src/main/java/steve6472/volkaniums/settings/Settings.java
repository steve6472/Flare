package steve6472.volkaniums.settings;

import com.mojang.serialization.Codec;
import steve6472.volkaniums.Registries;
import steve6472.volkaniums.registry.Key;
import steve6472.volkaniums.registry.Keyable;
import steve6472.volkaniums.registry.Serializable;
import steve6472.volkaniums.registry.StringValue;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class Settings
{
    // Dummy value mainly for bootstrap
    public static StringSetting USERNAME = registerString("username", "Steve");
    public static EnumSetting<ValidationLevel> VALIDATION_LEVEL = registerInt("validation_level", ValidationLevel.VERBOSE);
    public static EnumSetting<PresentMode> PRESENT_MODE = registerInt("present_mode", PresentMode.MAILBOX);

    private static StringSetting registerString(String id, String defaultValue)
    {
        var obj = new StringSetting(defaultValue);
        obj.key = Key.defaultNamespace(id);
        Registries.SETTINGS.register(obj);
        return obj;
    }

    private static IntSetting registerInt(String id, int defaultValue)
    {
        var obj = new IntSetting(defaultValue);
        obj.key = Key.defaultNamespace(id);
        Registries.SETTINGS.register(obj);
        return obj;
    }

    private static <E extends Enum<E> & StringValue> EnumSetting<E> registerInt(String id, E defaultValue)
    {
        var obj = new EnumSetting<>(defaultValue);
        obj.key = Key.defaultNamespace(id);
        Registries.SETTINGS.register(obj);
        return obj;
    }

    public static abstract class Setting<T> implements Serializable<T>, Keyable
    {
        Key key;

        @Override
        public Key key()
        {
            return key;
        }
    }

    private static abstract class PrimitiveSetting<V, T> extends Setting<T>
    {
        protected final V defaultValue;
        protected V currentValue;

        private PrimitiveSetting(V defaultValue, V currentValue)
        {
            this.defaultValue = defaultValue;
            this.currentValue = currentValue;
        }

        private PrimitiveSetting(V defaultValue)
        {
            this(defaultValue, defaultValue);
        }

        public V get()
        {
            return currentValue;
        }
    }

    public static class StringSetting extends PrimitiveSetting<String, StringSetting>
    {
        private StringSetting(String defaultValue, String currentValue)
        {
            super(defaultValue, currentValue);
        }

        private StringSetting(String defaultValue)
        {
            super(defaultValue);
        }

        @Override
        public Codec<StringSetting> codec()
        {
            return Codec.STRING.xmap(s -> new StringSetting(defaultValue, currentValue), s -> s.currentValue);
        }
    }

    public static class IntSetting extends PrimitiveSetting<Integer, IntSetting>
    {
        private IntSetting(Integer defaultValue, Integer currentValue)
        {
            super(defaultValue, currentValue);
        }

        private IntSetting(Integer defaultValue)
        {
            super(defaultValue);
        }

        @Override
        public Codec<IntSetting> codec()
        {
            return Codec.INT.xmap(s -> new IntSetting(defaultValue, currentValue), s -> s.currentValue);
        }
    }

    public static class EnumSetting<E extends Enum<E> & StringValue> extends Setting<E>
    {
        protected final E defaultValue;
        protected E currentValue;

        private EnumSetting(E defaultValue, E currentValue)
        {
            this.defaultValue = defaultValue;
            this.currentValue = currentValue;
        }

        private EnumSetting(E defaultValue)
        {
            this(defaultValue, defaultValue);
        }

        public E get()
        {
            return currentValue;
        }

        @Override
        public Codec<E> codec()
        {
            E[] enumConstants = defaultValue.getDeclaringClass().getEnumConstants();
            return StringValue.fromValues(() -> enumConstants);
        }
    }
}
