package steve6472.flare.assets.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.registry.Key;
import steve6472.core.util.ImagePacker;
import steve6472.flare.*;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.source.Source;
import steve6472.flare.assets.atlas.source.SourceResult;
import steve6472.flare.core.Flare;
import steve6472.flare.framebuffer.AnimatedAtlasFrameBuffer;
import steve6472.flare.registry.VkSetup;
import steve6472.flare.settings.VisualSettings;
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.flare.util.Obj;
import steve6472.flare.util.PackerUtil;

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
        this.sources = new ArrayList<>(sources);
    }

    @Override
    void create(Map<Atlas, ImagePacker> packerMap)
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
        Map<Key, BufferedImage> images = new HashMap<>(loadResult.entries().size());
        loadResult.entries().forEach((key, pair) -> {
            registerSprite(key, pair.getFirst());
            images.put(key, pair.getSecond());
        });

        // Set error texture
        SpriteEntry errorTexture = getSprite(FlareConstants.ERROR_TEXTURE);
        if (errorTexture == null)
            throw new NullPointerException("Error Texture not in atlas!");
        setErrorTexture(errorTexture);

        Map<String, BufferedImage> toPack = new HashMap<>();
        images.forEach((key, image) -> toPack.put(key.toString(), image));
        ImagePacker imagePacker = PackerUtil.pack(SpriteLoader.STARTING_IMAGE_SIZE, toPack, false);
        packerMap.put(this, imagePacker);
        images.clear();

        if (VisualSettings.GENERATE_STARTUP_ATLAS_DATA.get())
        {
            SamplerLoader.Debug.generateFromAtlasAndImagePacker(SamplerLoader.Debug.getFile("/atlas_data"), this, imagePacker);
        }

        if (!loadResult.animatedAtlasData().isEmpty())
        {
            Obj<ImagePacker> packerGet = Obj.empty();
            animationAtlas = new AnimationAtlas(Key.withNamespace(key().namespace(), "animation/" + key().id()), loadResult.animatedAtlasData(), packerGet);
            packerMap.put(animationAtlas, packerGet.get());
        }

        fixUvs(imagePacker);
    }

    @Override
    TextureSampler createVkResource(BufferedImage image, VkSetup setup)
    {
        if (animationAtlas != null)
        {
            this.frameBuffer = new AnimatedAtlasFrameBuffer(setup.device(), image.getWidth(), image.getHeight(), VK_FORMAT_R8G8B8A8_UNORM,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT);
            this.frameBuffer.fromImage(image, setup.device(), setup.commands(), setup.graphicsQueue());
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
            sampler.textureSampler = sampler.createSampler(setup.device(), VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_NEAREST, false);
            return sampler;
        } else
        {
            Texture texture = new Texture();
            texture.createTextureImageFromBufferedImage(setup.device(), image, setup.commands().commandPool, setup.graphicsQueue());
            return new TextureSampler(texture, setup.device(), key(), VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_NEAREST, false);
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
