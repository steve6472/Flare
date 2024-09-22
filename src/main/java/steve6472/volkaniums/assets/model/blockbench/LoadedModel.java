package steve6472.volkaniums.assets.model.blockbench;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import steve6472.volkaniums.assets.model.blockbench.anim.Animation;
import steve6472.volkaniums.assets.model.blockbench.element.CubeElement;
import steve6472.volkaniums.assets.model.blockbench.element.MeshElement;
import steve6472.volkaniums.assets.model.blockbench.outliner.OutlinerElement;
import steve6472.volkaniums.assets.model.blockbench.outliner.OutlinerUUID;
import steve6472.volkaniums.assets.model.primitive.PrimitiveStaticModel;
import steve6472.volkaniums.assets.model.primitive.PrimitiveSkinModel;
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

    public LoadedModel
    {
        fixResolution(resolution, elements);
    }

    /// @deprecated will be removed after atlas creation is in place
    @Deprecated(forRemoval = true)
    private static void fixResolution(Resolution resolution, List<Element> elements)
    {
        float scaleX = 1f / resolution.width();
        float scaleY = 1f / resolution.height();

        for (Element element : elements)
        {
            switch (element)
            {
                case CubeElement(UUID _, Vector3f _, Vector3f _, Vector3f _, float _, Map<FaceType, CubeFace> faces) -> faces.forEach((_, face) -> face.uv().mul(scaleX, scaleY, scaleX, scaleY));
                case MeshElement(UUID _, Vector3f _, Vector3f _, Map<String, Vector3f> _, Map<String, MeshFace> faces) -> faces.forEach((_, face) -> face.uv().forEach((_, uv) -> uv.mul(scaleX, scaleY)));
                default -> {}
            }
        }
    }

    public Animation getAnimationByName(String name)
    {
        return animations.stream().filter(anim -> anim.name().equals(name)).findFirst().orElse(null);
    }

    public PrimitiveStaticModel toPrimitiveModel()
    {
        PrimitiveStaticModel model = new PrimitiveStaticModel(Vertex.POS3F_NORMAL_UV);

        Map<UUID, Element> elementMap = new HashMap<>(elements.size());
        for (Element element : elements)
        {
            elementMap.put(element.uuid(), element);
        }

        for (OutlinerUUID outlinerUUID : outliner)
        {
            recursiveOutliner(outlinerUUID, elementMap, model, new Matrix4f());
        }

        return model;
    }

    private void recursiveOutliner(OutlinerUUID outliner, Map<UUID, Element> elementMap, PrimitiveStaticModel model, Matrix4f parentTransform)
    {
        if (outliner instanceof OutlinerElement el)
        {
            Matrix4f transformMatrix = new Matrix4f(parentTransform);
            transformMatrix.translate(el.origin());
            transformMatrix.rotateZ(el.rotation().z);
            transformMatrix.rotateY(el.rotation().y);
            transformMatrix.rotateX(el.rotation().x);
            transformMatrix.translate(-el.origin().x, -el.origin().y, -el.origin().z);

            for (OutlinerUUID child : el.children())
            {
                recursiveOutliner(child, elementMap, model, transformMatrix);
            }
        } else
        {
            Element element = elementMap.get(outliner.uuid());
            if (element == null)
                throw new RuntimeException("Element " + outliner.uuid() + " is null!");

            List<Vector3f> vertices = element.toVertices();
            vertices.forEach(parentTransform::transformPosition);
            model.positions.addAll(vertices);
            List<Vector3f> normals = element.toNormals();
            normals.forEach(normal -> {
                // TODO: fix normals of transformed elements
//                Matrix3f normalMatrix = new Matrix3f();
//                parentTransform.get3x3(normalMatrix);
//                normalMatrix.invert().transpose();
//
//                normalMatrix.transform(normal);
                normal.normalize();
            });
            model.normals.addAll(normals);
            model.texCoords.addAll(element.toUVs());
        }
    }

    public PrimitiveSkinModel toPrimitiveSkinModel()
    {
        PrimitiveSkinModel model = new PrimitiveSkinModel(Vertex.SKIN, this);

        Map<UUID, Element> elementMap = new HashMap<>(elements.size());
        for (Element element : elements)
        {
            elementMap.put(element.uuid(), element);
        }

        for (OutlinerUUID outliner : outliner)
        {
            recursiveOutlinerSkin(outliner, null, new Matrix4f(), elementMap, model);
        }

        return model;
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

            // TODO: instead of setting the transformMatrix to skinData, set an empty Matrix and transform the verticies with parentTransform, just like in static model
//            System.out.println("Adding " + el.uuid() + " with index of " + (model.skinData.transformations.size() + 1));
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
                throw new RuntimeException("Element " + outliner.uuid() + " is null!");

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

    /*
     * Getters for.. stuff
     */

    public <T extends Element> Optional<T> getElementByUUIDWithType(Class<T> clazz, UUID uuid)
    {
        List<Element> list = elements()
            .stream()
            .filter(el -> el.uuid().equals(uuid) && el.getClass().equals(clazz))
            .toList();
        if (list.size() > 1)
            throw new RuntimeException("Too many locators with the same UUID (" + uuid + ")");
        if (list.isEmpty())
            return Optional.empty();
        return Optional.of(clazz.cast(list.getFirst()));
    }

    public <T extends Element> List<T> getElementsWithType(Class<T> clazz)
    {
        return elements()
            .stream()
            .filter(el -> el.getClass().equals(clazz))
            .map(clazz::cast)
            .toList();
    }

    public Optional<Element> getElementByUUID(UUID uuid)
    {
        List<Element> list = elements()
            .stream()
            .filter(el -> el.uuid().equals(uuid))
            .toList();
        if (list.size() > 1)
            throw new RuntimeException("Too many elements with the same UUID (" + uuid + ")");
        if (list.isEmpty())
            return Optional.empty();
        return Optional.of(list.getFirst());
    }

    public Optional<OutlinerUUID> getOutlinerByChildElementUUID(UUID elementUUID)
    {
        for (OutlinerUUID outlinerUUID : outliner())
        {
            if (outlinerUUID instanceof OutlinerElement && outlinerUUID.uuid().equals(elementUUID))
                return Optional.empty();

            Optional<OutlinerUUID> result = recursiveGetOutlinerByChildElementUUID(outlinerUUID, elementUUID);
            if (result.isPresent())
                return result;
        }
        return Optional.empty();
    }

    private Optional<OutlinerUUID> recursiveGetOutlinerByChildElementUUID(OutlinerUUID outlinerUUID, UUID elementUUID)
    {
        if (outlinerUUID instanceof OutlinerElement el)
        {
            for (OutlinerUUID child : el.children())
            {
                if (child.uuid().equals(elementUUID))
                    return Optional.of(outlinerUUID);

                Optional<OutlinerUUID> result = recursiveGetOutlinerByChildElementUUID(child, elementUUID);
                if (result.isPresent())
                    return result;
            }
        }
        return Optional.empty();
    }
}
