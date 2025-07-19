package steve6472.flare.assets.atlas;

import steve6472.core.registry.Key;
import steve6472.flare.FlareParts;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.FlareRegistries;

import java.util.*;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public class AtlasLoader
{
    public static void boostrap()
    {
        Map<Key, List<Atlas>> atlases = new LinkedHashMap<>();

        Flare.getModuleManager().loadParts(FlareParts.ATLAS, SpriteAtlas.CODEC, (atlas, key) -> {
            atlas.key = key;
            atlases.computeIfAbsent(key, _ -> new ArrayList<>()).add(atlas);
        });

        atlases.forEach((_, arr) ->
        {
            Atlas last = arr.getFirst();

            for (int i = 1; i < arr.size(); i++)
            {
                last.mergeWith(arr.get(i));
            }

            FlareRegistries.ATLAS.register(last);
        });

        // Load the sprites
        Collection<Key> keys = List.copyOf(FlareRegistries.ATLAS.keys());
        keys.forEach(key ->
        {
            Atlas atlas = FlareRegistries.ATLAS.get(key);
            atlas.create();

            if (atlas instanceof SpriteAtlas spriteAtlas)
                if (spriteAtlas.getAnimationAtlas() != null)
                    FlareRegistries.ATLAS.register(spriteAtlas.getAnimationAtlas());
        });
    }
}
