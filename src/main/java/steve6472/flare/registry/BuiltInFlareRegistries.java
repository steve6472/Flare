package steve6472.flare.registry;

import com.mojang.serialization.MapCodec;
import steve6472.core.registry.Registry;
import steve6472.core.registry.RegistryCore;
import steve6472.core.registry.ReloadableRegistry;
import steve6472.core.setting.Setting;
import steve6472.flare.SamplerLoader;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.atlas.AtlasLoader;
import steve6472.flare.assets.atlas.source.Source;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.blockbench.*;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationController;
import steve6472.flare.assets.model.blockbench.animation.keyframe.KeyFrame;
import steve6472.flare.settings.FontDebugSettings;
import steve6472.flare.settings.VisualSettings;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.FontLoader;
import steve6472.flare.ui.font.style.FontStyleEntry;
import steve6472.flare.ui.font.style.StyleLoader;
import steve6472.flare.ui.textures.type.SpriteUI;

/**
 * Created by steve6472
 * Date: 5/8/2026
 * Project: Flare <br>
 *
 */
public class BuiltInFlareRegistries
{
    public static final Registry<Setting<?, ?>> VISUAL_SETTINGS = RegistryCore.createSettingsRegistry(FlareRegistries.VISUAL_SETTINGS, VisualSettings::bootstrap);
    public static final Registry<Setting<?, ?>> FONT_DEBUG_SETTINGS = RegistryCore.createSettingsRegistry(FlareRegistries.FONT_DEBUG_SETTINGS, FontDebugSettings::bootstrap);

    public static final Registry<MapCodec<? extends Element>> MODEL_ELEMENT = RegistryCore.createRegistry(FlareRegistries.MODEL_ELEMENT, Element::bootstrap);
    public static final Registry<MapCodec<? extends KeyFrame>> KEYFRAME_TYPE = RegistryCore.createRegistry(FlareRegistries.KEYFRAME_TYPE, KeyFrame::bootstrap);
    public static final Registry<MapCodec<? extends SpriteUI>> SPRITE_UI_TYPE = RegistryCore.createRegistry(FlareRegistries.SPRITE_UI_TYPE, SpriteUI::bootstrap);
    public static final Registry<MapCodec<? extends Source>> ATLAS_SOURCE_TYPE = RegistryCore.createRegistry(FlareRegistries.ATLAS_SOURCE_TYPE, Source::bootstrap);

    // Atlas also holds sprites and the data. It has a duplicate TextureSampler for easier access.
    public static final Registry<Atlas> ATLAS = RegistryCore.createDynamicRegistry(FlareRegistries.ATLAS, AtlasLoader::boostrap);

    // Models have to load after the model types registries
    public static final Registry<AnimationController> ANIMATION_CONTROLLER = RegistryCore.createDynamicRegistry(FlareRegistries.ANIMATION_CONTROLLER, AnimationControllerLoader::load);
    public static final Registry<LoadedModel> STATIC_LOADED_MODEL = RegistryCore.createDynamicRegistry(FlareRegistries.STATIC_LOADED_MODEL, BlockbenchLoader::loadStaticModels);
    public static final Registry<LoadedModel> ANIMATED_LOADED_MODEL = RegistryCore.createDynamicRegistry(FlareRegistries.ANIMATED_LOADED_MODEL, BlockbenchLoader::loadAnimatedModels);
    public static final Registry<FontEntry> FONT = RegistryCore.createDynamicRegistry(FlareRegistries.FONT, FontLoader::bootstrap);
    public static final Registry<FontStyleEntry> FONT_STYLE = RegistryCore.createDynamicRegistry(FlareRegistries.FONT_STYLE, StyleLoader::bootstrap);

    // VK Objects
    public static final Registry<TextureSampler> SAMPLER = RegistryCore.registerRegistry(FlareRegistryGroups.VULKAN_RESOURCE, ReloadableRegistry::new, FlareRegistries.SAMPLER, SamplerLoader::loadSamplers);
    public static final Registry<Model> STATIC_MODEL = RegistryCore.registerRegistry(FlareRegistryGroups.VULKAN_RESOURCE, ReloadableRegistry::new, FlareRegistries.STATIC_MODEL, BlockbenchLoader::createStaticModels);
    public static final Registry<Model> ANIMATED_MODEL = RegistryCore.registerRegistry(FlareRegistryGroups.VULKAN_RESOURCE, ReloadableRegistry::new, FlareRegistries.ANIMATED_MODEL, BlockbenchLoader::createAnimatedModels);

    public static void bootstrap() {}
}
