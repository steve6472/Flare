package steve6472.flare.ui.textures.type;

import com.mojang.serialization.MapCodec;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class Stretch implements SpriteUI
{
    private static final Stretch INSTANCE = new Stretch();
    public static final MapCodec<Stretch> CODEC = MapCodec.unit(INSTANCE);

    private Stretch()
    {
    }

    public static Stretch instance()
    {
        return INSTANCE;
    }

    @Override
    public MapCodec<? extends SpriteUI> codec()
    {
        return CODEC;
    }
}
