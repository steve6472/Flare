package steve6472.flare.registry;

import steve6472.core.registry.*;
import steve6472.core.setting.Setting;
import steve6472.flare.FlareConstants;
import steve6472.flare.SamplerLoader;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.atlas.AtlasLoader;
import steve6472.flare.assets.atlas.source.SourceType;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.blockbench.*;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationController;
import steve6472.flare.assets.model.blockbench.animation.keyframe.KeyframeType;
import steve6472.flare.settings.FontDebugSettings;
import steve6472.flare.settings.VisualSettings;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.FontLoader;
import steve6472.flare.ui.font.style.FontStyleEntry;
import steve6472.flare.ui.font.style.StyleLoader;
import steve6472.flare.ui.textures.type.SpriteUIType;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Flare <br>
 */
public class FlareRegistries extends RegistryCreators
{
    static {
        NAMESPACE = FlareConstants.NAMESPACE;
    }

    public static final ObjectRegistry<Setting<?, ?>> VISUAL_SETTINGS = createNamespacedObjectRegistry(FlareConstants.NAMESPACE, "visual_settings", () -> VisualSettings.USERNAME);
    public static final ObjectRegistry<Setting<?, ?>> FONT_DEBUG_SETTINGS = createNamespacedObjectRegistry(FlareConstants.NAMESPACE, "font_debug_settings", () -> FontDebugSettings.BASELINE);

    public static final Registry<ElementType<?>> MODEL_ELEMENT = createNamespacedRegistry(FlareConstants.NAMESPACE, "model_element", () -> ElementType.CUBE);
    public static final Registry<KeyframeType<?>> KEYFRAME_TYPE = createNamespacedRegistry(FlareConstants.NAMESPACE, "keyframe_type", () -> KeyframeType.ROTATION);
    public static final Registry<SpriteUIType<?>> SPRITE_UI_TYPE = createNamespacedRegistry(FlareConstants.NAMESPACE, "sprite_ui_type", () -> SpriteUIType.STRETCH);
    public static final Registry<SourceType<?>> ATLAS_SOURCE_TYPE = createNamespacedRegistry(FlareConstants.NAMESPACE, "atlas_source_type", () -> SourceType.DIRECTORY);

    // Atlas also holds sprites and the data. It has a duplicate TextureSampler for easier access.
    public static final ObjectRegistry<Atlas> ATLAS = createObjectRegistry("atlas", AtlasLoader::boostrap);

    // Models have to load after the model types registries
    public static final ObjectRegistry<AnimationController> ANIMATION_CONTROLLER = createObjectRegistry("animation_controller", AnimationControllerLoader::load);
    public static final ObjectRegistry<LoadedModel> STATIC_LOADED_MODEL = createObjectRegistry("static_loaded_model", ErrorModel.INSTANCE, BlockbenchLoader::loadStaticModels);
    public static final ObjectRegistry<LoadedModel> ANIMATED_LOADED_MODEL = createObjectRegistry("animated_loaded_model", ErrorModel.INSTANCE, BlockbenchLoader::loadAnimatedModels);
    public static final ObjectRegistry<FontEntry> FONT = createObjectRegistry("font", FontLoader::bootstrap);
    public static final ObjectRegistry<FontStyleEntry> FONT_STYLE = createObjectRegistry("font_style", StyleLoader::bootstrap);

    // VK Objects
    public static final ObjectRegistry<TextureSampler> SAMPLER = createVkObjectRegistry("sampler", SamplerLoader::loadSamplers);
    public static final ObjectRegistry<Model> STATIC_MODEL = createVkObjectRegistry("static_model", ErrorModel.VK_STATIC_INSTANCE, BlockbenchLoader::createStaticModels);
    public static final ObjectRegistry<Model> ANIMATED_MODEL = createVkObjectRegistry("animated_model", ErrorModel.VK_ANIMATED_INSTANCE, BlockbenchLoader::createAnimatedModels);
}
