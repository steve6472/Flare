package steve6472.flare;

import steve6472.core.module.ModulePart;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public interface FlareParts
{
    ModulePart ATLAS = new ModulePart("Atlas", "atlas");
    ModulePart TEXTURES = new ModulePart("Textures", "textures");

    ModulePart MODEL_STATIC = new ModulePart("Static Model", "model/blockbench/static");
    ModulePart MODEL_ANIMATED = new ModulePart("Animated Model", "model/blockbench/animated");

    ModulePart ANIMATION_CONTROLLER = new ModulePart("Animation Controller", "model/blockbench/animation_controller");

    ModulePart FONT = new ModulePart("Font", "font/fonts");
    ModulePart STYLE = new ModulePart("Font Style", "font/styles");
}
