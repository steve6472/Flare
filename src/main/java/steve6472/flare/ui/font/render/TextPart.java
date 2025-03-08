package steve6472.flare.ui.font.render;

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
public record TextPart(String text, float size, FontStyleEntry style)
{
    private static final Logger LOGGER = Log.getLogger(TextPart.class);

    public static final float MESSAGE_SIZE = Float.NEGATIVE_INFINITY;

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
