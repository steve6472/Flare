package steve6472.flare.assets.atlas;

import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.core.registry.Registry;
import steve6472.core.util.ImagePacker;
import steve6472.flare.FlareConstants;
import steve6472.flare.FlareParts;
import steve6472.flare.SamplerLoader;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.VkSetup;
import steve6472.flare.tracy.FlareProfiler;
import steve6472.flare.tracy.Profiler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public class AtlasLoader
{
    private static final Logger LOGGER = Log.getLogger(AtlasLoader.class);
    public static boolean SAVE_DEBUG_ATLASES = false;
    
    public static void boostrap(Registry<Atlas> registry)
    {
        Profiler profiler = FlareProfiler.frame();
        profiler.push("atlas");
        Map<Key, List<Atlas>> atlases = new LinkedHashMap<>();

        // Load all atlases, do not repleace if key collision occures
        Flare.getModuleManager().loadParts(FlareParts.ATLAS, SpriteAtlas.CODEC, (atlas, key) -> {
            atlas.setKey(key);
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
            Registry.register(registry, last.key(), last);
        });

        // Load the sprites
        for (Atlas atlas : mergedAtlases)
        {
            Map<Atlas, ImagePacker> packerMap = new HashMap<>(2);
            profiler.push(atlas.key().toString());
            atlas.create(packerMap);
            profiler.pop();

            SamplerLoader.addSamplerLoader(
                atlas.key(),
                setup -> createTexture(atlas, packerMap.get(atlas), setup),
                samplerHolder -> atlas.sampler = samplerHolder
            );

            if (atlas instanceof SpriteAtlas spriteAtlas && spriteAtlas.getAnimationAtlas() != null)
            {
                Registry.register(registry, spriteAtlas.getAnimationAtlas().key(), spriteAtlas.getAnimationAtlas());

                SamplerLoader.addSamplerLoader(
                    spriteAtlas.getAnimationAtlas().key(),
                    setup -> createTexture(spriteAtlas.getAnimationAtlas(), packerMap.get(spriteAtlas.getAnimationAtlas()), setup),
                    samplerHolder -> spriteAtlas.getAnimationAtlas().sampler = samplerHolder
                );
            }
        }

        profiler.pop();
    }

    public static TextureSampler createTexture(Atlas atlas, ImagePacker imagePacker, VkSetup setup)
    {
        BufferedImage image = imagePacker.getImage();
        saveDebugAtlas(atlas, image);
        return atlas.createVkResource(image, setup);
    }

    private static void saveDebugAtlas(Atlas atlas, BufferedImage image)
    {
        if (!SAVE_DEBUG_ATLASES)
            return;

        Profiler profiler = FlareProfiler.frame();
        profiler.push("saveDebugAtlas");

        try
        {
            File namespaceDir = new File(FlareConstants.FLARE_DEBUG_ATLAS, atlas.key().namespace());
            String folderPath = atlas.key().id();
            if (folderPath.contains("/"))
                folderPath = atlas.key().id().substring(0, folderPath.lastIndexOf('/'));
            else
                folderPath = "";
            File pathDir = new File(namespaceDir, folderPath);
            if (!pathDir.exists() && !pathDir.mkdirs())
                LOGGER.warning("Failed to create path " + pathDir.getAbsolutePath());
            String filePath = atlas.key().id();
            if (filePath.contains("/"))
                filePath = filePath.substring(atlas.key().id().lastIndexOf('/'));
            File output = new File(pathDir, filePath + ".png");
            ImageIO.write(image, "PNG", output);
        } catch (IOException e)
        {
            LOGGER.warning("Failed to save debug " + atlas.key() + ", exception: " + e.getMessage());
        }
        profiler.pop();
    }
}
