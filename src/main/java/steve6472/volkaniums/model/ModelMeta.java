package steve6472.volkaniums.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public record ModelMeta(String formatVersion, ModelFormat modelFormat, boolean boxUv)
{
    public static final Codec<ModelMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("format_version").forGetter(o -> o.formatVersion),
        ModelFormat.CODEC.fieldOf("model_format").forGetter(o -> o.modelFormat),
        Codec.BOOL.fieldOf("box_uv").forGetter(o -> o.boxUv)
    ).apply(instance, ModelMeta::new));
}
