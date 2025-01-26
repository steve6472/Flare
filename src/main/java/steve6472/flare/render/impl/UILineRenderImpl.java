package steve6472.flare.render.impl;

import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.core.log.Log;
import steve6472.flare.render.impl.UIRenderImpl;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Vertex;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 12/19/2024
 * Project: MoonDust <br>
 */
public abstract class UILineRenderImpl extends RenderImpl
{
    protected void line(Vector3f start, Vector3f end, Vector4f color)
    {
        vertex(start, color);
        vertex(end, color);
    }

    protected void rectangle(Vector3f start, Vector3f end, Vector4f color)
    {
        line(start, new Vector3f(start.x, end.y, end.z), color);
        line(new Vector3f(start).add(-1, 0, 0), new Vector3f(end.x, start.y, end.z), color);

        line(new Vector3f(start.x, end.y, end.z), end, color);
        line(new Vector3f(end.x, start.y, end.z), end, color);
    }

    protected final void vertex(Vector3f position, Vector4f color)
    {
        structList.add(Vertex.POS3F_COL4F.create(position, color));
    }
}
