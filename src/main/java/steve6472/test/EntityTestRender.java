package steve6472.test;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import steve6472.core.registry.Key;
import steve6472.flare.assets.model.Model;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.render.SBOTransfromArray;
import steve6472.flare.render.StaticModelRenderImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/1/2024
 * Project: Flare <br>
 */
class EntityTestRender extends StaticModelRenderImpl
{
    @Override
    protected void init(SBOTransfromArray<Model> transfromArray)
    {
        createPhysics(transfromArray);
    }

    private record Entity(int modelIndex, Vector3f position, Vector3f scale, Quaternionf rotation) {}
    private final List<Entity> entities = new ArrayList<>(4);

    private void createPhysics(SBOTransfromArray<Model> transfromArray)
    {
        var ball = transfromArray.addArea(FlareRegistries.STATIC_MODEL.get(Key.withNamespace("test", "blockbench/static/ball")));
        var cube = transfromArray.addArea(FlareRegistries.STATIC_MODEL.get(Key.withNamespace("test", "blockbench/static/cube")));
        var pebble = transfromArray.addArea(FlareRegistries.STATIC_MODEL.get(Key.withNamespace("test", "blockbench/static/mesh_pebble")));
        var rainbowInAPot = transfromArray.addArea(FlareRegistries.STATIC_MODEL.get(Key.withNamespace("test", "blockbench/static/rainbow_in_a_pot")));
        var fire = transfromArray.addArea(FlareRegistries.STATIC_MODEL.get(Key.withNamespace("test", "blockbench/static/fire")));

        entities.add(new Entity(ball.index(), new Vector3f(2, 0, 0), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(cube.index(), new Vector3f(-2, 0, 0), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(pebble.index(), new Vector3f(0, 0, 2), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(rainbowInAPot.index(), new Vector3f(0, 0, 0), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(fire.index(), new Vector3f(0, 0, -2), new Vector3f(1.0f), new Quaternionf()));
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
