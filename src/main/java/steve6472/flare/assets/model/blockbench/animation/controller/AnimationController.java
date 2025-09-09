package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.SkinData;
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
    public OrlangEnvironment environment;
    private final Map<String, Controller> controllers;

    /// Internal so it can be set
    @ApiStatus.Internal
    public Key key;

    LoadedModel model;
    SkinData masterSkinData;
    Matrix4f[] transformations;

    public static final Codec<OrlangEnvironment> ENV_CODEC = Codec.unit(OrlangEnvironment::new);

    public static final Codec<AnimationController> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ENV_CODEC.fieldOf("environment").forGetter(o -> o. environment),
        Codec.unboundedMap(Codec.STRING, Controller.CODEC).fieldOf("controllers").validate(in -> {
            if (in.size() != 1)
            {
                HashMap<String, Controller> partial = new HashMap<>();
                for (String s : in.keySet())
                {
                    partial.put(s, in.get(s));
                    break;
                }
                return DataResult.error(() -> "Currently only exactly one controller per model is supported!", partial);
            }
            return DataResult.success(in);
        }).forGetter(o -> o.controllers)
    ).apply(instance, AnimationController::new));

    public AnimationController(OrlangEnvironment environment, Map<String, Controller> controllers)
    {
        this.environment = environment;
        this.controllers = controllers;
    }

    public void tick(Matrix4f modelTransform)
    {
        for (Controller controller : controllers.values())
        {
            controller.tick(modelTransform);
        }
    }

    public Matrix4f[] getTransformations()
    {
        return transformations;
    }

    public OrlangEnvironment environment()
    {
        return environment;
    }

    public Map<String, Controller> controllers()
    {
        return controllers;
    }

    public AnimationController createForModel(LoadedModel model)
    {
        Map<String, Controller> controllersCopy = new HashMap<>(controllers.size());
        AnimationController result = new AnimationController(new OrlangEnvironment(), controllersCopy);
        controllers.forEach((k, v) -> controllersCopy.put(k, v.copy(result)));
        result.model = model;
        result.masterSkinData = model.toPrimitiveSkinModel().skinData.copy();
        return result;
    }

    @Override
    public Key key()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return "AnimationController[" + "environment=" + environment + ", " + "controllers=" + controllers + ']';
    }
}
