package steve6472.flare.ui.font.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.ui.font.UnknownCharacter;
import steve6472.flare.ui.font.style.FontStyleEntry;

import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Flare <br>
 */
public record UITextLine(String text, float size, FontStyleEntry style, Anchor2D anchor)
{
    private static final Logger LOGGER = Log.getLogger(UITextLine.class);

    private static final Anchor2D DEFAULT_ANCHOR = Anchor2D.TOP_LEFT;

    public static final Codec<UITextLine> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("text").forGetter(UITextLine::text),
        Codec.FLOAT.optionalFieldOf("size", 1f)
            .validate(f -> f > 0 ? DataResult.success(f) : DataResult.error(() -> "Size can not be smaller or equal to 0", 0.01f)).forGetter(UITextLine::size),
        Key.CODEC.xmap(FlareRegistries.FONT_STYLE::get, FontStyleEntry::key).fieldOf("style").forGetter(UITextLine::style),
        Anchor2D.CODEC.optionalFieldOf("anchor", DEFAULT_ANCHOR).forGetter(UITextLine::anchor)
    ).apply(instance, UITextLine::new));

    public static final float MESSAGE_SIZE = Float.NEGATIVE_INFINITY;
    public static final Codec<UITextLine> CODEC_MESSAGE = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("text").forGetter(UITextLine::text),
        Codec.FLOAT.optionalFieldOf("size", MESSAGE_SIZE)
            .validate(f -> f > 0 || f == MESSAGE_SIZE ? DataResult.success(f) : DataResult.error(() -> "Size can not be smaller or equal to 0", 0.01f)).forGetter(UITextLine::size),
        Key.CODEC.xmap(FlareRegistries.FONT_STYLE::get, FontStyleEntry::key).fieldOf("style").forGetter(UITextLine::style)
    ).apply(instance, (chars, size, style) -> new UITextLine(chars, size, style, DEFAULT_ANCHOR)));

    public UITextLine(String text, float size, FontStyleEntry style)
    {
        this(text, size, style, DEFAULT_ANCHOR);
    }

    public UITextLine(String text, FontStyleEntry style)
    {
        this(text, MESSAGE_SIZE, style, DEFAULT_ANCHOR);
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
