package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.core.registry.Key;
import steve6472.core.registry.Type;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class SpriteRenderType<T extends SpriteRender> extends Type<T>
{
    public static final SpriteRenderType<Stretch> STRETCH = register("stretch", Stretch.CODEC);
    public static final SpriteRenderType<NineSlice> NINE_SLICE = register("nine_slice", NineSlice.CODEC);
    public static final SpriteRenderType<Tile> TILE = register("tile", Tile.CODEC);

    public SpriteRenderType(Key key, MapCodec<T> codec)
    {
        super(key, codec);
    }

    private static <T extends SpriteRender> SpriteRenderType<T> register(String id, Codec<T> codec)
    {
        var obj = new SpriteRenderType<>(Key.defaultNamespace(id), MapCodec.assumeMapUnsafe(codec));
        FlareRegistries.SPRITE_RENDER_TYPE.register(obj);
        return obj;
    }
}
