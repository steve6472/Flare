package steve6472.flare.assets.texture;

import org.joml.Vector4fc;
import steve6472.core.registry.Holder;
import steve6472.flare.assets.TextureSampler;

/**
 * Created by steve6472
 * Date: 7/18/2026
 * Project: Flare <br>
 * This concept is currently unused
 */
public interface TextureLocation
{
    Holder<TextureSampler> sampler();
    Vector4fc uv();
}
