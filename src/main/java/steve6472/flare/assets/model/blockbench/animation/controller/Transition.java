package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.orlang.OrlangEnvironment;
import steve6472.orlang.codec.OrBoolValue;
import steve6472.orlang.codec.OrNumValue;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public record Transition(String state, OrBoolValue condition)
{
    public static final Codec<Transition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("state").forGetter(Transition::state),
        OrBoolValue.CODEC.fieldOf("condition").forGetter(Transition::condition)
    ).apply(instance, Transition::new));

    public Transition copy()
    {
        return new Transition(state, condition.copy());
    }

    public boolean shouldTransition(OrlangEnvironment environment)
    {
        return condition.evaluateAndGet(environment);
    }
}
