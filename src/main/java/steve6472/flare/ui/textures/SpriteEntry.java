package steve6472.flare.ui.textures;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.core.util.BitUtil;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.ui.textures.type.NineSlice;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public record SpriteEntry(Key key, SpriteData data, Vector4f uv, Vector2i pixelSize, int index) implements Keyable
{
    public Struct toStruct()
    {
        Vector4f border = new Vector4f();

        int flags = 0;

        if (data.uiType() instanceof NineSlice nineSlice)
        {
            border.set(nineSlice.border());
            flags |= 0b1;
            flags = BitUtil.setBit(flags, 2, nineSlice.stretch().inner());
            flags = BitUtil.setBit(flags, 3, nineSlice.stretch().left());
            flags = BitUtil.setBit(flags, 4, nineSlice.stretch().right());
            flags = BitUtil.setBit(flags, 5, nineSlice.stretch().top());
            flags = BitUtil.setBit(flags, 6, nineSlice.stretch().bottom());
        }

        return SBO.SPRITE_ENTRY.create(
            uv,
            border,

            flags,
            0,
            new Vector2f(pixelSize)
        );
    }
}
