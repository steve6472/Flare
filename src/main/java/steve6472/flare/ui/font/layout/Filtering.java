package steve6472.flare.ui.font.layout;

import com.mojang.serialization.Codec;
import org.lwjgl.vulkan.VK10;
import steve6472.core.registry.StringValue;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Flare <br>
 */
public enum Filtering implements StringValue
{
    LINEAR("linear", VK10.VK_FILTER_LINEAR, VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR),
    NEAREST("nearest", VK10.VK_FILTER_NEAREST, VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST);

    public static final Codec<Filtering> CODEC = StringValue.fromValues(Filtering::values);

    private final String typeName;
    public final int vkCode, vkCodeMipmap;

    Filtering(String typeName, int vkCode, int vkCodeMipmap)
    {
        this.typeName = typeName;
        this.vkCode = vkCode;
        this.vkCodeMipmap = vkCodeMipmap;
    }

    @Override
    public String stringValue()
    {
        return typeName;
    }
}
