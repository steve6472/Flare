package steve6472.test;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import steve6472.core.registry.Holder;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.blockbench.ErrorModel;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.registry.BuiltInFlareRegistries;
import steve6472.flare.render.SBOTransfromArray;
import steve6472.flare.render.StaticModelRenderImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by steve6472
 * Date: 7/18/2026
 * Project: Flare <br>
 *
 */
public class StaticRenderSystem extends StaticModelRenderImpl
{
    private record Entity(int modelIndex, Vector3f position, Vector3f scale, Quaternionf rotation) {}
    private final List<Entity> entities = new ArrayList<>(4);

    private Model getModel(String testId)
    {
        Optional<Holder.Reference<Model>> modelReference = BuiltInFlareRegistries.STATIC_MODEL.get(TestApp.key(testId));
        if (modelReference.isPresent())
        {
            return modelReference.get().value();
        } else
        {
            return ErrorModel.VK_STATIC_INSTANCE;
        }
    }

    @Override
    protected void init(SBOTransfromArray<Model> transfromArray)
    {
        entities.clear();

        var ball = transfromArray.addArea(getModel("blockbench/static/ball"));
        var cube = transfromArray.addArea(getModel("blockbench/static/cube"));
        var pebble = transfromArray.addArea(getModel("blockbench/static/mesh_pebble"));
        var rainbowInAPot = transfromArray.addArea(getModel("blockbench/static/rainbow_in_a_pot"));
        var fire = transfromArray.addArea(getModel("blockbench/static/fire"));

        entities.add(new Entity(ball.index(), new Vector3f(2, 0, 0), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(cube.index(), new Vector3f(4, 0, 0), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(pebble.index(), new Vector3f(6, 0, 0), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(rainbowInAPot.index(), new Vector3f(8, 0, 0), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(fire.index(), new Vector3f(10, 0, 0), new Vector3f(1.0f), new Quaternionf()));
    }

    @Override
    public void updateTransformArray(SBOTransfromArray<Model> transfromArray, FrameInfo frameInfo)
    {
        transfromArray.sort(entities, a -> a.modelIndex);
        var lastArea = transfromArray.getAreaByIndex(0);
        for (Entity entity : entities)
        {
            if (lastArea == null || lastArea.index() != entity.modelIndex)
                lastArea = transfromArray.getAreaByIndex(entity.modelIndex);

            Matrix4f mat = new Matrix4f();
            mat.rotate(entity.rotation);
            mat.translate(entity.position);
            mat.scale(entity.scale);
            lastArea.updateTransform(mat);
        }
    }
}
