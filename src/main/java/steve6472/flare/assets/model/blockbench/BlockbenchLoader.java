package steve6472.flare.assets.model.blockbench;

import com.mojang.datafixers.util.Pair;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.log.Log;
import steve6472.core.module.ModuleUtil;
import steve6472.core.registry.Key;
import steve6472.core.registry.ObjectRegistry;
import steve6472.core.util.ImagePacker;
import steve6472.flare.Commands;
import steve6472.flare.FlareConstants;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.primitive.PrimitiveModel;
import steve6472.flare.util.PackerUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/22/2024
 * Project: Flare <br>
 */
public class BlockbenchLoader
{
    private static final Logger LOGGER = Log.getLogger(BlockbenchLoader.class);
    private static final int STARTING_IMAGE_SIZE = 512;

    private static final File DEBUG_ATLAS = new File(FlareConstants.FLARE_DEBUG_FOLDER, "blockbench_atlas.png");

    private static final Map<String, BufferedImage> IMAGES = new HashMap<>();

    public static TextureSampler packImages(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        return createMainBlockbenchTexture(device, commands, graphicsQueue);
    }

    private static TextureSampler createMainBlockbenchTexture(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        ImagePacker packer = PackerUtil.pack(STARTING_IMAGE_SIZE, IMAGES, true);

        IMAGES.clear();

        BufferedImage image = packer.getImage();
        saveDebugAtlas(image);
        Texture texture = new Texture();
        texture.createTextureImageFromBufferedImage(device, image, commands.commandPool, graphicsQueue);
        TextureSampler sampler = new TextureSampler(texture, device, FlareConstants.BLOCKBENCH_TEXTURE);
        FlareRegistries.SAMPLER.register(sampler);
        fixModelUvs(packer);
        return sampler;
    }

    private static void saveDebugAtlas(BufferedImage image)
    {
        try
        {
            ImageIO.write(image, "PNG", DEBUG_ATLAS);
        } catch (IOException e)
        {
            LOGGER.warning("Failed to save debug " + DEBUG_ATLAS.getName() + ", exception: " + e.getMessage());
        }
    }

    private static void fixModelUvs(ImagePacker imagePacker)
    {
        FlareRegistries.ANIMATED_LOADED_MODEL.keys().forEach(key ->
        {
            LoadedModel model = FlareRegistries.ANIMATED_LOADED_MODEL.get(key);
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });

        FlareRegistries.STATIC_LOADED_MODEL.keys().forEach(key ->
        {
            LoadedModel model = FlareRegistries.STATIC_LOADED_MODEL.get(key);
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });

        ErrorModel.INSTANCE.elements().forEach(el -> el.fixUvs(ErrorModel.INSTANCE, imagePacker));
    }

    public static void loadStaticModels()
    {
        loadModels("static", FlareRegistries.STATIC_LOADED_MODEL);
    }

    public static void loadAnimatedModels()
    {
        loadModels("animated", FlareRegistries.ANIMATED_LOADED_MODEL);
    }

    public static Model createStaticModels(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        ErrorModel.VK_STATIC_INSTANCE.createVertexBuffer(device, commands, graphicsQueue, ErrorModel.INSTANCE.toPrimitiveModel());
        FlareRegistries.STATIC_MODEL.register(ErrorModel.VK_STATIC_INSTANCE);

        return createModels(device, commands, graphicsQueue, FlareRegistries.STATIC_LOADED_MODEL, FlareRegistries.STATIC_MODEL, LoadedModel::toPrimitiveModel);
    }

    public static Model createAnimatedModels(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        ErrorModel.VK_ANIMATED_INSTANCE.createVertexBuffer(device, commands, graphicsQueue, ErrorModel.INSTANCE.toPrimitiveSkinModel());
        FlareRegistries.ANIMATED_MODEL.register(ErrorModel.VK_ANIMATED_INSTANCE);

        return createModels(device, commands, graphicsQueue, FlareRegistries.ANIMATED_LOADED_MODEL, FlareRegistries.ANIMATED_MODEL, LoadedModel::toPrimitiveSkinModel);
    }

    private static Model createModels(VkDevice device, Commands commands, VkQueue graphicsQueue, ObjectRegistry<LoadedModel> from, ObjectRegistry<Model> to, Function<LoadedModel, PrimitiveModel> converter)
    {
        Model first = null;
        for (Key key : from.keys())
        {
            LoadedModel loadedModel = from.get(key);
            Model model = new Model(key);
            model.createVertexBuffer(device, commands, graphicsQueue, converter.apply(loadedModel));
            if (first == null)
                first = model;
            to.register(model);
        }
        return first;
    }

    private static void loadModels(String path, ObjectRegistry<LoadedModel> modelRegistry)
    {
        Map<Key, Pair<LoadedModel, String>> models = new LinkedHashMap<>();

        ModuleUtil.loadModuleJsonCodecs(Flare.getModuleManager(), "model/blockbench/" + path, LoadedModel.CODEC, (module, file, key, loadedModel) -> {
            key = Key.withNamespace(key.namespace(), "blockbench/" + path + "/" + key.id());
            loadedModel = overrideKey(loadedModel, key);
            LOGGER.finest("Loaded %s '%s' from module '%s'".formatted("model", key, module.name()));
            models.put(key, Pair.of(loadedModel, file.getAbsolutePath()));
        });

        for (Pair<LoadedModel, String> value : models.values())
        {
            LoadedModel model = value.getFirst();
            modelRegistry.register(model.key(), model);
            loadTextures(model, value.getSecond());
        }
    }

    private static void loadTextures(LoadedModel model, String modelPath)
    {
        LOGGER.finest("Loading textures for model '" + model.key() + "'");
        try
        {
            List<TextureData> textures = model.textures();
            if (textures.isEmpty()) LOGGER.warning("Model has no texture, weirdness may happen! (" + model.key() + ")");

            for (TextureData texture : textures)
            {
                String pathId = texture.relativePath();
                IMAGES.computeIfAbsent(pathId, _ ->
                {
                    String path = Paths.get(modelPath).resolve(Paths.get(pathId)).normalize().toAbsolutePath().toString();
                    try
                    {
                        File input = new File(path);
                        if (!input.exists())
                        {
                            throw new RuntimeException("Texture " + path + " (" + pathId + ")" + " not found");
                        }
                        return ImageIO.read(input);
                    } catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (RuntimeException e)
        {
            LOGGER.severe("Failed to load texture for model " + model.key());
            throw e;
        }
    }

    private static LoadedModel overrideKey(LoadedModel model, Key newKey)
    {
        return new LoadedModel(model.meta(), newKey, model.resolution(), model.elements(), model.outliner(), model.textures(), model.animations());
    }
}
