package steve6472.volkaniums.model;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import steve6472.volkaniums.model.anim.Animation;
import steve6472.volkaniums.struct.def.Vertex;

import java.util.*;

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

    public Animation getAnimationByName(String name)
    {
        return animations.stream().filter(anim -> anim.name().equals(name)).findFirst().orElse(null);
    }

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

    public PrimitiveSkinModel toPrimitiveSkinModel()
    {
        PrimitiveSkinModel model = new PrimitiveSkinModel(Vertex.SKIN, this);

        Map<UUID, Element> elementMap = new HashMap<>(elements.size());
        for (Element element : elements)
        {
            elementMap.put(element.uuid(), element);
        }

        //        Stack<Matrix4f> matrixStack = new ObjectArrayList<>(32);
        //        matrixStack.push(new Matrix4f());

        for (OutlinerUUID outliner : outliner)
        {
            recursiveOutlinerSkin(outliner, null, new Matrix4f(), elementMap, model);
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

    private void recursiveOutlinerSkin(OutlinerUUID outliner, OutlinerUUID parent, Matrix4f parentTransform, Map<UUID, Element> elementMap, PrimitiveSkinModel model)
    {
        if (outliner instanceof OutlinerElement el)
        {
            Matrix4f transformMatrix = new Matrix4f(parentTransform);
            transformMatrix.translate(el.origin());
            transformMatrix.rotateZ(el.rotation().z);
            transformMatrix.rotateY(el.rotation().y);
            transformMatrix.rotateX(el.rotation().x);
            transformMatrix.translate(-el.origin().x, -el.origin().y, -el.origin().z);

            model.skinData.transformations.put(el.uuid(), new Pair<>(model.skinData.transformations.size() + 1, transformMatrix));

            List<OutlinerUUID> children = el.children();
            for (OutlinerUUID child : children)
            {
                recursiveOutlinerSkin(child, outliner, transformMatrix, elementMap, model);
            }
        } else
        {
            Element element = elementMap.get(outliner.uuid());
            if (element == null)
                throw new RuntimeException("Element " + outliner.uuid + " is null!");

            List<Vector3f> vertices = element.toVertices();
            List<Integer> bones1 = new ArrayList<>(vertices.size());

            int boneIndex = 0;
            if (parent != null)
            {
                Pair<Integer, Matrix4f> integerMatrix4fPair = model.skinData.transformations.get(parent.uuid());
                if (integerMatrix4fPair != null)
                    boneIndex = integerMatrix4fPair.getFirst();
            }

            for (int i = 0; i < vertices.size(); i++)
            {
                bones1.add(boneIndex);
            }

            model.positions.addAll(vertices);
            model.texCoords.addAll(element.toUVs());
            model.transformationIndicies.addAll(bones1);
        }
    }
}
