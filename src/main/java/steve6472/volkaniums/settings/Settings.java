package steve6472.volkaniums.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import steve6472.volkaniums.Registries;
import steve6472.volkaniums.registry.Key;
import steve6472.volkaniums.registry.Keyable;
import steve6472.volkaniums.registry.Serializable;
import steve6472.volkaniums.registry.StringValue;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class Settings
{
    /// Dummy value mainly for bootstrap
    public static StringSetting USERNAME = registerString("username", "Steve");
    public static EnumSetting<ValidationLevel> VALIDATION_LEVEL = registerEnum("validation_level", ValidationLevel.VERBOSE);

    /*
     * Graphics
     */
    public static EnumSetting<PresentMode> PRESENT_MODE = registerEnum("present_mode", PresentMode.MAILBOX);
    public static IntSetting FOV = registerInt("fov", 90);
    /// Can be disabled if Graphics Card does not support wide lines
    /// _(disable implementation not fully implemented)_
    public static BoolSetting ENABLE_WIDE_LINES = registerBool("enable_wide_lines", true);
    /// Only applied if [#ENABLE_WIDE_LINES] is `true`
    public static FloatSetting LINE_WIDTH = registerFloat("line_width", 4.0f);

    /*
     * Keyboard
     */

    public static IntSetting KEY_MOVE_LEFT = registerInt("key_move_left", GLFW_KEY_A);
    public static IntSetting KEY_MOVE_RIGHT = registerInt("key_move_right", GLFW_KEY_D);
    public static IntSetting KEY_MOVE_FORWARD = registerInt("key_move_forward", GLFW_KEY_W);
    public static IntSetting KEY_MOVE_BACKWARD = registerInt("key_move_backward", GLFW_KEY_S);

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

    private static BoolSetting registerBool(String id, boolean defaultValue)
    {
        var obj = new BoolSetting(defaultValue);
        obj.key = Key.defaultNamespace(id);
        Registries.SETTINGS.register(obj);
        return obj;
    }

    private static FloatSetting registerFloat(String id, float defaultValue)
    {
        var obj = new FloatSetting(defaultValue);
        obj.key = Key.defaultNamespace(id);
        Registries.SETTINGS.register(obj);
        return obj;
    }

    private static <E extends Enum<E> & StringValue> EnumSetting<E> registerEnum(String id, E defaultValue)
    {
        var obj = new EnumSetting<>(defaultValue);
        obj.key = Key.defaultNamespace(id);
        Registries.SETTINGS.register(obj);
        return obj;
    }

    public static abstract class Setting<SELF, T> implements Serializable<SELF>, Keyable
    {
        Key key;

        @Override
        public Key key()
        {
            return key;
        }

        public abstract T get();

        public abstract void set(T value);
    }

    private static abstract class PrimitiveSetting<V, SELF> extends Setting<SELF, V>
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

        @Override
        public V get()
        {
            return currentValue;
        }

        @Override
        public void set(V value)
        {
            currentValue = value;
        }

        @Override
        public String toString()
        {
            return "PrimitiveSetting{" + "defaultValue=" + defaultValue + ", currentValue=" + currentValue + ", key=" + key + '}';
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
        private IntSetting(int defaultValue, int currentValue)
        {
            super(defaultValue, currentValue);
        }

        private IntSetting(int defaultValue)
        {
            super(defaultValue);
        }

        @Override
        public Codec<IntSetting> codec()
        {
            return Codec.INT.xmap(s -> new IntSetting(defaultValue, currentValue), s -> s.currentValue);
        }
    }

    public static class BoolSetting extends PrimitiveSetting<Boolean, BoolSetting>
    {
        private BoolSetting(boolean defaultValue, boolean currentValue)
        {
            super(defaultValue, currentValue);
        }

        private BoolSetting(boolean defaultValue)
        {
            super(defaultValue);
        }

        @Override
        public Codec<BoolSetting> codec()
        {
            return Codec.BOOL.xmap(s -> new BoolSetting(defaultValue, currentValue), s -> s.currentValue);
        }
    }

    public static class FloatSetting extends PrimitiveSetting<Float, FloatSetting>
    {
        private FloatSetting(float defaultValue, float currentValue)
        {
            super(defaultValue, currentValue);
        }

        private FloatSetting(float defaultValue)
        {
            super(defaultValue);
        }

        @Override
        public Codec<FloatSetting> codec()
        {
            return Codec.FLOAT.xmap(s -> new FloatSetting(defaultValue, currentValue), s -> s.currentValue);
        }
    }

    public static class EnumSetting<T extends Enum<T> & StringValue> extends Setting<EnumSetting<T>, T>
    {
        protected final T defaultValue;
        protected T currentValue;

        private EnumSetting(T defaultValue, T currentValue)
        {
            this.defaultValue = defaultValue;
            this.currentValue = currentValue;
        }

        private EnumSetting(T defaultValue)
        {
            this(defaultValue, defaultValue);
        }

        @Override
        public T get()
        {
            return currentValue;
        }

        @Override
        public void set(T value)
        {
            currentValue = value;
        }

        @Override
        public Codec<EnumSetting<T>> codec()
        {
            T[] enumConstants = defaultValue.getDeclaringClass().getEnumConstants();
            Codec<T> valueCodec = StringValue.fromValues(() -> enumConstants);
            return valueCodec.xmap(a -> new EnumSetting<>(defaultValue, a), EnumSetting::get);
        }

        @Override
        public String toString()
        {
            return "EnumSetting{" + "defaultValue=" + defaultValue + ", currentValue=" + currentValue + ", key=" + key + '}';
        }
    }
}
