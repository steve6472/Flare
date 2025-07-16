package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class Stretch implements SpriteUI
{
    private static final Stretch INSTANCE = new Stretch();
    public static final Codec<Stretch> CODEC = Codec.unit(INSTANCE);

    private Stretch()
    {
    }

    @Override
    public SpriteUIType<?> getType()
    {
        return SpriteUIType.STRETCH;
    }

    public static Stretch instance()
    {
        return INSTANCE;
    }
}
