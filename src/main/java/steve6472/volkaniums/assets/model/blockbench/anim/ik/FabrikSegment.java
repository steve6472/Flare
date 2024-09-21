package steve6472.volkaniums.assets.model.blockbench.anim.ik;

import org.joml.Vector3f;

public record FabrikSegment(Vector3f pos, Vector3f other, float targetLen)
{
    public FabrikSegment(Vector3f pos, Vector3f other)
    {
        this(pos, other, pos.distance(other));
    }

    public FabrikSegment copy()
    {
        return new FabrikSegment(new Vector3f(pos()), new Vector3f(other()), targetLen());
    }
}