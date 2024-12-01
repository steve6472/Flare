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

import javax.xml.crypto.dsig.Transform;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/1/2024
 * Project: Flare <br>
 */
class PhysicsTestRender extends StaticModelRenderImpl
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

        entities.add(new Entity(ball.index(), new Vector3f(2, 0, 0), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(cube.index(), new Vector3f(-2, 0, 0), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(pebble.index(), new Vector3f(0, 0, 2), new Vector3f(1.0f), new Quaternionf()));
        entities.add(new Entity(rainbowInAPot.index(), new Vector3f(0, 0, 0), new Vector3f(1.0f), new Quaternionf()));

        /*

        for (int i = 0; i < TestSettings.SPHERE_AMOUNT.get(); i++)
        {
            // Add a sphere-shaped, dynamic, rigid body at the origin.
            float radius = RandomUtil.randomFloat(0.25f, 0.75f);
            CollisionShape shape = new SphereCollisionShape(radius);
            PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
            physicsSpace.add(body);
            body.setPhysicsLocation(new Vector3f(RandomUtil.randomFloat(-scaleX, scaleX), RandomUtil.randomFloat(4, 16), RandomUtil.randomFloat(-scaleX, scaleX)));
            body.setUserObject(new UserObj(ballArea.index()));
            objects.add(body);
        }

        for (int i = 0; i < TestSettings.CUBE_AMOUNT.get(); i++)
        {
            // Add a sphere-shaped, dynamic, rigid body at the origin.
            float radius = RandomUtil.randomFloat(0.25f, 0.75f);
            CollisionShape shape = new BoxCollisionShape(radius);
            PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
            physicsSpace.add(body);
            body.setPhysicsLocation(new Vector3f(RandomUtil.randomFloat(-scaleX, scaleX), RandomUtil.randomFloat(4, 16), RandomUtil.randomFloat(-scaleX, scaleX)));
            body.setUserObject(new UserObj(cubeArea.index()));
            objects.add(body);
        }*/
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

    static final class UserObj
    {
        public int modelIndex;

        UserObj(int modelIndex)
        {
            this.modelIndex = modelIndex;
        }
    }
}
