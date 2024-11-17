package steve6472.volkaniums.ui.font.render;

/**
 * Created by steve6472
 * Date: 11/17/2024
 * Project: Volkaniums <br>
 */
@FunctionalInterface
public interface MessageCharIterator
{
    void iterate(MessageChar messageChar, MessageChar nextMessageChar);
}
