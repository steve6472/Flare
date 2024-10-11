package steve6472.volkaniums.assets.model.blockbench;

import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.core.registry.Key;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.assets.model.blockbench.element.CubeElement;
import steve6472.volkaniums.assets.model.blockbench.outliner.OutlinerUUID;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by steve6472
 * Date: 9/25/2024
 * Project: Volkaniums <br>
 */
public final class ErrorModel
{
    private ErrorModel() {}

    private static final UUID CUBE_ELEMENT_UUID = UUID.randomUUID();
    private static final String CUBE_ELEMENT_NAME = "*error_model*";

    public static final String VERSION = "null";

    public static final Key KEY = Key.withNamespace(Constants.ENGINE_NAMESPACE, "error_model");

    public static final LoadedModel INSTANCE = new LoadedModel(
        new ModelMeta(VERSION, ModelFormat.FREE, false),
        KEY,
        new Resolution(2, 2),
        List.of(new CubeElement(
            CUBE_ELEMENT_UUID,
            CUBE_ELEMENT_NAME,
            new Vector3f(-0.5f, -0.5f, -0.5f),
            new Vector3f(0.5f, 0.5f, 0.5f),
            new Vector3f(),
            new Vector3f(),
            0f,
            Map.of(
                FaceType.NORTH, new CubeFace(new Vector4f(0, 0, 2, 2), 0),
                FaceType.EAST, new CubeFace(new Vector4f(0, 0, 2, 2), 0),
                FaceType.SOUTH, new CubeFace(new Vector4f(0, 0, 2, 2), 0),
                FaceType.WEST, new CubeFace(new Vector4f(0, 0, 2, 2), 0),
                FaceType.UP, new CubeFace(new Vector4f(0, 0, 2, 2), 0),
                FaceType.DOWN, new CubeFace(new Vector4f(0, 0, 2, 2), 0))
        )),
        List.of(new OutlinerUUID(CUBE_ELEMENT_UUID)),
        List.of(new TextureData(0, 2, 2, 2, 2, Constants.ERROR_TEXTURE)),
        List.of()
    );

    /// Buffer is filled from [BlockbenchLoader]
    public static final Model VK_STATIC_INSTANCE = new Model(KEY);

    /// Buffer is filled from [BlockbenchLoader]
    public static final Model VK_ANIMATED_INSTANCE = new Model(KEY);

    public static final BufferedImage IMAGE = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);

    static
    {
        IMAGE.setRGB(0, 0, 0xff000000);
        IMAGE.setRGB(1, 0, 0xffff00ff);
        IMAGE.setRGB(0, 1, 0xffff00ff);
        IMAGE.setRGB(1, 1, 0xff000000);
    }
}
