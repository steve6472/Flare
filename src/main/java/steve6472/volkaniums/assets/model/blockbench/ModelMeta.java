package steve6472.volkaniums.assets.model.blockbench;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.log.Log;

import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public record ModelMeta(String formatVersion, ModelFormat modelFormat, boolean boxUv)
{
    private static final Logger LOGGER = Log.getLogger(ModelMeta.class);
    private static final String LAST_TESTED_VERSION = "4.10";

    public static final Codec<ModelMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("format_version").forGetter(o -> o.formatVersion),
        ModelFormat.CODEC.fieldOf("model_format").forGetter(o -> o.modelFormat),
        Codec.BOOL.fieldOf("box_uv").forGetter(o -> o.boxUv)
    ).apply(instance, ModelMeta::new));

    public ModelMeta
    {
        if (!formatVersion.equals(ErrorModel.VERSION) && !formatVersion.equals(LAST_TESTED_VERSION))
            LOGGER.warning("Format Version " + formatVersion + " has not been verified, errors or crashes may occur, please use last tested version: " + LAST_TESTED_VERSION);
    }
}
