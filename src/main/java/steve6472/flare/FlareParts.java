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
    ModulePart TEXTURE_MODEL = new ModulePart("Model Texture", "textures/model");
    ModulePart TEXTURE_UI = new ModulePart("UI Texture", "textures/ui");

    ModulePart MODEL_STATIC = new ModulePart("Static Model", "model/blockbench/static");
    ModulePart MODEL_ANIMATED = new ModulePart("Animated Model", "model/blockbench/animated");

    ModulePart FONT = new ModulePart("Font", "font/fonts");
    ModulePart STYLE = new ModulePart("Font Style", "font/styles");
}
