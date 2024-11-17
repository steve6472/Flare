package steve6472.flare.assets.model;

import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;

/**
 * Created by steve6472
 * Date: 9/22/2024
 * Project: Flare <br>
 */
public class Model extends VkModel implements Keyable
{
    private final Key key;

    public Model(Key key)
    {
        this.key = key;
    }

    @Override
    public Key key()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return "Model{" + "key=" + key + '}';
    }
}
