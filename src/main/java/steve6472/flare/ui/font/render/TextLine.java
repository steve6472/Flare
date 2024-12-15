package steve6472.flare.ui.font.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.ui.font.UnknownCharacter;
import steve6472.flare.ui.font.style.FontStyle;
import steve6472.flare.ui.font.style.FontStyleEntry;

import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Flare <br>
 */
public record TextLine(char[] charEntries, float size, FontStyleEntry style, Anchor anchor, Billboard billboard)
{
    private static final Logger LOGGER = Log.getLogger(TextLine.class);

    private static final Anchor DEFAULT_ANCHOR = Anchor.CENTER;
    private static final Billboard DEFAULT_BILLBOARD = Billboard.FIXED;

    public static final Codec<TextLine> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.xmap(String::toCharArray, String::valueOf).fieldOf("text").forGetter(TextLine::charEntries),
        Codec.FLOAT.optionalFieldOf("size", 1f)
            .validate(f -> f > 0 ? DataResult.success(f) : DataResult.error(() -> "Size can not be smaller or equal to 0", 0.01f)).forGetter(TextLine::size),
        Key.CODEC.xmap(FlareRegistries.FONT_STYLE::get, FontStyleEntry::key).fieldOf("style").forGetter(TextLine::style),
        Anchor.CODEC.optionalFieldOf("anchor", DEFAULT_ANCHOR).forGetter(TextLine::anchor),
        Billboard.CODEC.optionalFieldOf("billboard", DEFAULT_BILLBOARD).forGetter(TextLine::billboard)
    ).apply(instance, TextLine::new));

    public static final Codec<TextLine> CODEC_UI = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.xmap(String::toCharArray, String::valueOf).fieldOf("text").forGetter(TextLine::charEntries),
        Codec.FLOAT.optionalFieldOf("size", 1f)
            .validate(f -> f > 0 ? DataResult.success(f) : DataResult.error(() -> "Size can not be smaller or equal to 0", 0.01f)).forGetter(TextLine::size),
        Key.CODEC.xmap(FlareRegistries.FONT_STYLE::get, FontStyleEntry::key).fieldOf("style").forGetter(TextLine::style),
        Anchor.CODEC.optionalFieldOf("anchor", DEFAULT_ANCHOR).forGetter(TextLine::anchor)
    ).apply(instance, (chars, size, style, anchor) -> new TextLine(chars, size, style, anchor, Billboard.FIXED)));

    public static final float MESSAGE_SIZE = -1f;
    public static final Codec<TextLine> CODEC_MESSAGE = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.xmap(String::toCharArray, String::valueOf).fieldOf("text").forGetter(TextLine::charEntries),
        Codec.FLOAT.optionalFieldOf("size", MESSAGE_SIZE)
            .validate(f -> f > 0 || f == MESSAGE_SIZE ? DataResult.success(f) : DataResult.error(() -> "Size can not be smaller or equal to 0", 0.01f)).forGetter(TextLine::size),
        Key.CODEC.xmap(FlareRegistries.FONT_STYLE::get, FontStyleEntry::key).fieldOf("style").forGetter(TextLine::style)
    ).apply(instance, (chars, size, style) -> new TextLine(chars, size, style, DEFAULT_ANCHOR, DEFAULT_BILLBOARD)));

    public static TextLine fromText(String text, float size, FontStyleEntry style, Anchor anchor, Billboard renderType)
    {
        return new TextLine(text.toCharArray(), size, style, anchor, renderType);
    }
    public static TextLine fromText(String text, float size, Key style, Anchor anchor, Billboard renderType)
    {
        return new TextLine(text.toCharArray(), size, findStyle(style), anchor, renderType);
    }


    public static TextLine fromText(String text, float size, FontStyleEntry style, Anchor anchor)
    {
        return new TextLine(text.toCharArray(), size, style, anchor, DEFAULT_BILLBOARD);
    }
    public static TextLine fromText(String text, float size, Key style, Anchor anchor)
    {
        return new TextLine(text.toCharArray(), size, findStyle(style), anchor, DEFAULT_BILLBOARD);
    }


    public static TextLine fromText(String text, float size, FontStyleEntry style, Billboard renderType)
    {
        return new TextLine(text.toCharArray(), size, style, DEFAULT_ANCHOR, renderType);
    }
    public static TextLine fromText(String text, float size, Key style, Billboard renderType)
    {
        return new TextLine(text.toCharArray(), size, findStyle(style), DEFAULT_ANCHOR, renderType);
    }


    public static TextLine fromText(String text, float size, Anchor anchor)
    {
        return new TextLine(text.toCharArray(), size, findStyle(FontStyle.DEFAULT), anchor, DEFAULT_BILLBOARD);
    }
    public static TextLine fromText(String text, float size, Billboard renderType)
    {
        return new TextLine(text.toCharArray(), size, findStyle(FontStyle.DEFAULT), DEFAULT_ANCHOR, renderType);
    }


    public static TextLine fromText(String text, float size, Anchor anchor, Billboard billboard)
    {
        return new TextLine(text.toCharArray(), size, findStyle(FontStyle.DEFAULT), anchor, billboard);
    }


    public static TextLine fromText(String text, float size, FontStyleEntry style)
    {
        return new TextLine(text.toCharArray(), size, style, DEFAULT_ANCHOR, DEFAULT_BILLBOARD);
    }
    public static TextLine fromText(String text, float size, Key style)
    {
        return new TextLine(text.toCharArray(), size, findStyle(style), DEFAULT_ANCHOR, DEFAULT_BILLBOARD);
    }


    public static TextLine fromText(String text, float size)
    {
        return new TextLine(text.toCharArray(), size, findStyle(FontStyle.DEFAULT), DEFAULT_ANCHOR, DEFAULT_BILLBOARD);
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
