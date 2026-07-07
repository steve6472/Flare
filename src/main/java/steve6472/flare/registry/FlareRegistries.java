package steve6472.flare.registry;

import com.mojang.serialization.MapCodec;
import steve6472.core.registry.*;
import steve6472.core.setting.Setting;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.atlas.source.Source;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.blockbench.*;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationController;
import steve6472.flare.assets.model.blockbench.animation.keyframe.KeyFrame;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.style.FontStyleEntry;
import steve6472.flare.ui.textures.type.SpriteUI;

import static steve6472.flare.FlareConstants.key;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Flare <br>
 */
public class FlareRegistries
{
    public static final ResourceKey<Registry<Setting<?, ?>>> VISUAL_SETTINGS = ResourceKey.createRegistryKey(key("visual_settings"));
    public static final ResourceKey<Registry<Setting<?, ?>>> FONT_DEBUG_SETTINGS = ResourceKey.createRegistryKey(key("font_debug_settings"));

    public static final ResourceKey<Registry<MapCodec<? extends Element>>> MODEL_ELEMENT = ResourceKey.createRegistryKey(key("model_element"));
    public static final ResourceKey<Registry<MapCodec<? extends KeyFrame>>> KEYFRAME_TYPE = ResourceKey.createRegistryKey(key("keyframe_type"));
    public static final ResourceKey<Registry<MapCodec<? extends SpriteUI>>> SPRITE_UI_TYPE = ResourceKey.createRegistryKey(key("sprite_ui_type"));
    public static final ResourceKey<Registry<MapCodec<? extends Source>>> ATLAS_SOURCE_TYPE = ResourceKey.createRegistryKey(key("atlas_source_type"));

    // Atlas also holds sprites and the data. It has a duplicate TextureSampler for easier access.
    public static final ResourceKey<Registry<Atlas>> ATLAS = ResourceKey.createRegistryKey(key("atlas"));

    // Models have to load after the model types registries
    public static final ResourceKey<Registry<AnimationController>> ANIMATION_CONTROLLER = ResourceKey.createRegistryKey(key("animation_controller"));
    public static final ResourceKey<Registry<LoadedModel>> STATIC_LOADED_MODEL = ResourceKey.createRegistryKey(key("static_loaded_model"));
    public static final ResourceKey<Registry<LoadedModel>> ANIMATED_LOADED_MODEL = ResourceKey.createRegistryKey(key("animated_loaded_model"));
    public static final ResourceKey<Registry<FontEntry>> FONT = ResourceKey.createRegistryKey(key("font"));
    public static final ResourceKey<Registry<FontStyleEntry>> FONT_STYLE = ResourceKey.createRegistryKey(key("font_style"));

    // VK Objects
    public static final ResourceKey<Registry<TextureSampler>> SAMPLER = ResourceKey.createRegistryKey(key("sampler"));
    public static final ResourceKey<Registry<Model>> STATIC_MODEL = ResourceKey.createRegistryKey(key("static_model"));
    public static final ResourceKey<Registry<Model>> ANIMATED_MODEL = ResourceKey.createRegistryKey(key("animated_model"));
}
