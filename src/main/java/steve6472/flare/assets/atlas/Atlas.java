package steve6472.flare.assets.atlas;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.registry.Holder;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.core.util.ImagePacker;
import steve6472.flare.Commands;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.registry.VkSetup;
import steve6472.flare.struct.Struct;
import steve6472.flare.ui.textures.SpriteEntry;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public abstract class Atlas implements Keyable
{
    /// Set from AtlasLoader or SpriteAtlas
    Key key;

    // TODO: this as a registry of sorts, return Holders instead, assuming SpriteEntry holds UV on the atlas
    protected final Map<Key, SpriteEntry> sprites = new HashMap<>();
    protected SpriteEntry errorTexture;
    /// Set from SpriteLoader
    public Holder<TextureSampler> sampler = null;

    /// Used only to transfer data
    /// TODO: actually clear after using it
    @ApiStatus.Internal
    ImagePacker imagePacker;

    abstract void mergeWith(Atlas other);
    abstract void create();
    abstract TextureSampler createVkResource(BufferedImage image, VkSetup setup);

    /// Just don't
    @ApiStatus.Internal
    public Map<Key, SpriteEntry> getSprites()
    {
        return sprites;
    }

    public SpriteEntry getSprite(Key key)
    {
        return sprites.getOrDefault(key, errorTexture);
    }

    public Holder<TextureSampler> getSampler()
    {
        return sampler;
    }

    @Override
    public Key key()
    {
        return key;
    }

    public Struct[] createTextureSettings()
    {
        Collection<Key> keys = sprites.keySet();
        Struct[] textureSettings = new Struct[keys.size()];
        for (Key key : keys)
        {
            SpriteEntry uiTextureEntry = sprites.get(key);
            textureSettings[uiTextureEntry.index()] = uiTextureEntry.toStruct();
        }

        return textureSettings;
    }

    protected void fixUvs(ImagePacker packer)
    {
        float texel = 1f / packer.getImage().getWidth();

        for (Key key : sprites.keySet())
        {
            SpriteEntry uiTextureEntry = sprites.get(key);
            Rectangle rectangle = packer.getRects().get(key.toString());
            Objects.requireNonNull(rectangle, "Texture data not found in ImagePacker, for " + key);
            uiTextureEntry.uv().set(
                (rectangle.x) * texel,
                (rectangle.y) * texel,
                (rectangle.x + rectangle.width) * texel,
                (rectangle.y + rectangle.height) * texel
            );
        }
    }
}
