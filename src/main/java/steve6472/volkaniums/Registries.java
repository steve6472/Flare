package steve6472.volkaniums;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.assets.model.blockbench.ElementType;
import steve6472.volkaniums.assets.model.blockbench.ErrorModel;
import steve6472.volkaniums.assets.model.blockbench.LoadedModel;
import steve6472.volkaniums.assets.model.blockbench.BlockbenchLoader;
import steve6472.volkaniums.assets.model.blockbench.anim.KeyframeType;
import steve6472.volkaniums.registry.*;
import steve6472.volkaniums.settings.Settings;
import steve6472.volkaniums.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class Registries
{
    private static final Logger LOGGER = Log.getLogger(Registries.class);
    private static final Map<Key, Supplier<?>> LOADERS = new LinkedHashMap<>();
    private static final Map<Key, VkContent<?>> VK_LOADERS = new LinkedHashMap<>();

    public static final ObjectRegistry<Settings.Setting<?, ?>> SETTINGS = createObjectRegistry("setting", () -> Settings.USERNAME);

    public static final Registry<ElementType<?>> MODEL_ELEMENT = createRegistry("model_element", () -> ElementType.CUBE);
    public static final Registry<KeyframeType<?>> KEYFRAME_TYPE = createRegistry("keyframe_type", () -> KeyframeType.ROTATION);

    // Models have to load after the model types registries
    public static final ObjectRegistry<LoadedModel> STATIC_LOADED_MODEL = createObjectRegistry("static_loaded_model", ErrorModel.INSTANCE, BlockbenchLoader::loadStaticModels);
    public static final ObjectRegistry<LoadedModel> ANIMATED_LOADED_MODEL = createObjectRegistry("animated_loaded_model", ErrorModel.INSTANCE, BlockbenchLoader::loadAnimatedModels);

    // VK Objects
    public static final ObjectRegistry<TextureSampler> SAMPLER = createVkObjectRegistry("sampler", BlockbenchLoader::packImages);
    public static final ObjectRegistry<Model> STATIC_MODEL = createVkObjectRegistry("static_model", ErrorModel.VK_STATIC_INSTANCE, BlockbenchLoader::createStaticModels);
    public static final ObjectRegistry<Model> ANIMATED_MODEL = createVkObjectRegistry("animated_model", ErrorModel.VK_ANIMATED_INSTANCE, BlockbenchLoader::createAnimatedModels);

    private static <T extends Keyable & Serializable<?>> Registry<T> createRegistry(String id, Supplier<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        LOADERS.put(key, bootstrap);
        return new Registry<>(key);
    }

    private static <T extends Keyable> ObjectRegistry<T> createObjectRegistry(String id, Supplier<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key);
    }

    private static <T extends Keyable> ObjectRegistry<T> createObjectRegistry(String id, T defaultValue, Supplier<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key, defaultValue);
    }

    private static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(String id, VkContent<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key);
    }

    private static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(String id, T defaultValue, VkContent<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key, defaultValue);
    }

    public static void createContents()
    {
        LOADERS.forEach((key, loader) -> {
            try
            {
                if (loader.get() == null)
                {
                    LOGGER.severe("Failed to load registry " + key);
                }
            } catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
            LOGGER.finest("Bootstrapped " + key);
        });
    }

    public static void createVkContents(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        VK_LOADERS.forEach((key, loader) -> {
            try
            {
                loader.apply(device, commands, graphicsQueue);
            } catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
            LOGGER.finest("Bootstrapped " + key);
        });
    }

    @FunctionalInterface
    private interface VkContent<T>
    {
        T apply(VkDevice device, Commands commands, VkQueue graphicsQueue);
    }
}
