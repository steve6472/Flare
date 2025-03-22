package steve6472.flare.registry;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.log.Log;
import steve6472.core.registry.*;
import steve6472.core.util.Preconditions;
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
public class RegistryCreators extends RegistryRegister
{
    private static final Logger LOGGER = Log.getLogger(RegistryCreators.class);
    protected static Map<Key, VkContent<?>> VK_LOADERS = new LinkedHashMap<>();

    /*
     * Creators
     */

    protected static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(String id, VkContent<T> bootstrap)
    {
        Preconditions.checkNotNull(NAMESPACE, "Create a static block and assign a String to NAMESPACE");
        Key key = Key.withNamespace(NAMESPACE, id);
        LOGGER.finest("Creating Vk Object Registry '" + key + "'");
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(Key key, VkContent<T> bootstrap)
    {
        LOGGER.finest("Creating Vk Object Registry '" + key + "'");
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(String id, T defaultValue, VkContent<T> bootstrap)
    {
        Preconditions.checkNotNull(NAMESPACE, "Create a static block and assign a String to NAMESPACE");
        Key key = Key.withNamespace(NAMESPACE, id);
        LOGGER.finest("Creating Vk Object Registry '" + key + "'");
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key, defaultValue);
    }

    protected static <T extends Keyable> ObjectRegistry<T> createVkObjectRegistry(Key key, T defaultValue, VkContent<T> bootstrap)
    {
        LOGGER.finest("Creating Vk Object Registry '" + key + "'");
        VK_LOADERS.put(key, bootstrap);
        return new ObjectRegistry<>(key, defaultValue);
    }

    /*
     * Loading
     */

    public static void createVkContents(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        LOGGER.finest("Creating VK content");
        VK_LOADERS.forEach((key, loader) -> {
            LOGGER.finest("Bootstrapping VK '" + key + "'");
            loader.apply(device, commands, graphicsQueue);
        });
    }
}
