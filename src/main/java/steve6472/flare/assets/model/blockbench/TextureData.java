package steve6472.flare.assets.model.blockbench;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public record TextureData(int id, int width, int height, int uvWidth, int uvHeight, String relativePath)
{
    // TODO: add animation data, new record for that
    public static final Codec<TextureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("id").xmap(Integer::parseInt, i -> Integer.toString(i)).forGetter(o -> o.id),
        Codec.INT.fieldOf("width").forGetter(o -> o.width),
        Codec.INT.fieldOf("height").forGetter(o -> o.height),
        Codec.INT.fieldOf("uv_width").forGetter(o -> o.uvWidth),
        Codec.INT.fieldOf("uv_height").forGetter(o -> o.uvHeight),
        Codec.STRING.fieldOf("relative_path").forGetter(o -> o.relativePath)
    ).apply(instance, TextureData::new));
}
