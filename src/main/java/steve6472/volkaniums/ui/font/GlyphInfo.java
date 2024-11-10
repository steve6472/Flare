package steve6472.volkaniums.ui.font;

import org.joml.Vector2i;
import org.joml.Vector4f;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Volkaniums <br>
 */
public record GlyphInfo(Vector2i size, Vector2i bearing, int advance, Vector4f texturePos)
{
    public boolean isInvisible()
    {
        return size().x == 0 || size().y == 0;
    }
}