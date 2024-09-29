package steve6472.volkaniums.assets.model.blockbench.anim;

import com.mojang.serialization.Codec;
import steve6472.core.util.MathUtil;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Volkaniums <br>
 */
public class ScriptValue
{
    private final String script;
    private final double value;
    private final double resultScale;

    public static final Codec<ScriptValue> CODEC = Codec.withAlternative(Codec.DOUBLE.xmap(ScriptValue::fromValue, v -> v.value), Codec.STRING, ScriptValue::fromScript);

    public static Codec<ScriptValue> scaledResultCodec(double scale)
    {
        return Codec.withAlternative(Codec.DOUBLE.xmap(value1 -> fromValue(value1, scale), v -> v.value), Codec.STRING, script1 -> fromScript(script1, scale));
    }

    private ScriptValue(String script, double value, double resultScale)
    {
        if (script != null && (MathUtil.isDecimal(script) || script.isBlank()))
        {
            this.value = script.isBlank() ? 0 : Double.parseDouble(script);
            this.script = null;
        } else
        {
            this.script = script;
            this.value = value;
        }

        this.resultScale = resultScale;
    }

    public static ScriptValue fromScript(String script)
    {
        return new ScriptValue(script, Double.NaN, 1.0);
    }

    public static ScriptValue fromScript(String script, double resultScale)
    {
        return new ScriptValue(script, Double.NaN, resultScale);
    }

    public static ScriptValue fromValue(double value)
    {
        return new ScriptValue(null, value, 1.0);
    }

    public static ScriptValue fromValue(double value, double resultScale)
    {
        return new ScriptValue(null, value, resultScale);
    }

    public double getValue()
    {
        if (script != null)
        {
            throw new RuntimeException("Scripts not yet implemented");
        } else
        {
            return value * resultScale;
        }
    }

    @Override
    public String toString()
    {
        if (script != null)
            return "ScriptValue{script=" + script + "}";
        else
            return "ScriptValue{" + "value=" + value + '}';
    }
}
