package steve6472.flare;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.log.Log;
import steve6472.core.registry.Holder;
import steve6472.core.registry.Key;
import steve6472.core.registry.Registry;
import steve6472.core.util.GsonUtil;
import steve6472.core.util.ImagePacker;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.model.blockbench.BlockbenchLoader;
import steve6472.flare.registry.BuiltInFlareRegistries;
import steve6472.flare.assets.atlas.SpriteLoader;
import steve6472.flare.registry.VkSetup;
import steve6472.flare.settings.VisualSettings;
import steve6472.flare.tracy.FlareProfiler;
import steve6472.flare.tracy.Profiler;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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

    private static final List<Pair<Key, Function<VkSetup, TextureSampler>>> SAMPLER_LOADERS = new ArrayList<>();

    public static void loadSamplers(Registry<TextureSampler> registry, VkSetup setup)
    {
        Profiler profiler = FlareProfiler.frame();
        profiler.push("iterateAtlases");
        File debugAtlasFolder = Debug.getFile("/atlas_data");

        BuiltInFlareRegistries.ATLAS.listElements().forEach(ref -> {
            profiler.push(ref.key().resource().toString());
            Atlas atlas = ref.value();
            profiler.push("createTexture");
            Pair<ImagePacker, TextureSampler> pair = SpriteLoader.createTexture(atlas, setup);
            profiler.popPush("fixModels");
            if (atlas.key().equals(FlareConstants.ATLAS_BLOCKBENCH))
            {
                BlockbenchLoader.fixModelUvs(pair.getFirst());
            }

            if (VisualSettings.GENERATE_STARTUP_ATLAS_DATA.get())
            {
                Debug.generateFromAtlasAndImagePacker(debugAtlasFolder, atlas, pair.getFirst());
            }

            atlas.sampler = Registry.registerForHolder(registry, pair.getSecond().key(), pair.getSecond());
            profiler.pop();
            profiler.pop();
        });

        profiler.popPush("loadSamplers");
        SAMPLER_LOADERS.forEach(loader ->
        {
            profiler.push(loader.getFirst().toString());
            TextureSampler sampler = loader.getSecond().apply(setup);
            Registry.register(registry, sampler.key(), sampler);
            profiler.pop();
        });
        SAMPLER_LOADERS.clear();
        profiler.pop();
    }

    public static void addSamplerLoader(Key key, Function<VkSetup, TextureSampler> loader)
    {
        SAMPLER_LOADERS.add(Pair.of(key, loader));
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
