package steve6472.volkaniums.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.volkaniums.model.anim.Animation;
import steve6472.volkaniums.struct.def.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public record LoadedModel(ModelMeta meta, Resolution resolution, List<Element> elements, List<OutlinerUUID> outliner, List<Texture> textures, List<Animation> animations)
{
    public static Codec<LoadedModel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ModelMeta.CODEC.fieldOf("meta").forGetter(o -> o.meta),
        Resolution.CODEC.fieldOf("resolution").forGetter(o -> o.resolution),
        Element.CODEC.listOf().fieldOf("elements").forGetter(o -> o.elements),
        OutlinerElement.CODEC.listOf().fieldOf("outliner").forGetter(o -> o.outliner),
        Texture.CODEC.listOf().fieldOf("textures").forGetter(o -> o.textures),
        Animation.CODEC.listOf().optionalFieldOf("animations", List.of()).forGetter(o -> o.animations)
    ).apply(instance, LoadedModel::new));

    public PrimitiveModel toPrimitiveModel()
    {
        PrimitiveModel model = new PrimitiveModel(Vertex.POS3F_COL3F_UV);

        Map<UUID, Element> elementMap = new HashMap<>(elements.size());
        for (Element element : elements)
        {
            elementMap.put(element.uuid(), element);
        }

//        Stack<Matrix4f> matrixStack = new ObjectArrayList<>(32);
//        matrixStack.push(new Matrix4f());

        for (OutlinerUUID outlinerUUID : outliner)
        {
            recursiveOutliner(outlinerUUID, elementMap, model);
        }

        return model;
    }

    private void recursiveOutliner(OutlinerUUID outliner, Map<UUID, Element> elementMap, PrimitiveModel model)
    {
        if (outliner instanceof OutlinerElement el)
        {
            for (OutlinerUUID child : el.children())
            {
                recursiveOutliner(child, elementMap, model);
            }
        } else
        {
            Element element = elementMap.get(outliner.uuid());
            if (element == null)
                throw new RuntimeException("Element " + outliner.uuid + " is null!");

            model.positions.addAll(element.toVertices());
            model.texCoords.addAll(element.toUVs());
        }
    }
}
