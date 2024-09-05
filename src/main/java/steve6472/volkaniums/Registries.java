package steve6472.volkaniums;

import steve6472.volkaniums.model.ElementType;
import steve6472.volkaniums.model.anim.KeyframeType;
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

    public static final ObjectRegistry<Settings.Setting<?, ?>> SETTINGS = createObjectRegistry("setting", () -> Settings.USERNAME);

    public static final Registry<ElementType<?>> MODEL_ELEMENT = createRegistry("model_element", () -> ElementType.CUBE);
    public static final Registry<KeyframeType<?>> KEYFRAME_TYPE = createRegistry("keyframe_type", () -> KeyframeType.ROTATION);

    // Models have to load after the model types registries
//    public static final ObjectRegistry<Settings.Setting<?>> MODEL = createObjectRegistry("model", () -> Settings.USERNAME);

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
                ex.printStackTrace();
                throw new RuntimeException(ErrorCode.REGISTRY_LOADING_ERROR.format(key));
            }
            LOGGER.finest("Bootstrapped " + key);
        });
    }
}
