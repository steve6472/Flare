package steve6472.flare.registry;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.log.Log;
import steve6472.core.registry.*;
import steve6472.flare.Commands;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
public class RegistryCreators
{
    private static final Logger LOGGER = Log.getLogger(RegistryCreators.class);
    protected static Map<Key, Supplier<?>> LOADERS = new LinkedHashMap<>();
    protected static Map<Key, VkContent<?>> VK_LOADERS = new LinkedHashMap<>();

    /// This method simply ensures that the fields in a static class are loaded.
    public static void init(Registry<?> dummyRegistry) { }
    /// This method simply ensures that the fields in a static class are loaded.
    public static void init(ObjectRegistry<?> dummyRegistry) { }

    /*
     * Creators
     */

    protected static <T extends Keyable & Serializable<?>> Registry<T> createRegistry(String id, Supplier<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        LOGGER.finest("Creating Registry " + key);
        LOADERS.put(key, bootstrap);
        return new Registry<>(key);
    }

    protected static <T extends Keyable & Serializable<?>> Registry<T> createRegistry(Key key, Supplier<T> bootstrap)
    {
        LOGGER.finest("Creating Registry " + key);
        LOADERS.put(key, bootstrap);
        return new Registry<>(key);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createObjectRegistry(String id, Supplier<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        LOGGER.finest("Creating Object Registry " + key);
        LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createObjectRegistry(Key key, Supplier<T> bootstrap)
    {
        LOGGER.finest("Creating Object Registry " + key);
        LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createObjectRegistry(String id, T defaultValue, Supplier<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        LOGGER.finest("Creating Object Registry " + key);
        LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key, defaultValue);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createObjectRegistry(Key key, T defaultValue, Supplier<T> bootstrap)
    {
        LOGGER.finest("Creating Object Registry " + key);
        LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key, defaultValue);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(String id, VkContent<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        LOGGER.finest("Creating Vk Object Registry " + key);
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(Key key, VkContent<T> bootstrap)
    {
        LOGGER.finest("Creating Vk Object Registry " + key);
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(String id, T defaultValue, VkContent<T> bootstrap)
    {
        Key key = Key.defaultNamespace(id);
        LOGGER.finest("Creating Vk Object Registry " + key);
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key, defaultValue);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(Key key, T defaultValue, VkContent<T> bootstrap)
    {
        LOGGER.finest("Creating Vk Object Registry " + key);
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key, defaultValue);
    }

    /*
     * Loading
     */

    public static void createContents()
    {
        LOGGER.finest("Creating content");
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
        LOGGER.finest("Creating VK content");
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
}
