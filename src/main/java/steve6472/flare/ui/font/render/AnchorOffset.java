package steve6472.flare.ui.font.render;

import org.joml.Vector2f;

/**
 * Created by steve6472
 * Date: 11/17/2024
 * Project: Flare <br>
 */
@FunctionalInterface
interface AnchorOffset
{
    void apply(Vector2f offset, float width, float top, float bottom);
}
