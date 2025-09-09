package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.ApiStatus;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.flare.assets.model.blockbench.animation.AnimationTicker;
import steve6472.orlang.OrlangEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public final class AnimationController implements Keyable
{
    private final OrlangEnvironment environment;
    private final Map<String, Controller> controllers;
    private AnimationTicker previousAnimation;
    private AnimationTicker currentAnimation;
    private float blendTime;

    /// Internal so it can be set
    @ApiStatus.Internal
    public Key key;

    public static final Codec<OrlangEnvironment> ENV_CODEC = Codec.unit(OrlangEnvironment::new);

    public static final Codec<AnimationController> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ENV_CODEC.fieldOf("environment").forGetter(o -> o. environment),
        Codec.unboundedMap(Codec.STRING, Controller.CODEC).fieldOf("controllers").forGetter(o -> o.controllers)
    ).apply(instance, AnimationController::new));

    public AnimationController(OrlangEnvironment environment, Map<String, Controller> controllers)
    {
        this.environment = environment;
        this.controllers = controllers;
    }

    public OrlangEnvironment environment()
    {
        return environment;
    }

    public Map<String, Controller> controllers()
    {
        return controllers;
    }

    @Override
    public String toString()
    {
        return "AnimationController[" + "environment=" + environment + ", " + "controllers=" + controllers + ']';
    }

    @Override
    public Key key()
    {
        return key;
    }

    public AnimationController copy()
    {
        Map<String, Controller> controllersCopy = new HashMap<>(controllers.size());
        controllers.forEach((k, v) -> controllersCopy.put(k, v.copy()));
        return new AnimationController(new OrlangEnvironment(), Map.copyOf(controllersCopy));
    }
}
