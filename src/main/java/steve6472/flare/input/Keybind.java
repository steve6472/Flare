package steve6472.flare.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.core.registry.Serializable;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
public class Keybind implements Keyable, Serializable<Keybind>
{
    private static final Codec<Keybind> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Key.CODEC.fieldOf("key").forGetter(o -> o.key),
        KeybindType.CODEC.fieldOf("type").forGetter(o -> o.keybindType),
        Codec.BOOL.fieldOf("require_order").forGetter(o -> o.requireOrder),
        Codec.INT.listOf().fieldOf("keys").forGetter(o -> IntStream.of(o.keys).boxed().toList())
    ).apply(instance, (key, keybindType, requireOrder, keys) -> new Keybind(key, keybindType, requireOrder, keys.stream().mapToInt(i -> i).toArray())));

    private final Key key;
    private final KeybindType keybindType;
    private final boolean requireOrder;
    private final int[] keys;

    UserInput input;

    private boolean consumed;
    private int progress = 0;

    public Keybind(Key key, KeybindType keybindType, boolean requireOrder, int... keys)
    {
        this.key = key;
        this.keybindType = keybindType;
        this.requireOrder = requireOrder;
        this.keys = keys;

        if (IntStream.of(keys).unordered().distinct().count() != keys.length)
            throw new RuntimeException("Keybind invalid, contains multiple of the same key!");
    }

    public Keybind(Key key, KeybindType keybindType, int keyboardKey)
    {
        this(key, keybindType, false, keyboardKey);
    }

    public boolean isActive()
    {
        if (input == null)
            return false;

        if (requireOrder)
            return isActiveOrdered();
        else
            return isActiveUnordered();
    }

    private boolean isActiveOrdered()
    {
        return isActiveUnordered();
    }

    private boolean isActiveUnordered()
    {
        for (int key : keys)
        {
            if (!input.isKeyPressed(key))
            {
                consumed = false;
                return false;
            }
        }

        if (consumed && keybindType == KeybindType.ONCE)
            return false;

        consumed = true;
        return true;
    }

    @Override
    public Key key()
    {
        return key;
    }

    @Override
    public Codec<Keybind> codec()
    {
        return CODEC;
    }

    @Override
    public String toString()
    {
        return "Keybind{" + "key=" + key + ", keybindType=" + keybindType + ", requireOrder=" + requireOrder + ", keys=" + Arrays.toString(keys) + ", input=" + input + '}';
    }
}
