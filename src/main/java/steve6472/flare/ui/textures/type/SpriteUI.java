package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.core.registry.Registry;
import steve6472.core.registry.Typed;
import steve6472.flare.assets.atlas.source.DirectorySource;
import steve6472.flare.assets.atlas.source.FilterSource;
import steve6472.flare.assets.atlas.source.SingleSource;
import steve6472.flare.registry.BuiltInFlareRegistries;
import steve6472.flare.registry.FlareRegistries;

import java.util.function.Function;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public interface SpriteUI extends Typed<SpriteUI>
{
    Codec<SpriteUI> CODEC = BuiltInFlareRegistries.SPRITE_UI_TYPE.byKeyCodec().dispatch(SpriteUI::codec, Function.identity());

    static void bootstrap(Registry<MapCodec<? extends SpriteUI>> registry)
    {
        Registry.register(registry, "stretch", Stretch.CODEC);
        Registry.register(registry, "nine_slice", NineSlice.CODEC);
        Registry.register(registry, "tile", Tile.CODEC);
    }
}
