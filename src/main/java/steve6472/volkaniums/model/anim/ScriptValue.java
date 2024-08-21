package steve6472.volkaniums.model.anim;

import com.mojang.serialization.Codec;
import steve6472.volkaniums.util.MathUtil;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Volkaniums <br>
 */
public class ScriptValue
{
    private final String script;
    private final double value;

    public static final Codec<ScriptValue> CODEC = Codec.withAlternative(Codec.DOUBLE.xmap(ScriptValue::fromValue, v -> v.value), Codec.STRING, ScriptValue::fromScript);

    private ScriptValue(String script, double value)
    {
        this.script = script;
        this.value = value;
    }

    public static ScriptValue fromScript(String script)
    {
        return new ScriptValue(script, Double.NaN);
    }

    public static ScriptValue fromValue(double value)
    {
        return new ScriptValue(null, value);
    }

    public double getValue()
    {
        if (script != null)
        {
            if (MathUtil.isDecimal(script))
            {
                return Double.parseDouble(script);
            } else
            {
                throw new RuntimeException("Scripts not yet implemented");
            }
        } else
        {
            return value;
        }
    }
}
