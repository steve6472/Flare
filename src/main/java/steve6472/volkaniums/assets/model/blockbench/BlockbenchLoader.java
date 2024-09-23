package steve6472.volkaniums.assets.model.blockbench;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.volkaniums.Commands;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.Main;
import steve6472.volkaniums.Registries;
import steve6472.volkaniums.assets.Texture;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.assets.model.primitive.PrimitiveModel;
import steve6472.volkaniums.registry.Key;
import steve6472.volkaniums.registry.ObjectRegistry;
import steve6472.volkaniums.util.ImagePacker;
import steve6472.volkaniums.util.Log;
import steve6472.volkaniums.util.Preconditions;
import steve6472.volkaniums.util.ResourceListing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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
    private static final String MODELS_PATH = "models/blockbench/";
    private static final int STARTING_IMAGE_SIZE = 64;

    private static final Map<String, BufferedImage> IMAGES = new HashMap<>();

    public static TextureSampler packImages(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        return createMainBlockbenchTexture(device, commands, graphicsQueue);
    }

    private static TextureSampler createMainBlockbenchTexture(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        int size = STARTING_IMAGE_SIZE;
        ImagePacker packer;

        l: while (true)
        {
            packer = new ImagePacker(size, size, 1, true);
            for (String imgKey : IMAGES.keySet())
            {
                BufferedImage bufferedImage = IMAGES.get(imgKey);
                try
                {
                    packer.insertImage(imgKey, bufferedImage);
                } catch (RuntimeException ignored)
                {
                    size *= 2;
                    continue l;
                }
            }
            break;
        }

        BufferedImage image = packer.getImage();
        Texture texture = new Texture();
        texture.createTextureImageFromBufferedImage(device, image, commands.commandPool, graphicsQueue);
        TextureSampler sampler = new TextureSampler(texture, device, Constants.BLOCKBENCH_TEXTURE);
        Registries.SAMPLER.register(sampler);
        fixModelUvs(packer);
        return sampler;
    }

    private static void fixModelUvs(ImagePacker imagePacker)
    {
        Registries.ANIMATED_LOADED_MODEL.keys().forEach(key ->
        {
            LoadedModel model = Registries.ANIMATED_LOADED_MODEL.get(key);
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });

        Registries.STATIC_LOADED_MODEL.keys().forEach(key ->
        {
            LoadedModel model = Registries.STATIC_LOADED_MODEL.get(key);
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });
    }

    public static LoadedModel loadStaticModels()
    {
        return loadModels("static", Registries.STATIC_LOADED_MODEL);
    }

    public static LoadedModel loadAnimatedModels()
    {
        return loadModels("animated", Registries.ANIMATED_LOADED_MODEL);
    }

    public static Model createStaticModels(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        return createModels(device, commands, graphicsQueue, Registries.STATIC_LOADED_MODEL, Registries.STATIC_MODEL, LoadedModel::toPrimitiveModel);
    }

    public static Model createAnimatedModels(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        return createModels(device, commands, graphicsQueue, Registries.ANIMATED_LOADED_MODEL, Registries.ANIMATED_MODEL, LoadedModel::toPrimitiveSkinModel);
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
            String[] resourceListing = ResourceListing.getResourceListing(Main.class, MODELS_PATH + path);
            models = loadModels(resourceListing, MODELS_PATH + path + "/", "");
        } catch (URISyntaxException | IOException e)
        {
            throw new RuntimeException(e);
        }

        for (LoadedModel model : models)
        {
            modelRegistry.register(model.key(), model);
            loadTextures(model);
        }

        if (models.length > 0)
            return models[0];

        return new LoadedModel(null, null, null, null, null, null, null);
    }

    private static void loadTextures(LoadedModel model)
    {
        LOGGER.finest("Loading textures for model " + model.key());
        try
        {
            List<TextureData> textures = model.textures();
            if (textures.size() > 1) throw new RuntimeException("Multiple textures not supported yet.");
            if (textures.isEmpty()) throw new RuntimeException("Model has no texture.");

            String pathId = textures.getFirst().relativePath();
            IMAGES.computeIfAbsent(pathId, _ -> {
                String texturePath = "/" + pathId.substring(pathId.indexOf("textures/"));
                try
                {
                    InputStream resourceAsStream = Main.class.getResourceAsStream(texturePath);
                    if (resourceAsStream == null)
                    {
                        throw new RuntimeException("Texture " + pathId + " (" + texturePath + ") " + "not found");
                    }
                    return ImageIO.read(resourceAsStream);
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e)
        {
            LOGGER.severe("Failed to load texture for model " + model.key());
            throw e;
        }
    }

    private static LoadedModel[] loadModels(String[] resources, String totalPath, String extraPath) throws URISyntaxException, IOException
    {
        Preconditions.checkNotNull(resources);

        List<LoadedModel> models = new ArrayList<>();

        for (String s : resources)
        {
            String newPath = totalPath + s + "/";
            String nexExtraPath = extraPath + (extraPath.isEmpty() ? "" : "/") + s;

            if (!s.endsWith(".bbmodel"))
            {
                Collections.addAll(models, loadModels(ResourceListing.getResourceListing(Main.class, newPath), newPath, nexExtraPath));
            } else
            {
                LoadedModel loadedModel = loadModel(newPath.substring(0, newPath.length() - 1), nexExtraPath);
                if (loadedModel != null)
                    models.add(loadedModel);
            }
        }

        return models.toArray(new LoadedModel[0]);
    }

    private static LoadedModel loadModel(String pathUrl, String extraPath)
    {
        if (!pathUrl.startsWith("/"))
            pathUrl = "/" + pathUrl;

        if (extraPath.endsWith(".bbmodel"))
        {
            extraPath = extraPath.substring(0, extraPath.length() - ".bbmodel".length());
        } else
        {
            LOGGER.severe("ExtraPath does not end with .bbmodel");
            return null;
        }

        LOGGER.finest("Loading model: " + pathUrl + " (" + extraPath + ")");

        InputStream inputStream = Main.class.getResourceAsStream(pathUrl);
        if (inputStream == null)
        {
            LOGGER.severe("Failed to load resource '" + pathUrl + "'");
            return null;
        }

        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        JsonElement jsonElement = JsonParser.parseReader(reader);
        DataResult<Pair<LoadedModel, JsonElement>> decode = LoadedModel.CODEC.decode(JsonOps.INSTANCE, jsonElement);

        if (decode.isError())
        {
            LOGGER.severe("Resource loading error '" + pathUrl + "'");
            decode.error().ifPresent(err -> LOGGER.severe(err.message()));
            return null;
        }

        Pair<LoadedModel, JsonElement> decoded = decode.getOrThrow();
        return decoded.getFirst();
    }
}
