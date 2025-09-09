package steve6472.flare.assets.model.blockbench;

import steve6472.flare.FlareParts;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationController;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 9/22/2024
 * Project: Flare <br>
 */
public class AnimationControllerLoader
{
    public static void load()
    {
        Flare.getModuleManager().loadModuleJsonCodecs(FlareParts.ANIMATION_CONTROLLER, AnimationController.CODEC, (_, _, key, result) -> {
            result.key = key;
            FlareRegistries.ANIMATION_CONTROLLER.register(result);
        });
    }
}
