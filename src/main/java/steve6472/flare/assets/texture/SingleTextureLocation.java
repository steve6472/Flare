package steve6472.flare.assets.texture;

import org.joml.Vector4f;
import org.joml.Vector4fc;
import steve6472.core.registry.Holder;
import steve6472.flare.assets.TextureSampler;

/**
 * Created by steve6472
 * Date: 7/18/2026
 * Project: Flare <br>
 *
 */
public record SingleTextureLocation(Holder<TextureSampler> sampler) implements TextureLocation
{
    private static final Vector4fc UV = new Vector4f(0, 0, 1, 1);

    @Override
    public Vector4fc uv()
    {
        return UV;
    }
}
