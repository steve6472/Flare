package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.core.registry.Key;
import steve6472.core.registry.Type;
import steve6472.flare.FlareConstants;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class SpriteUIType<T extends SpriteUI> extends Type<T>
{
    public static final SpriteUIType<Stretch> STRETCH = register("stretch", Stretch.CODEC);
    public static final SpriteUIType<NineSlice> NINE_SLICE = register("nine_slice", NineSlice.CODEC);
    public static final SpriteUIType<Tile> TILE = register("tile", Tile.CODEC);

    public SpriteUIType(Key key, MapCodec<T> codec)
    {
        super(key, codec);
    }

    private static <T extends SpriteUI> SpriteUIType<T> register(String id, Codec<T> codec)
    {
        var obj = new SpriteUIType<>(FlareConstants.key(id), MapCodec.assumeMapUnsafe(codec));
        FlareRegistries.SPRITE_UI_TYPE.register(obj);
        return obj;
    }
}
