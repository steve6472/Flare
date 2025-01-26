package steve6472.flare.render.impl;

import steve6472.flare.struct.Struct;

import java.util.List;

/**
 * Created by steve6472
 * Date: 12/12/2024
 * Project: Flare <br>
 */
public abstract class RenderImpl
{
    protected List<Struct> structList;
    protected float far = 256;

    public abstract void render();

    public final void setStructList(List<Struct> structs)
    {
        structList = structs;
    }

    public void setFar(float far)
    {
        this.far = far;
    }

    public float getFar()
    {
        return far;
    }
}
