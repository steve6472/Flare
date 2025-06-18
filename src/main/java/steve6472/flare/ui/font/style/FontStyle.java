package steve6472.flare.ui.font.style;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2f;
import steve6472.core.registry.Key;
import steve6472.core.util.ExtraCodecs;
import steve6472.flare.FlareConstants;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.ui.font.Font;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.layout.AtlasData;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Flare <br>
 */
public record FontStyle(FontEntry fontEntry, ColorSoftThick base, ColorSoftThick outline, ColorSoftThick shadow, Flags flags, Vector2f shadowOffset)
{
    public static final Codec<FontStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.keyedFromRegistry(FlareRegistries.FONT).fieldOf("font").forGetter(FontStyle::fontEntry),
        ColorSoftThick.CODEC.optionalFieldOf("base", ColorSoftThick.EMPTY).forGetter(FontStyle::base),
        ColorSoftThick.CODEC.optionalFieldOf("outline", ColorSoftThick.EMPTY).forGetter(FontStyle::outline),
        ColorSoftThick.CODEC.optionalFieldOf("shadow", ColorSoftThick.EMPTY).forGetter(FontStyle::shadow),
        Flags.CODEC.optionalFieldOf("flags", Flags.DEFAULT).forGetter(FontStyle::flags),
        ExtraCodecs.VEC_2F.fieldOf("shadow_offset").forGetter(FontStyle::shadowOffset)
    ).apply(instance, FontStyle::new));

    public Font font()
    {
        return fontEntry.font();
    }

    public static final Key DEFAULT = FlareConstants.key("arial");

    public Struct toStruct(FontEntry font)
    {
        AtlasData atlasData = font.font().getAtlasData();

        return SBO.MSDF_FONT_STYLE.create(
            base.color(),
            outline.color(),
            shadow.color(),

            base.softness(),
            outline.softness(),
            shadow.softness(),
            flags.compressFlags(),

            base.thickness(),
            outline.thickness(),
            shadow.thickness(),
            font.index(),

            shadowOffset,
            new Vector2f(atlasData.width() / atlasData.size(), atlasData.height() / atlasData.size())
        );
    }

    public record Flags(boolean soft)
    {
        public static final Codec<Flags> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("soft", false).forGetter(Flags::soft)
        ).apply(instance, Flags::new));

        public static final Flags DEFAULT = new Flags(false);

        public int compressFlags()
        {
            return
//                (false ? 1 << 5 : 0) |
//                (false ? 1 << 4 : 0) |
//                (false ? 1 << 3 : 0) |
//                (false ? 1 << 2 : 0) |
//                (false ? 1 << 1 : 0) |
                (soft  ? 1      : 0);
        }
    }
}