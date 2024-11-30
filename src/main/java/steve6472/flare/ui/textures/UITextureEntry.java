package steve6472.flare.ui.textures;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.core.util.BitUtil;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.ui.textures.type.NineSliceTexture;
import steve6472.flare.ui.textures.type.UITexture;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public record UITextureEntry(Key key, UITexture uiTexture, Vector4f uv, Vector2i pixelSize, int index) implements Keyable
{
    public Struct toStruct()
    {
        Vector4f border = new Vector4f();

        int flags = 0;

        if (uiTexture instanceof NineSliceTexture nineSlice)
        {
            border.set(nineSlice.border());
            flags |= 0b1;
            flags = BitUtil.setBit(flags, 2, nineSlice.stretchInner());
        }

        return SBO.UI_TEXTURE_ENTRY.create(
            uv,
            border,

            flags,
            0,
            new Vector2f(pixelSize)
        );
    }
}
