package steve6472.flare.registry;

import steve6472.core.registry.*;
import steve6472.core.setting.Setting;
import steve6472.flare.FlareConstants;
import steve6472.flare.SamplerLoader;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.blockbench.ElementType;
import steve6472.flare.assets.model.blockbench.ErrorModel;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.BlockbenchLoader;
import steve6472.flare.assets.model.blockbench.anim.KeyframeType;
import steve6472.flare.settings.VisualSettings;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.FontLoader;
import steve6472.flare.ui.font.style.FontStyleEntry;
import steve6472.flare.ui.font.style.StyleLoader;
import steve6472.flare.ui.textures.UITextureEntry;
import steve6472.flare.ui.textures.UITextureLoader;
import steve6472.flare.ui.textures.type.UITextureType;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Flare <br>
 */
public class FlareRegistries extends RegistryCreators
{
    public static final ObjectRegistry<Setting<?, ?>> VISUAL_SETTINGS = createObjectRegistry(id("visual_settings"), () -> VisualSettings.USERNAME);

    public static final Registry<ElementType<?>> MODEL_ELEMENT = createRegistry(id("model_element"), () -> ElementType.CUBE);
    public static final Registry<KeyframeType<?>> KEYFRAME_TYPE = createRegistry(id("keyframe_type"), () -> KeyframeType.ROTATION);
    public static final Registry<UITextureType<?>> UI_TEXTURE_TYPE = createRegistry(id("ui_texture_type"), () -> UITextureType.STRETCH);

    // Models have to load after the model types registries
    public static final ObjectRegistry<LoadedModel> STATIC_LOADED_MODEL = createObjectRegistry(id("static_loaded_model"), ErrorModel.INSTANCE, BlockbenchLoader::loadStaticModels);
    public static final ObjectRegistry<LoadedModel> ANIMATED_LOADED_MODEL = createObjectRegistry(id("animated_loaded_model"), ErrorModel.INSTANCE, BlockbenchLoader::loadAnimatedModels);
    public static final ObjectRegistry<FontEntry> FONT = createObjectRegistry(id("font"), FontLoader::bootstrap);
    public static final ObjectRegistry<FontStyleEntry> FONT_STYLE = createObjectRegistry(id("font_style"), StyleLoader::bootstrap);
    public static final ObjectRegistry<UITextureEntry> UI_TEXTURE = createObjectRegistry(id("ui_texture"), UITextureLoader::bootstrap);

    // VK Objects
    public static final ObjectRegistry<TextureSampler> SAMPLER = createVkObjectRegistry(id("sampler"), SamplerLoader::loadSamplers);
    public static final ObjectRegistry<Model> STATIC_MODEL = createVkObjectRegistry(id("static_model"), ErrorModel.VK_STATIC_INSTANCE, BlockbenchLoader::createStaticModels);
    public static final ObjectRegistry<Model> ANIMATED_MODEL = createVkObjectRegistry(id("animated_model"), ErrorModel.VK_ANIMATED_INSTANCE, BlockbenchLoader::createAnimatedModels);

    private static Key id(String id)
    {
        return Key.withNamespace(FlareConstants.NAMESPACE, id);
    }
}
