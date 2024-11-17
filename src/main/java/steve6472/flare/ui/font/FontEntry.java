package steve6472.flare.ui.font;

import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public record FontEntry(Key key, Font font, int index) implements Keyable
{
}
