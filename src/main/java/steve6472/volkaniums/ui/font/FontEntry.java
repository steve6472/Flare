package steve6472.volkaniums.ui.font;

import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Volkaniums <br>
 */
public record FontEntry(Key key, Font font, int index) implements Keyable
{
}
