package steve6472.flare.render.impl;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.flare.FlareConstants;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Vertex;
import steve6472.flare.ui.textures.SpriteEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 12/1/2024
 * Project: Flare <br>
 */
public abstract class UIRenderImpl
{
    private static final Logger LOGGER = Log.getLogger(UIRenderImpl.class);
    private static final Set<String> MISSING_TEXTURES = new HashSet<>(16);
    private List<Struct> structList;

    protected static final Vector3f NO_TINT = new Vector3f(1.0f);

    public abstract void render();

    public void setStructList(List<Struct> structs)
    {
        structList = structs;
    }

    protected final void sprite(int x, int y, float zIndex, int width, int height, int pixelWidth, int pixelHeight, @NotNull Key textureKey)
    {
        sprite(x, y, zIndex, width, height, pixelWidth, pixelHeight, NO_TINT, textureKey);
    }

    protected final SpriteEntry getTextureEntry(Key textureKey)
    {
        SpriteEntry uiTextureEntry = FlareRegistries.ATLAS.get(FlareConstants.ATLAS_UI).getSprites().get(textureKey);
        if (uiTextureEntry == null)
        {
            if (!MISSING_TEXTURES.contains(textureKey.toString()))
            {
                MISSING_TEXTURES.add(textureKey.toString());
                LOGGER.warning("Missing UI Texture for " + textureKey);
            }
        }
        return uiTextureEntry;
    }

    protected final void sprite(int x, int y, float zIndex, int width, int height, int pixelWidth, int pixelHeight, Vector3f tint, @NotNull Key textureKey)
    {
        createSprite(x, y, zIndex, width, height, pixelWidth, pixelHeight, tint, getTextureEntry(textureKey));
    }

    protected final void createSprite(
        int x, int y, float zIndex,
        int width, int height,
        int pixelWidth, int pixelHeight,
        Vector3f tint,
        SpriteEntry texture)
    {
        int index;
        if (texture == null)
        {
            index = 0;
        } else
        {
            index = texture.index();
        }

        // Fit zIndex to 0 - 0.1 range
        zIndex /= 256f;
        zIndex /= 10f;

        // Define base vertices
        Vector3f vtl = new Vector3f(x, y , zIndex);
        Vector3f vbl = new Vector3f(x, y + height, zIndex);
        Vector3f vbr = new Vector3f(x + width, y + height, zIndex);
        Vector3f vtr = new Vector3f(x + width, y, zIndex);
        //noinspection SuspiciousNameCombination
        Vector3f vertexData = new Vector3f(index, pixelWidth, pixelHeight);

        vertex(vtl, tint, vertexData);
        vertex(vbl, tint, vertexData);
        vertex(vbr, tint, vertexData);

        vertex(vbr, tint, vertexData);
        vertex(vtr, tint, vertexData);
        vertex(vtl, tint, vertexData);
    }

    protected final void vertex(Vector3f position, Vector3f tint, Vector3f data)
    {
        structList.add(Vertex.POS3F_COL3F_DATA3F.create(position, tint, data));
    }
}
