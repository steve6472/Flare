package steve6472.flare.ui.font.style;

import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public record FontStyleEntry(Key key, FontStyle style, int index) implements Keyable
{
}
