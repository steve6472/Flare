package steve6472.volkaniums.render.debug.objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Vertex;

import java.util.List;

/**
 * Created by steve6472
 * Date: 10/11/2024
 * Project: Volkaniums <br>
 */
public record Hemisphere(float radius, float height, int quality, boolean isTop, Vector4f color) implements DebugObject
{
    @Override
    public void addVerticies(List<Struct> vertices, Matrix4f transform)
    {
        int latSegments = quality; // Latitude lines
        int lonSegments = quality + 3; // Longitude lines

        for (int i = 0; i < latSegments; i++)
        {
            float lat1 = (float) (Math.PI / 2 * i / latSegments);
            float lat2 = (float) (Math.PI / 2 * (i + 1) / latSegments);

            if (!isTop)
            {
                lat1 = (float) (-Math.PI / 2 * i / latSegments);
                lat2 = (float) (-Math.PI / 2 * (i + 1) / latSegments);
            }

            for (int j = 0; j < lonSegments; j++)
            {
                float lon1 = (float) (2.0 * Math.PI * j / lonSegments);
                float lon2 = (float) (2.0 * Math.PI * (j + 1) / lonSegments);

                // Convert polar coordinates to Cartesian for both latitude circles
                Vector3f v1 = polarToCartesian(radius, lat1, lon1, isTop ? height : -height).mulPosition(transform);
                Vector3f v2 = polarToCartesian(radius, lat1, lon2, isTop ? height : -height).mulPosition(transform);
                Vector3f v3 = polarToCartesian(radius, lat2, lon1, isTop ? height : -height).mulPosition(transform);
                Vector3f v4 = polarToCartesian(radius, lat2, lon2, isTop ? height : -height).mulPosition(transform);

                // Add lines connecting latitudes and longitudes
                vertices.add(Vertex.POS3F_COL4F.create(v1, color));
                vertices.add(Vertex.POS3F_COL4F.create(v2, color));  // Horizontal line on current latitude

                vertices.add(Vertex.POS3F_COL4F.create(v1, color));
                vertices.add(Vertex.POS3F_COL4F.create(v3, color));  // Vertical line between latitudes
            }
        }
    }

    private static Vector3f polarToCartesian(float radius, float lat, float lon, float offsetY)
    {
        float x = radius * (float) Math.cos(lat) * (float) Math.cos(lon);
        float z = radius * (float) Math.cos(lat) * (float) Math.sin(lon);
        float y = radius * (float) Math.sin(lat) + offsetY;
        return new Vector3f(x, y, z);
    }
}
