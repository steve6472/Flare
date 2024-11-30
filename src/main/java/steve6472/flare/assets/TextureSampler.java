package steve6472.flare.assets;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.flare.VulkanUtil;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/7/2024
 * Project: Flare <br>
 */
public class TextureSampler implements Keyable
{
    public long textureImageView;
    public long textureSampler;
    public Texture texture;
    private final Key key;

    public TextureSampler(Texture texture, VkDevice device, Key key)
    {
        this(texture, device, key, VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_LINEAR, true);
    }

    public TextureSampler(Texture texture, VkDevice device, Key key, int filter, int mipmapMode, boolean anisotropy)
    {
        this.texture = texture;
        this.key = key;
        create(device, filter, mipmapMode, anisotropy);
    }

    private void create(VkDevice device, int filter, int mipmapMode, boolean anisotropy)
    {
        textureImageView = VulkanUtil.createImageView(device, texture.textureImage, VK_FORMAT_R8G8B8A8_UNORM, VK_IMAGE_ASPECT_COLOR_BIT);

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
            samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerInfo.magFilter(filter);
            samplerInfo.minFilter(filter);
            samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.anisotropyEnable(anisotropy);
            samplerInfo.maxAnisotropy(16f);
            samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerInfo.mipmapMode(mipmapMode);

            LongBuffer pTextureSampler = stack.mallocLong(1);

            if (vkCreateSampler(device, samplerInfo, null, pTextureSampler) != VK_SUCCESS)
            {
                throw new RuntimeException("Fialed to create texture sampler");
            }

            textureSampler = pTextureSampler.get(0);
        }
    }

    public void cleanup(VkDevice device)
    {
        vkDestroySampler(device, textureSampler, null);
        vkDestroyImageView(device, textureImageView, null);
        texture.cleanup(device);
    }

    @Override
    public String toString()
    {
        return "TextureSampler{" + "textureImageView=" + textureImageView + ", textureSampler=" + textureSampler + ", texture=" + texture + '}';
    }

    @Override
    public Key key()
    {
        return key;
    }
}
