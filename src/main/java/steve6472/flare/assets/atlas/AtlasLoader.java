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

        Flare.getModuleManager().loadParts(FlareParts.ATLAS, Atlas.CODEC, (atlas, key) -> {
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
        FlareRegistries.ATLAS.keys().forEach(key -> FlareRegistries.ATLAS.get(key).loadSprites());


    }
}
