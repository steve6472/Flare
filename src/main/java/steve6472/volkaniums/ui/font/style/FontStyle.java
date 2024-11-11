package steve6472.volkaniums.ui.font.style;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2f;
import org.joml.Vector4f;
import steve6472.core.registry.Key;
import steve6472.core.util.ExtraCodecs;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.SBO;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Volkaniums <br>
 */
public record FontStyle(ColorSoftThick base, ColorSoftThick outline, ColorSoftThick shadow, boolean soft, Vector2f shadowOffset, Vector2f atlasSize)
{
    public static final Codec<FontStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ColorSoftThick.CODEC.optionalFieldOf("base", ColorSoftThick.EMPTY).forGetter(FontStyle::base),
        ColorSoftThick.CODEC.optionalFieldOf("outline", ColorSoftThick.EMPTY).forGetter(FontStyle::outline),
        ColorSoftThick.CODEC.optionalFieldOf("shadow", ColorSoftThick.EMPTY).forGetter(FontStyle::shadow),
        Codec.BOOL.optionalFieldOf("soft", false).forGetter(FontStyle::soft),
        ExtraCodecs.VEC_2F.fieldOf("shadow_offset").forGetter(FontStyle::shadowOffset),
        ExtraCodecs.VEC_2F.optionalFieldOf("atlas_size", new Vector2f()).forGetter(FontStyle::atlasSize)
    ).apply(instance, FontStyle::new));

    /*
    public static final FontStyle DEBUG = new FontStyle(
        new ColorSoftThick( // base
            new Vector4f(0.4f, 0.8f, 0.8f, 1.0f),
            0.1f,
            0.7f
        ),
        new ColorSoftThick( // outline
            new Vector4f(1.0f, 0.6f, 1.0f, 1.0f),
            0.5f,
            0.5f
        ),
        new ColorSoftThick( // shadow
            new Vector4f(0.00f, 0.00f, 0.3f, 1.0f),
            0.2f,
            0.7f
        ),
        true,
        new Vector2f(0.2f, 0.3f),
        new Vector2f()
    );*/

    public static final Key BASE_KEY = Key.defaultNamespace("base");

    public static final FontStyle BASE = new FontStyle(
        new ColorSoftThick( // base
            new Vector4f(0.8f, 0.8f, 0.8f, 1.0f),
            0.0f,
            0.7f
        ),
        new ColorSoftThick( // outline
            new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
            0.5f,
            0.5f
        ),
        new ColorSoftThick( // shadow
            new Vector4f(0.07f, 0.07f, 0.07f, 1.0f),
            0.2f,
            0.7f
        ),
        false,
        new Vector2f(0.03f, 0.03f),
        new Vector2f()
    );

    public void updateFontStyles(Struct styles, int index)
    {
        styles.getMember(0, Struct[].class)[index] = toStruct();
    }

    public Struct toStruct()
    {
        return SBO.MSDF_FONT_STYLE.create(
            base.color(),
            outline.color(),
            shadow.color(),

            base.softness(),
            outline.softness(),
            shadow.softness(),
            soft ? 1f : 0f,

            base.thickness(),
            outline.thickness(),
            shadow.thickness(),
            0f, // PADDING

            shadowOffset,
            atlasSize
        );
    }
}