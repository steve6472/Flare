package steve6472.volkaniums.assets.model.blockbench;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.core.registry.ObjectRegistry;
import steve6472.core.util.ImagePacker;
import steve6472.volkaniums.Commands;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.assets.Texture;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.assets.model.primitive.PrimitiveModel;
import steve6472.volkaniums.util.PackerUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/22/2024
 * Project: Volkaniums <br>
 */
public class BlockbenchLoader
{
    private static final Logger LOGGER = Log.getLogger(BlockbenchLoader.class);
    private static final String MODELS_PATH = "resources" + File.separator + "models" + File.separator + "blockbench" + File.separator;
    private static final int STARTING_IMAGE_SIZE = 4;

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
        TextureSampler sampler = new TextureSampler(texture, device, Constants.BLOCKBENCH_TEXTURE);
        VolkaniumsRegistries.SAMPLER.register(sampler);
        fixModelUvs(packer);
        return sampler;
    }

    private static void saveDebugAtlas(BufferedImage image)
    {
        try
        {
            ImageIO.write(image, "PNG", new File("atlas.png"));
        } catch (IOException e)
        {
            LOGGER.warning("Failed to save debug atlas.png, exception: " + e.getMessage());
        }
    }

    private static void fixModelUvs(ImagePacker imagePacker)
    {
        VolkaniumsRegistries.ANIMATED_LOADED_MODEL.keys().forEach(key ->
        {
            LoadedModel model = VolkaniumsRegistries.ANIMATED_LOADED_MODEL.get(key);
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });

        VolkaniumsRegistries.STATIC_LOADED_MODEL.keys().forEach(key ->
        {
            LoadedModel model = VolkaniumsRegistries.STATIC_LOADED_MODEL.get(key);
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });

        ErrorModel.INSTANCE.elements().forEach(el -> el.fixUvs(ErrorModel.INSTANCE, imagePacker));
    }

    public static LoadedModel loadStaticModels()
    {
        return loadModels("static", VolkaniumsRegistries.STATIC_LOADED_MODEL);
    }

    public static LoadedModel loadAnimatedModels()
    {
        return loadModels("animated", VolkaniumsRegistries.ANIMATED_LOADED_MODEL);
    }

    public static Model createStaticModels(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        ErrorModel.VK_STATIC_INSTANCE.createVertexBuffer(device, commands, graphicsQueue, ErrorModel.INSTANCE.toPrimitiveModel());
        VolkaniumsRegistries.STATIC_MODEL.register(ErrorModel.VK_STATIC_INSTANCE);

        return createModels(device, commands, graphicsQueue, VolkaniumsRegistries.STATIC_LOADED_MODEL, VolkaniumsRegistries.STATIC_MODEL, LoadedModel::toPrimitiveModel);
    }

    public static Model createAnimatedModels(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        ErrorModel.VK_ANIMATED_INSTANCE.createVertexBuffer(device, commands, graphicsQueue, ErrorModel.INSTANCE.toPrimitiveSkinModel());
        VolkaniumsRegistries.ANIMATED_MODEL.register(ErrorModel.VK_ANIMATED_INSTANCE);

        return createModels(device, commands, graphicsQueue, VolkaniumsRegistries.ANIMATED_LOADED_MODEL, VolkaniumsRegistries.ANIMATED_MODEL, LoadedModel::toPrimitiveSkinModel);
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

    private static LoadedModel loadModels(String path, ObjectRegistry<LoadedModel> modelRegistry)
    {
        LoadedModel[] models;

        try
        {
            File[] files = new File(MODELS_PATH + path).listFiles();
            models = loadModels(files);
        } catch (URISyntaxException | IOException e)
        {
            throw new RuntimeException(e);
        }

        for (LoadedModel model : models)
        {
            modelRegistry.register(model.key(), model);
            loadTextures(model, MODELS_PATH + path);
        }

        if (models.length > 0)
            return models[0];

        return ErrorModel.INSTANCE;
    }

    private static void loadTextures(LoadedModel model, String modelPath)
    {
        LOGGER.finest("Loading textures for model " + model.key());
        try
        {
            List<TextureData> textures = model.textures();
//            if (textures.size() > 1) LOGGER.warning("Multiple textures per model functionality not confirmed yet! (" + model.key() + ")");
            if (textures.isEmpty()) LOGGER.warning("Model has no texture, weirdness may happen! (" + model.key() + ")");

            for (TextureData texture : textures)
            {
                String pathId = texture.relativePath();
                IMAGES.computeIfAbsent(pathId, _ -> {
                    String path = Paths.get(modelPath).resolve(Paths.get(pathId)).normalize().toAbsolutePath().toString();

                    try
                    {
                        File input = new File(path);
                        if (!input.exists())
                        {
                            throw new RuntimeException("Texture " + path + "(" + pathId + ")" + " not found");
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

    private static LoadedModel[] loadModels(File[] files) throws URISyntaxException, IOException
    {
        if (files == null)
            return new LoadedModel[0];

        List<LoadedModel> models = new ArrayList<>();

        for (File file : files)
        {
            if (!file.getAbsolutePath().endsWith(".bbmodel"))
            {
                Collections.addAll(models, loadModels(file.listFiles()));
            } else
            {
                LoadedModel loadedModel = loadModel(file);
                if (loadedModel != null)
                    models.add(loadedModel);
            }
        }

        return models.toArray(new LoadedModel[0]);
    }

    private static LoadedModel loadModel(File file) throws FileNotFoundException
    {
        if (!file.getAbsolutePath().endsWith(".bbmodel"))
        {
            LOGGER.severe("ExtraPath does not end with .bbmodel");
            return ErrorModel.INSTANCE;
        }

        LOGGER.finest("Loading model: " + file);

        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        JsonElement jsonElement = JsonParser.parseReader(reader);
        DataResult<Pair<LoadedModel, JsonElement>> decode = LoadedModel.CODEC.decode(JsonOps.INSTANCE, jsonElement);

        if (decode.isError())
        {
            LOGGER.severe("Resource loading error '" + file + "'");
            decode.error().ifPresent(err -> LOGGER.severe(err.message()));
            return ErrorModel.INSTANCE;
        }

        Pair<LoadedModel, JsonElement> decoded = decode.getOrThrow();
        LoadedModel loadedModel = decoded.getFirst();

        String filePath = file.getAbsolutePath();
        int startIndex = filePath.indexOf(MODELS_PATH);
        String extractedPath = filePath.substring(startIndex + MODELS_PATH.length() - ("blockbench" + File.separator).length());
        extractedPath = extractedPath.substring(0, extractedPath.lastIndexOf(".bbmodel"));

        // Replace windows separator \ with /
        extractedPath = extractedPath.replace("\\", "/");

        return overrideKey(loadedModel, Key.defaultNamespace(extractedPath));
    }

    private static LoadedModel overrideKey(LoadedModel model, Key newKey)
    {
        return new LoadedModel(model.meta(), newKey, model.resolution(), model.elements(), model.outliner(), model.textures(), model.animations());
    }
}
