package steve6472.flare.assets.atlas;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.flare.Commands;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.struct.Struct;
import steve6472.flare.ui.textures.SpriteEntry;

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

    protected final Map<Key, SpriteEntry> sprites = new HashMap<>();
    protected SpriteEntry errorTexture;
    /// Set from SpriteLoader
    TextureSampler sampler = null;

    /// Used only to transfer data
    @ApiStatus.Internal
    Map<Key, BufferedImage> tempImages;

    abstract void mergeWith(Atlas other);
    abstract void create();
    abstract void createVkResource(BufferedImage image, VkDevice device, Commands commands, VkQueue graphicsQueue);

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

    public TextureSampler getSampler()
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
}
