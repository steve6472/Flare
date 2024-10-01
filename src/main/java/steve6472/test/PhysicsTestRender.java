package steve6472.test;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import org.joml.Matrix4f;
import steve6472.core.registry.Key;
import steve6472.core.util.RandomUtil;
import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.core.FrameInfo;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.render.SBOTransfromArray;
import steve6472.volkaniums.render.StaticModelRenderImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/1/2024
 * Project: Volkaniums <br>
 */
public class PhysicsTestRender extends StaticModelRenderImpl
{
    PhysicsSpace physicsSpace;
    private final List<PhysicsRigidBody> objects = new ArrayList<>();

    @Override
    protected void init(SBOTransfromArray<Model> transfromArray)
    {
        createPhysics(transfromArray);
    }

    private void createPhysics(SBOTransfromArray<Model> transfromArray)
    {
        physicsSpace = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);

        float scaleX = 4;

        // Add a static horizontal plane at y=-1.
        float mass = 1f;
        addPlane(Vector3f.UNIT_Y, -0f);
        addPlane(Vector3f.UNIT_Y.mult(-1), -64f);
        addPlane(Vector3f.UNIT_X, -scaleX);
        addPlane(Vector3f.UNIT_Z, -scaleX);
        addPlane(Vector3f.UNIT_X.mult(-1), -scaleX);
        addPlane(Vector3f.UNIT_Z.mult(-1), -scaleX);

        Model ballModel = VolkaniumsRegistries.STATIC_MODEL.get(Key.defaultNamespace("blockbench/static/ball"));
        Model cubeModel = VolkaniumsRegistries.STATIC_MODEL.get(Key.defaultNamespace("blockbench/static/cube"));

        var ballArea = transfromArray.addArea(ballModel);
        var cubeArea = transfromArray.addArea(cubeModel);

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
        }
    }

    private void addPlane(Vector3f normal, float constant)
    {
        Plane plane = new Plane(normal, constant);
        CollisionShape planeShape = new PlaneCollisionShape(plane);
        float mass = PhysicsBody.massForStatic;
        PhysicsRigidBody floor = new PhysicsRigidBody(planeShape, mass);
        physicsSpace.addCollisionObject(floor);
    }

    @Override
    public void updateTransformArray(SBOTransfromArray<Model> transfromArray, FrameInfo frameInfo)
    {
        physicsSpace.update(frameInfo.frameTime(), 8);

        transfromArray.sort(objects, a -> ((UserObj) a.getUserObject()).modelIndex);
        var lastArea = transfromArray.getAreaByIndex(0);
        for (PhysicsRigidBody body : objects)
        {
            UserObj userObject = (UserObj) body.getUserObject();
            // Because the list is sorted, we can do this
            if (lastArea == null || lastArea.index() != userObject.modelIndex)
                lastArea = transfromArray.getAreaByIndex(userObject.modelIndex);

            Matrix4f jomlMat = toJomlMat(body);
            lastArea.updateTransform(jomlMat);
        }
    }

    private final Matrix4f STORE_MAT4F = new Matrix4f();
    private final Vector3f STORE_VEC3F = new Vector3f();
    private Matrix4f toJomlMat(PhysicsRigidBody body)
    {
        Transform transform = new Transform();
        body.getTransform(transform);
        com.jme3.math.Matrix4f t = transform.toTransformMatrix();
        STORE_MAT4F.set(
            t.m00, t.m10, t.m20, t.m30,   // First row
            t.m01, t.m11, t.m21, t.m31,   // Second row
            t.m02, t.m12, t.m22, t.m32,   // Third row
            t.m03, t.m13, t.m23, t.m33    // Fourth row
        );

        if (body.getCollisionShape() instanceof SphereCollisionShape coll)
        {
            body.getPhysicsLocation(STORE_VEC3F);
            STORE_MAT4F.scale(coll.getRadius() * 2f);
        } else if (body.getCollisionShape() instanceof BoxCollisionShape coll)
        {
            coll.getHalfExtents(STORE_VEC3F);
            STORE_MAT4F.scale(STORE_VEC3F.x * 2f, STORE_VEC3F.y * 2f, STORE_VEC3F.z * 2f);
        }

        return STORE_MAT4F;
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
