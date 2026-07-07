package steve6472.flare.assets.atlas;

import steve6472.core.registry.Key;
import steve6472.core.registry.Registry;
import steve6472.flare.FlareParts;
import steve6472.flare.core.Flare;

import java.util.*;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public class AtlasLoader
{
    public static void boostrap(Registry<Atlas> registry)
    {
        Map<Key, List<Atlas>> atlases = new LinkedHashMap<>();

        // Load all atlases, do not repleace if key collision occures
        Flare.getModuleManager().loadParts(FlareParts.ATLAS, SpriteAtlas.CODEC, (atlas, key) -> {
            atlas.key = key;
            atlases.computeIfAbsent(key, _ -> new ArrayList<>()).add(atlas);
        });

        // Merge atlases with the same key
        List<Atlas> mergedAtlases = new ArrayList<>(atlases.size());

        atlases.forEach((_, arr) ->
        {
            Atlas last = arr.getFirst();

            for (int i = 1; i < arr.size(); i++)
            {
                last.mergeWith(arr.get(i));
            }

            mergedAtlases.add(last);
            Registry.register(registry, last.key, last);
        });

        // Load the sprites
        for (Atlas atlas : mergedAtlases)
        {
            atlas.create();

            if (atlas instanceof SpriteAtlas spriteAtlas && spriteAtlas.getAnimationAtlas() != null)
            {
                Registry.register(registry, spriteAtlas.getAnimationAtlas().key, spriteAtlas.getAnimationAtlas());
            }
        }
    }
}
