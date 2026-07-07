package steve6472.flare.ui.textures.type;

import com.mojang.serialization.MapCodec;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class Tile implements SpriteUI
{
    private static final Tile INSTANCE = new Tile();
    public static final MapCodec<Tile> CODEC = MapCodec.unit(INSTANCE);

    private Tile()
    {
    }

    @Override
    public MapCodec<? extends SpriteUI> codec()
    {
        return CODEC;
    }
}
