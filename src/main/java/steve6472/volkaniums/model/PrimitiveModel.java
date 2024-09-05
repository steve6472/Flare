package steve6472.volkaniums.model;

import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.type.StructVertex;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public class PrimitiveModel
{
    public final List<Vector3f> positions;
    public final List<Vector2f> texCoords;
    public final StructVertex vertexType;

    public PrimitiveModel(StructVertex vertexType)
    {
        this.positions = new ArrayList<>();
        this.texCoords = new ArrayList<>();
        this.vertexType = vertexType;
    }

    @Deprecated(forRemoval = true)
    public List<Struct> toVkVertices()
    {
        if (positions.size() != texCoords.size())
            throw new RuntimeException("Different count of vertices and texture coordinates");

        List<Struct> vertices = new ArrayList<>(positions.size());

        for (int i = 0; i < positions.size(); i++)
        {
            Vector2f uv = texCoords.get(i);
            Vector3f pos = positions.get(i);
            Color color = generateRandomSaturatedColor(pos.x, pos.y, pos.z);
            Struct vertex = vertexType.create(pos, new Vector3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f), uv);
            vertices.add(vertex);
        }
        return vertices;
    }

    @Deprecated(forRemoval = true)
    private static Color generateRandomSaturatedColor(float seed1, float seed2) {
        // Combine the float seeds into a single long seed
        long combinedSeed = Float.floatToIntBits(seed1) ^ Float.floatToIntBits(seed2);

        // Initialize the random number generator with the combined seed
        Random random = new Random(combinedSeed);

        // Generate a random hue value (0.0 to 1.0)
        float hue = random.nextFloat();

        // Saturation and brightness are set to maximum values for a fully saturated color
        float saturation = 1.0f;
        float brightness = 1.0f;

        // Convert HSB (Hue, Saturation, Brightness) to RGB
        return Color.getHSBColor(hue, saturation, brightness);
    }

    @Deprecated(forRemoval = true)
    private static Color generateRandomSaturatedColor(float seed1, float seed2, float seed3) {
        // Combine the float seeds into a single long seed
        long combinedSeed = Float.floatToIntBits(seed1) ^ Float.floatToIntBits(seed2) ^ Float.floatToIntBits(seed3);

        // Initialize the random number generator with the combined seed
        Random random = new Random(combinedSeed);

        // Generate a random hue value (0.0 to 1.0)
        float hue = random.nextFloat();

        // Saturation and brightness are set to maximum values for a fully saturated color
        float saturation = 1.0f;
        float brightness = 1.0f;

        // Convert HSB (Hue, Saturation, Brightness) to RGB
        return Color.getHSBColor(hue, saturation, brightness);
    }
}
