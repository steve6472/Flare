package steve6472.flare.assets.atlas;

import steve6472.flare.FlareParts;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public class AtlasLoader
{
    public static void boostrap()
    {
        Flare.getModuleManager().loadParts(FlareParts.ATLAS, Atlas.CODEC, (atlas, key) -> {
            atlas.key = key;
            FlareRegistries.ATLAS.register(atlas);
        });
    }
}
