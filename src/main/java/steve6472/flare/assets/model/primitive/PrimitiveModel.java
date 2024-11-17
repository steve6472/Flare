package steve6472.flare.assets.model.primitive;

import steve6472.flare.struct.Struct;
import steve6472.flare.struct.type.StructVertex;

import java.util.List;

/**
 * Created by steve6472
 * Date: 9/22/2024
 * Project: Flare <br>
 */
public interface PrimitiveModel
{
    StructVertex vertexType();
    List<Struct> createVerticies();

    default void checkDataSizeEqual(List<?>... lists)
    {
        if (lists == null || lists.length == 0) return;
        int listSize = lists[0].size();

        for (List<?> list : lists)
        {
            if (list.size() != listSize)
            {
                StringBuilder sizes = new StringBuilder();
                for (List<?> objects : lists)
                {
                    sizes.append(objects.size()).append(", ");
                }
                sizes.setLength(sizes.length() - 2);
                throw new RuntimeException("Data size is not equal! Provided: " + sizes);
            }
        }
    }
}
