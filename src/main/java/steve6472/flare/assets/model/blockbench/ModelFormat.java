package steve6472.flare.assets.model.blockbench;

import com.mojang.serialization.Codec;
import steve6472.core.registry.StringValue;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public enum ModelFormat implements StringValue
{
    FREE("free");

    public static final Codec<ModelFormat> CODEC = StringValue.fromValues(ModelFormat::values);

    private final String value;

    ModelFormat(String value)
    {
        this.value = value;
    }

    @Override
    public String stringValue()
    {
        return value;
    }
}
