package steve6472.volkaniums.ui.font;

import steve6472.core.registry.Key;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.ui.font.layout.GlyphInfo;
import steve6472.volkaniums.ui.font.style.FontStyleEntry;

/**
 * Created by steve6472
 * Date: 11/17/2024
 * Project: Volkaniums <br>
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
        fontEntry = VolkaniumsRegistries.FONT.get(FONT_KEY);
        styleEntry = VolkaniumsRegistries.FONT_STYLE.get(STYLE_KEY);
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
