package steve6472.flare.ui.font;

import steve6472.core.registry.Key;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.ui.font.layout.GlyphInfo;
import steve6472.flare.ui.font.style.FontStyleEntry;

/**
 * Created by steve6472
 * Date: 11/17/2024
 * Project: Flare <br>
 */
public class UnknownCharacter
{
    private static final String UNKNOWN = "unknown";
    public static final Key FONT_KEY = Key.defaultNamespace(UNKNOWN);
    public static final Key STYLE_KEY = Key.defaultNamespace(UNKNOWN);

    private static FontEntry fontEntry;
    private static FontStyleEntry styleEntry;
    private static GlyphInfo unknownGlyph;

    public static void init()
    {
        fontEntry = FlareRegistries.FONT.get(FONT_KEY);
        styleEntry = FlareRegistries.FONT_STYLE.get(STYLE_KEY);
        unknownGlyph = fontEntry().font().glyphInfo('A');
    }

    public static FontEntry fontEntry()
    {
        return fontEntry;
    }

    public static FontStyleEntry styleEntry()
    {
        return styleEntry;
    }

    public static GlyphInfo unknownGlyph()
    {
        return unknownGlyph;
    }
}
