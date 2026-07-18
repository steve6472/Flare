package steve6472.flare.assets.atlas;

import org.jetbrains.annotations.ApiStatus;
import steve6472.core.registry.Holder;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.core.util.ImagePacker;
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
    private final Map<Key, SpriteEntry> sprites = new HashMap<>();
    private Key key;
    private SpriteEntry errorTexture;

    /// Set from SpriteLoader
    public Holder<TextureSampler> sampler = null;

    abstract void mergeWith(Atlas other);
    abstract void create(Map<Atlas, ImagePacker> packerMap);
    abstract TextureSampler createVkResource(BufferedImage image, VkSetup setup);

    /// Just don't
    @ApiStatus.Internal
    public Map<Key, SpriteEntry> getSprites()
    {
        // TODO: replace omfg
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
        Struct[] textureSettings = new Struct[sprites.size()];
        sprites.forEach((_, entry) -> textureSettings[entry.index()] = entry.toStruct());

        return textureSettings;
    }

    /*
     *
     */

    protected void fixUvs(ImagePacker packer)
    {
        float texel = 1f / packer.getImage().getWidth();
        sprites.forEach((key, entry) -> {
            Rectangle rectangle = packer.getRects().get(key.toString());
            Objects.requireNonNull(rectangle, "Texture data not found in ImagePacker, for " + key);
            entry.uv().set(
                (rectangle.x) * texel,
                (rectangle.y) * texel,
                (rectangle.x + rectangle.width) * texel,
                (rectangle.y + rectangle.height) * texel
            );
        });
    }

    void setKey(Key key)
    {
        this.key = key;
    }

    protected void registerSprite(Key key, SpriteEntry entry)
    {
        sprites.put(key, entry);
    }

    protected void setErrorTexture(SpriteEntry errorTexture)
    {
        this.errorTexture = errorTexture;
    }
}
