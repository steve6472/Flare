package steve6472.flare.assets.texture;

import org.joml.Vector4f;
import steve6472.core.registry.Holder;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.Atlas;

/**
 * Created by steve6472
 * Date: 7/18/2026
 * Project: Flare <br>
 *
 */
public record AtlasTextureLocation(Holder<Atlas> atlas, Vector4f uv) implements TextureLocation
{
    @Override
    public Holder<TextureSampler> sampler()
    {
        return atlas.value().getSampler();
    }
}
