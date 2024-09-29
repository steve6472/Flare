package steve6472.volkaniums.render;

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
import org.lwjgl.system.MemoryStack;
import steve6472.core.registry.Key;
import steve6472.core.util.RandomUtil;
import steve6472.volkaniums.*;
import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.assets.model.blockbench.ErrorModel;
import steve6472.volkaniums.core.FrameInfo;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.pipeline.builder.PipelineConstructor;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Push;
import steve6472.volkaniums.struct.def.SBO;
import steve6472.volkaniums.struct.def.UBO;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static steve6472.volkaniums.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public class BBStaticModelRenderSystem extends RenderSystem
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    private final SBOTransfromArray<Model> transfromArray = new SBOTransfromArray<>(ErrorModel.VK_STATIC_INSTANCE);

    public BBStaticModelRenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
    {
        super(masterRenderer, pipeline);

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT)
            .addBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, MAX_FRAMES_IN_FLIGHT)
            .build();

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            FlightFrame frame = new FlightFrame();
            frames.add(frame);

            VkBuffer global = new VkBuffer(
                device,
                UBO.GLOBAL_CAMERA_UBO.sizeof(),
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            global.map();
            frame.uboBuffer = global;

            VkBuffer sbo = new VkBuffer(
                device,
                SBO.TRANSFORMATIONS.sizeof(),
                1,
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            sbo.map();
            frame.sboBuffer = sbo;

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
                frame.descriptorSet = descriptorWriter
                    .writeBuffer(0, frame.uboBuffer, stack, UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT)
                    .writeImage(1, VolkaniumsRegistries.SAMPLER.get(Constants.BLOCKBENCH_TEXTURE), stack)
                    .writeBuffer(2, frame.sboBuffer, stack)
                    .build();
            }
        }

        createPhysics();
    }

    PhysicsSpace physicsSpace;
    private static List<PhysicsRigidBody> objects = new ArrayList<>();

    private void createPhysics()
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

        transfromArray.addArea(ballModel);
        transfromArray.addArea(cubeModel);

        var ballArea = transfromArray.getAreaByType(ballModel);
        var cubeArea = transfromArray.getAreaByType(cubeModel);

        for (int i = 0; i < 8; i++)
        {
            // Add a sphere-shaped, dynamic, rigid body at the origin.
            float radius = RandomUtil.randomFloat(0.25f, 0.75f);
            CollisionShape shape = new SphereCollisionShape(radius);
            PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
            physicsSpace.add(body);
            body.setPhysicsLocation(new Vector3f(RandomUtil.randomFloat(-scaleX, scaleX), RandomUtil.randomFloat(4, 16), RandomUtil.randomFloat(-scaleX, scaleX)));
            body.setUserObject(new UserObj(ballArea.index));
            objects.add(body);
        }

        for (int i = 0; i < 8; i++)
        {
            // Add a sphere-shaped, dynamic, rigid body at the origin.
            float radius = RandomUtil.randomFloat(0.25f, 0.75f);
            CollisionShape shape = new BoxCollisionShape(radius);
            PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
            physicsSpace.add(body);
            body.setPhysicsLocation(new Vector3f(RandomUtil.randomFloat(-scaleX, scaleX), RandomUtil.randomFloat(4, 16), RandomUtil.randomFloat(-scaleX, scaleX)));
            body.setUserObject(new UserObj(cubeArea.index));
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
    public long[] setLayouts()
    {
        return new long[] {globalSetLayout.descriptorSetLayout};
    }

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        FlightFrame flightFrame = frames.get(frameInfo.frameIndex());

        physicsSpace.update(frameInfo.frameTime(), 8);

        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(frameInfo.camera().getProjectionMatrix(), frameInfo.camera().getViewMatrix());
        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_CAMERA_UBO::memcpy, List.of(globalUBO), singleInstanceSize, singleInstanceSize * frameInfo.camera().cameraIndex);
        flightFrame.uboBuffer.flush(singleInstanceSize, (long) singleInstanceSize * frameInfo.camera().cameraIndex);

        updateSbo(flightFrame.sboBuffer);

        pipeline().bind(frameInfo.commandBuffer());

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer(),
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline().pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            stack.ints(singleInstanceSize * frameInfo.camera().cameraIndex));

        int totalIndex = 0;
        for (var area : transfromArray.getAreas())
        {
            if (area.toRender == 0)
                continue;

            Struct offset = Push.STATIC_TRANSFORM_OFFSET.create(totalIndex);
            Push.STATIC_TRANSFORM_OFFSET.push(offset, frameInfo.commandBuffer(), pipeline().pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0);
            area.modelType.bind(frameInfo.commandBuffer());
            area.modelType.draw(frameInfo.commandBuffer(), area.toRender);
            totalIndex += area.toRender;
        }
    }

    private void updateSbo(VkBuffer sboBuffer)
    {
        transfromArray.start();
        transfromArray.sort(objects, a -> ((UserObj) a.getUserObject()).modelIndex);
        var lastArea = transfromArray.getAreaByIndex(0);
        for (PhysicsRigidBody body : objects)
        {
            UserObj userObject = (UserObj) body.getUserObject();
            // Because the list is sorted, we can do this
            if (lastArea == null || lastArea.index != userObject.modelIndex)
                lastArea = transfromArray.getAreaByIndex(userObject.modelIndex);

            Matrix4f jomlMat = toJomlMat(body);
            lastArea.updateTransform(jomlMat);
        }

        var sbo = SBO.TRANSFORMATIONS.create(transfromArray.getTransformsArray());

        sboBuffer.writeToBuffer(SBO.TRANSFORMATIONS::memcpy, sbo);
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

    @Override
    public void cleanup()
    {
        globalSetLayout.cleanup();
        globalPool.cleanup();

        for (FlightFrame flightFrame : frames)
        {
            flightFrame.uboBuffer.cleanup();
            flightFrame.sboBuffer.cleanup();
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

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        VkBuffer sboBuffer;
        long descriptorSet;
    }
}
