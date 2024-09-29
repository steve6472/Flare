package steve6472.volkaniums.registry;

import steve6472.core.registry.*;
import steve6472.core.setting.Setting;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.assets.model.blockbench.ElementType;
import steve6472.volkaniums.assets.model.blockbench.ErrorModel;
import steve6472.volkaniums.assets.model.blockbench.LoadedModel;
import steve6472.volkaniums.assets.model.blockbench.BlockbenchLoader;
import steve6472.volkaniums.assets.model.blockbench.anim.KeyframeType;
import steve6472.volkaniums.settings.VisualSettings;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class VolkaniumsRegistries extends RegistryCreators
{
    public static final ObjectRegistry<Setting<?, ?>> VISUAL_SETTINGS = createObjectRegistry("visual_settings", () -> VisualSettings.USERNAME);

    public static final Registry<ElementType<?>> MODEL_ELEMENT = createRegistry("model_element", () -> ElementType.CUBE);
    public static final Registry<KeyframeType<?>> KEYFRAME_TYPE = createRegistry("keyframe_type", () -> KeyframeType.ROTATION);

    // Models have to load after the model types registries
    public static final ObjectRegistry<LoadedModel> STATIC_LOADED_MODEL = createObjectRegistry("static_loaded_model", ErrorModel.INSTANCE, BlockbenchLoader::loadStaticModels);
    public static final ObjectRegistry<LoadedModel> ANIMATED_LOADED_MODEL = createObjectRegistry("animated_loaded_model", ErrorModel.INSTANCE, BlockbenchLoader::loadAnimatedModels);

    // VK Objects
    public static final ObjectRegistry<TextureSampler> SAMPLER = createVkObjectRegistry("sampler", BlockbenchLoader::packImages);
    public static final ObjectRegistry<Model> STATIC_MODEL = createVkObjectRegistry("static_model", ErrorModel.VK_STATIC_INSTANCE, BlockbenchLoader::createStaticModels);
    public static final ObjectRegistry<Model> ANIMATED_MODEL = createVkObjectRegistry("animated_model", ErrorModel.VK_ANIMATED_INSTANCE, BlockbenchLoader::createAnimatedModels);
}
