package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.SkinData;
import steve6472.flare.assets.model.blockbench.element.LocatorElement;
import steve6472.flare.assets.model.primitive.PrimitiveSkinModel;
import steve6472.orlang.OrlangEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public final class AnimationController implements Keyable
{
    private final Map<String, Controller> controllers;

    /// Internal so it can be set
    @ApiStatus.Internal
    public Key key;

    LoadedModel model;
    SkinData masterSkinData;
    Matrix4f[] transformations;
    AnimationCallbacks callbacks;
    List<PrimitiveSkinModel.LocatorData> locatorIndicies;

    public record LocatorInfo(Vector3f position, Vector3f velocity) {}

    Map<String, Vector3f> nullObjects;
    Map<String, LocatorInfo> locators;

    public static final Codec<AnimationController> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, Controller.CODEC).fieldOf("controllers").forGetter(o -> o.controllers)
    ).apply(instance, AnimationController::new));

    public AnimationController(Map<String, Controller> controllers)
    {
        this.controllers = controllers;
    }

    public void tick(Matrix4f modelTransform, OrlangEnvironment environment)
    {
        int i = 0;
        for (Controller controller : controllers.values())
        {
            controller.tick(environment);

            if (i == 0)
            {
                transformations = controller.copyTransformations();
            } else
            {
                for (int j = 0; j < transformations.length; j++)
                {
                    Matrix4f transformation = transformations[j];
                    transformation.mul(controller.transformations[j]);
                }
            }

            i++;
        }

        for (Matrix4f transformation : transformations)
        {
            transformation.mulLocal(modelTransform);
        }

        // Set locators
        for (PrimitiveSkinModel.LocatorData locatorIndicy : locatorIndicies)
        {
            Vector3f pos = new Vector3f(locatorIndicy.position());
            pos.mulPosition(transformations[locatorIndicy.transformIndex()]);
            LocatorInfo lastInfo = locators.get(locatorIndicy.name());
            Vector3f velocity = new Vector3f();
            if (lastInfo != null)
            {
                velocity.set(pos).sub(lastInfo.position);
            }
            locators.put(locatorIndicy.name(), new LocatorInfo(pos, velocity));
        }
    }

    public Matrix4f[] getTransformations()
    {
        return transformations;
    }

    public Map<String, Controller> controllers()
    {
        return controllers;
    }

    public AnimationCallbacks callbacks()
    {
        return callbacks;
    }

    public AnimationController createForModel(LoadedModel model)
    {
        Map<String, Controller> controllersCopy = new HashMap<>(controllers.size());
        AnimationController result = new AnimationController(controllersCopy);
        result.key = key;
        controllers.forEach((k, v) -> controllersCopy.put(k, v.copy(result)));
        result.model = model;
        PrimitiveSkinModel primitiveSkinModel = model.toPrimitiveSkinModel();
        result.locatorIndicies = primitiveSkinModel.locatorNames;
        result.masterSkinData = primitiveSkinModel.skinData.copy();
        result.callbacks = new AnimationCallbacks();
        result.nullObjects = new HashMap<>();
        result.locators = new HashMap<>();

        model.getElementsWithType(LocatorElement.class).forEach(l -> result.locators.put(l.name(), new LocatorInfo(l.position(), new Vector3f())));

        return result;
    }

    public LocatorInfo getLocator(String name)
    {
        return locators.get(name);
    }

    public Vector3f getNullObject(String name)
    {
        return nullObjects.get(name);
    }

    @ApiStatus.Internal
    public void setNullObject(String name, Vector3f position)
    {
        nullObjects.put(name, position);
    }

    @Override
    public Key key()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return "AnimationController[" + "controllers=" + controllers + ']';
    }
}
