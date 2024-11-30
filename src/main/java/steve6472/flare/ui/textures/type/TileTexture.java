package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class TileTexture implements UITexture
{
    private static final TileTexture INSTANCE = new TileTexture();
    public static final Codec<TileTexture> CODEC = Codec.unit(INSTANCE);

    private TileTexture()
    {
    }

    @Override
    public UITextureType<?> getType()
    {
        return UITextureType.TILE;
    }
}
