package steve6472.volkaniums.ui.font.style;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2f;
import steve6472.core.registry.Key;
import steve6472.core.util.ExtraCodecs;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.SBO;
import steve6472.volkaniums.ui.font.Font;
import steve6472.volkaniums.ui.font.FontEntry;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Volkaniums <br>
 */
public record FontStyle(FontEntry fontEntry, ColorSoftThick base, ColorSoftThick outline, ColorSoftThick shadow, boolean soft, Vector2f shadowOffset, Vector2f atlasSize)
{
    public static final Codec<FontStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Key.CODEC.xmap(VolkaniumsRegistries.FONT::get, FontEntry::key).fieldOf("font").forGetter(FontStyle::fontEntry),
        ColorSoftThick.CODEC.optionalFieldOf("base", ColorSoftThick.EMPTY).forGetter(FontStyle::base),
        ColorSoftThick.CODEC.optionalFieldOf("outline", ColorSoftThick.EMPTY).forGetter(FontStyle::outline),
        ColorSoftThick.CODEC.optionalFieldOf("shadow", ColorSoftThick.EMPTY).forGetter(FontStyle::shadow),
        Codec.BOOL.optionalFieldOf("soft", false).forGetter(FontStyle::soft),
        ExtraCodecs.VEC_2F.fieldOf("shadow_offset").forGetter(FontStyle::shadowOffset),
        ExtraCodecs.VEC_2F.optionalFieldOf("atlas_size", new Vector2f()).forGetter(FontStyle::atlasSize)
    ).apply(instance, FontStyle::new));

    public Font font()
    {
        return fontEntry.font();
    }

    public static final Key DEFAULT = Key.defaultNamespace("default");

    public Struct toStruct(int fontIndex)
    {
        return SBO.MSDF_FONT_STYLE.create(
            base.color(),
            outline.color(),
            shadow.color(),

            base.softness(),
            outline.softness(),
            shadow.softness(),
            soft ? 1 : 0,

            base.thickness(),
            outline.thickness(),
            shadow.thickness(),
            fontIndex,

            shadowOffset,
            atlasSize
        );
    }
}