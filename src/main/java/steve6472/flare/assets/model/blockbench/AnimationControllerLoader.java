package steve6472.flare.assets.model.blockbench;

import steve6472.core.registry.Registry;
import steve6472.flare.FlareParts;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationController;
import steve6472.flare.core.Flare;

/**
 * Created by steve6472
 * Date: 9/22/2024
 * Project: Flare <br>
 */
public class AnimationControllerLoader
{
    public static void load(Registry<AnimationController> registry)
    {
        Flare.getModuleManager().loadModuleJsonCodecs(FlareParts.ANIMATION_CONTROLLER, AnimationController.CODEC, (_, _, key, result) -> {
            result.key = key;
            Registry.register(registry, key, result);
        });
    }
}
