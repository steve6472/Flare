package steve6472.flare;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.core.util.GsonUtil;
import steve6472.core.util.ImagePacker;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.atlas.SpriteAtlas;
import steve6472.flare.assets.model.blockbench.BlockbenchLoader;
import steve6472.flare.registry.VkContent;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.assets.atlas.SpriteLoader;
import steve6472.flare.settings.VisualSettings;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Flare <br>
 *
 */
public final class SamplerLoader
{
    private SamplerLoader() {}

    private static final List<VkContent<TextureSampler>> SAMPLER_LOADERS = new ArrayList<>();

    public static TextureSampler loadSamplers(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        File debugAtlasFolder = Debug.getFile("/atlas_data");

        for (Key key : FlareRegistries.ATLAS.keys())
        {
            Atlas atlas = FlareRegistries.ATLAS.get(key);
            ImagePacker packer = SpriteLoader.createTexture(atlas, device, commands, graphicsQueue);
            if (atlas.key().equals(FlareConstants.ATLAS_BLOCKBENCH))
            {
                BlockbenchLoader.fixModelUvs(packer);
            }

            if (VisualSettings.GENERATE_STARTUP_ATLAS_DATA.get())
            {
                Debug.generateFromAtlasAndImagePacker(debugAtlasFolder, atlas, packer);
            }
        }

        SAMPLER_LOADERS.forEach(loader ->
        {
            TextureSampler sampler = loader.apply(device, commands, graphicsQueue);
            FlareRegistries.SAMPLER.register(sampler);
        });

        return null;
    }

    public static void addSamplerLoader(VkContent<TextureSampler> loader)
    {
        SAMPLER_LOADERS.add(loader);
    }

    private static final class Debug
    {
        private static final Logger LOGGER = Log.getLogger("Sampler Loader Debug");

        public static final Codec<Rectangle> RECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(r -> r.x),
            Codec.INT.fieldOf("y").forGetter(r -> r.y),
            Codec.INT.fieldOf("width").forGetter(r -> r.width),
            Codec.INT.fieldOf("height").forGetter(r -> r.height)
        ).apply(instance, Rectangle::new));

        private static void generateFromAtlasAndImagePacker(File file, Atlas atlas, ImagePacker packer)
        {
            LOGGER.info("Dumping atlas data " + atlas.key());
            File dumpFile = new File(file, atlas.key().namespace() + "-" + atlas.key().id().replaceAll("/", "__") + ".json5");
            DataResult<JsonElement> jsonElementDataResult = Codec
                .unboundedMap(Codec.STRING, RECT_CODEC)
                .encodeStart(JsonOps.INSTANCE, packer.getRects());

            if (jsonElementDataResult.isError())
            {
                LOGGER.severe("Could not save atlas, error: " + jsonElementDataResult.error().orElseThrow().message());
            } else if (jsonElementDataResult.isSuccess())
            {
                JsonElement orThrow = jsonElementDataResult.getOrThrow();
                GsonUtil.saveJson(orThrow, dumpFile);
            }
        }

        private static File getFile(String suffix)
        {
            if (!VisualSettings.GENERATE_STARTUP_ATLAS_DATA.get())
            {
                return null;
            }

            File file = new File(FlareConstants.FLARE_DEBUG_FOLDER, "dumped" + suffix);
            if (file.exists())
            {
                LOGGER.info("Removing old data");
                File[] files = file.listFiles();
                if (files != null)
                {
                    for (File listFile : files)
                    {
                        if (!listFile.delete())
                        {
                            LOGGER.severe("Could not delete " + listFile.getAbsolutePath());
                        }
                    }
                }
            } else
            {
                if (!file.mkdirs())
                {
                    LOGGER.severe("Could not create " + file.getAbsolutePath());
                }
            }
            return file;
        }
    }
}
