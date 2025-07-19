package steve6472.flare.assets.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.registry.Key;
import steve6472.flare.*;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.source.Source;
import steve6472.flare.assets.atlas.source.SourceResult;
import steve6472.flare.core.Flare;
import steve6472.flare.framebuffer.AnimatedAtlasFrameBuffer;
import steve6472.flare.registry.FlareRegistries;

import java.awt.image.BufferedImage;
import java.util.*;

import static org.lwjgl.vulkan.VK10.*;
/**
 * Created by steve6472
 * Date: 7/18/2025
 * Project: Flare <br>
 */
public class SpriteAtlas extends Atlas
{
    public static final Codec<SpriteAtlas> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Source.CODEC.listOf().fieldOf("sources").forGetter(SpriteAtlas::getSources)
    ).apply(instance, SpriteAtlas::new));

    private final List<Source> sources;
    private AnimationAtlas animationAtlas;
    public AnimatedAtlasFrameBuffer frameBuffer;

    private SpriteAtlas(List<Source> sources)
    {
        this.sources = sources;
    }

    @Override
    void create()
    {
        // Set to prevent duplicates
        Set<SourceResult> toLoad = new LinkedHashSet<>();

        Flare.getModuleManager().iterateWithNamespaces((module, namespace) ->
        {
            for (Source source : sources)
            {
                Collection<SourceResult> load = source.load(module, namespace);
                for (SourceResult sourceResult : load)
                {
                    if (!sourceResult.file().isFile())
                        throw new RuntimeException("Source did not return a file");
                    if (!sourceResult.file().getName().endsWith(".png"))
                        throw new RuntimeException("Source returned a non-.png file");
                    toLoad.add(sourceResult);
                }
            }
        });

        SpriteLoader.LoadResult loadResult = SpriteLoader.loadFromAtlas(toLoad);
        tempImages = new HashMap<>();
        loadResult.entries().forEach((key, pair) -> {
            sprites.put(key, pair.getFirst());
            tempImages.put(key, pair.getSecond());
        });
        errorTexture = sprites.get(FlareConstants.ERROR_TEXTURE);
        if (errorTexture == null)
            throw new NullPointerException("Error Texture not in atlas!");

        if (loadResult.animationAtlas() != null)
        {
            animationAtlas = loadResult.animationAtlas();
            animationAtlas.key = Key.withNamespace(key.namespace(), "animation/" + key.id());
        }
    }

    @Override
    void createVkResource(BufferedImage image, VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        if (animationAtlas != null)
        {
            this.frameBuffer = new AnimatedAtlasFrameBuffer(device, image.getWidth(), image.getHeight(), VK_FORMAT_R8G8B8A8_UNORM,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT);
            this.frameBuffer.fromImage(image, device, commands, graphicsQueue);
            this.frameBuffer.createRenderPass();
            this.frameBuffer.createFrameBuffer();

            Texture texture = new Texture();
            texture.textureImage = frameBuffer.image;
            texture.textureImageMemory = frameBuffer.imageMemory;
            texture.width = image.getWidth();
            texture.height = image.getHeight();
            TextureSampler sampler = new TextureSampler(key());
            sampler.texture = texture;
            sampler.textureImageView = frameBuffer.imageView;
            sampler.textureSampler = sampler.createSampler(device, VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_NEAREST, false);
            this.sampler = sampler;
            FlareRegistries.SAMPLER.register(sampler);

        } else
        {
            Texture texture = new Texture();
            texture.createTextureImageFromBufferedImage(device, image, commands.commandPool, graphicsQueue);
            TextureSampler sampler = new TextureSampler(texture, device, key(), VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_NEAREST, false);
            this.sampler = sampler;
            FlareRegistries.SAMPLER.register(sampler);
        }
    }

    public AnimationAtlas getAnimationAtlas()
    {
        return animationAtlas;
    }

    /// @return immutable copy
    public List<Source> getSources()
    {
        return List.copyOf(sources);
    }

    @Override
    void mergeWith(Atlas other)
    {
        if (other instanceof SpriteAtlas spriteAtlas)
            sources.addAll(spriteAtlas.sources);
        else
            throw new IllegalArgumentException("Can only merge SpriteAtlas with SpriteAtlas");
    }
}
