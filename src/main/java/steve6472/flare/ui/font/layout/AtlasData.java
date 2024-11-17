package steve6472.flare.ui.font.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Flare <br>
 */
public record AtlasData(AtlasType type, int distanceRange, int distanceRangeMiddle, float size, int width, int height, String yOrigin)
{
    public static final Codec<AtlasData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        AtlasType.CODEC.fieldOf("type").forGetter(AtlasData::type),
        Codec.INT.fieldOf("distanceRange").forGetter(AtlasData::distanceRange),
        Codec.INT.fieldOf("distanceRangeMiddle").forGetter(AtlasData::distanceRangeMiddle),
        Codec.FLOAT.fieldOf("size").forGetter(AtlasData::size),
        Codec.INT.fieldOf("width").forGetter(AtlasData::width),
        Codec.INT.fieldOf("height").forGetter(AtlasData::height),
        Codec.STRING.fieldOf("yOrigin").forGetter(AtlasData::yOrigin)
    ).apply(instance, AtlasData::new));
}
