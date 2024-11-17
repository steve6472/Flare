package steve6472.flare.ui.font.layout;

import com.mojang.serialization.Codec;
import steve6472.core.registry.StringValue;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Flare <br>
 */
public enum AtlasType implements StringValue
{
    HARDMASK("hardmask"),
    SOFTMASK("softmask"),
    SDF("sdf"),
    PSDF("psdf"),
    MSDF("msdf"),
    MTSDF("mtsdf");

    public static final Codec<AtlasType> CODEC = StringValue.fromValues(AtlasType::values);

    private final String typeName;

    AtlasType(String typeName)
    {
        this.typeName = typeName;
    }

    @Override
    public String stringValue()
    {
        return typeName;
    }
}
