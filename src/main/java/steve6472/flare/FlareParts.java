package steve6472.flare;

import steve6472.core.module.ModulePart;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationController;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.registry.FlareRegistryGroups;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.style.FontStyleEntry;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public interface FlareParts
{
    ModulePart<Atlas> ATLAS = new ModulePart<>(FlareRegistries.ATLAS, "Atlas", "atlas");
    ModulePart<TextureSampler> TEXTURES = new ModulePart<>(FlareRegistries.SAMPLER, "Textures", "textures");

    ModulePart<LoadedModel> MODEL_STATIC = new ModulePart<>(FlareRegistries.STATIC_LOADED_MODEL, "Static Model", "model/blockbench/static");
    ModulePart<LoadedModel> MODEL_ANIMATED = new ModulePart<>(FlareRegistries.ANIMATED_LOADED_MODEL, "Animated Model", "model/blockbench/animated");

    ModulePart<AnimationController> ANIMATION_CONTROLLER = new ModulePart<>(FlareRegistries.ANIMATION_CONTROLLER, "Animation Controller", "model/blockbench/animation_controller");

    ModulePart<FontEntry> FONT = new ModulePart<>(FlareRegistries.FONT, "Font", "font/fonts");
    ModulePart<FontStyleEntry> STYLE = new ModulePart<>(FlareRegistries.FONT_STYLE, "Font Style", "font/styles");
}
