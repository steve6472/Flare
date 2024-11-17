package steve6472.volkaniums.ui.font.style;

import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Volkaniums <br>
 */
public record FontStyleEntry(Key key, FontStyle style, int index) implements Keyable
{
}
