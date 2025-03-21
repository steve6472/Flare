package steve6472.flare.ui.font.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.flare.FlareConstants;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.ui.font.Font;
import steve6472.flare.ui.font.UnknownCharacter;
import steve6472.flare.ui.font.style.FontStyleEntry;

import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Flare <br>
 */
public record TextPart(String text, float size, FontStyleEntry style)
{
    private static final Logger LOGGER = Log.getLogger(TextPart.class);

    public static final float MESSAGE_SIZE = Float.NEGATIVE_INFINITY;

    private static final Codec<FontStyleEntry> ENTRY_CODEC = Key.CODEC.xmap(TextPart::findStyle, FontStyleEntry::key);

    public static final Codec<TextPart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("text").forGetter(TextPart::text),
        Codec.FLOAT.optionalFieldOf("size", MESSAGE_SIZE).forGetter(TextPart::size),
        ENTRY_CODEC.optionalFieldOf("style", findStyle(FlareConstants.key("arial"))).forGetter(TextPart::style)
    ).apply(instance, TextPart::new));

    public TextPart(String text, FontStyleEntry style)
    {
        this(text, MESSAGE_SIZE, style);
    }

    private static FontStyleEntry findStyle(Key key)
    {
        FontStyleEntry fontStyleEntry = FlareRegistries.FONT_STYLE.get(key);
        if (fontStyleEntry == null)
        {
            LOGGER.warning("Could not find font style '" + key + "'");
            return FlareRegistries.FONT_STYLE.get(UnknownCharacter.STYLE_KEY);
        }
        return fontStyleEntry;
    }
}
