package steve6472.volkaniums.render;

import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.core.FrameInfo;

/**
 * Created by steve6472
 * Date: 10/1/2024
 * Project: Volkaniums <br>
 */
public abstract class StaticModelRenderImpl
{
    protected abstract void init(SBOTransfromArray<Model> transfromArray);

    public abstract void updateTransformArray(SBOTransfromArray<Model> transfromArray, FrameInfo frameInfo);
}
