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
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.flare.ui.textures.SpriteLoader;
import steve6472.flare.ui.textures.type.SpriteRenderType;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Flare <br>
 */
public class FlareRegistries extends RegistryCreators
{
    public static final ObjectRegistry<Setting<?, ?>> VISUAL_SETTINGS = createObjectRegistry(key("visual_settings"), () -> VisualSettings.USERNAME);

    public static final Registry<ElementType<?>> MODEL_ELEMENT = createRegistry(key("model_element"), () -> ElementType.CUBE);
    public static final Registry<KeyframeType<?>> KEYFRAME_TYPE = createRegistry(key("keyframe_type"), () -> KeyframeType.ROTATION);
    public static final Registry<SpriteRenderType<?>> SPRITE_RENDER_TYPE = createRegistry(key("sprite_render_type"), () -> SpriteRenderType.STRETCH);

    // Models have to load after the model types registries
    public static final ObjectRegistry<LoadedModel> STATIC_LOADED_MODEL = createObjectRegistry(key("static_loaded_model"), ErrorModel.INSTANCE, BlockbenchLoader::loadStaticModels);
    public static final ObjectRegistry<LoadedModel> ANIMATED_LOADED_MODEL = createObjectRegistry(key("animated_loaded_model"), ErrorModel.INSTANCE, BlockbenchLoader::loadAnimatedModels);
    public static final ObjectRegistry<FontEntry> FONT = createObjectRegistry(key("font"), FontLoader::bootstrap);
    public static final ObjectRegistry<FontStyleEntry> FONT_STYLE = createObjectRegistry(key("font_style"), StyleLoader::bootstrap);
    public static final ObjectRegistry<SpriteEntry> SPRITE = createObjectRegistry(key("sprite"), SpriteLoader::bootstrap);

    // VK Objects
    public static final ObjectRegistry<TextureSampler> SAMPLER = createVkObjectRegistry(key("sampler"), SamplerLoader::loadSamplers);
    public static final ObjectRegistry<Model> STATIC_MODEL = createVkObjectRegistry(key("static_model"), ErrorModel.VK_STATIC_INSTANCE, BlockbenchLoader::createStaticModels);
    public static final ObjectRegistry<Model> ANIMATED_MODEL = createVkObjectRegistry(key("animated_model"), ErrorModel.VK_ANIMATED_INSTANCE, BlockbenchLoader::createAnimatedModels);

    private static Key key(String id)
    {
        return Key.withNamespace(FlareConstants.NAMESPACE, id);
    }
}
