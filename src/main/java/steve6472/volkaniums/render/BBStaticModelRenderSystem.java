package steve6472.volkaniums.render;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import steve6472.volkaniums.*;
import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.registry.Key;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Push;
import steve6472.volkaniums.struct.def.SBO;
import steve6472.volkaniums.struct.def.UBO;
import steve6472.volkaniums.util.RandomUtil;

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
    private DescriptorPool globalPool;
    private DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);

    public BBStaticModelRenderSystem(MasterRenderer masterRenderer, Pipeline pipeline)
    {
        super(masterRenderer, pipeline);

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT)
            .addBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, MAX_FRAMES_IN_FLIGHT)
            .build();

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            FlightFrame frame = new FlightFrame();
            frames.add(frame);

            VkBuffer global = new VkBuffer(
                device,
                UBO.STATIC_BB_MODEL_UBO.sizeof(),
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
                    .writeBuffer(0, frame.uboBuffer, stack)
                    .writeImage(1, Registries.SAMPLER.get(Constants.BLOCKBENCH_TEXTURE), stack)
                    .writeBuffer(2, frame.sboBuffer, stack)
                    .build();
            }
        }

        createPhysics();
    }

    PhysicsSpace physicsSpace;
    private static List<PhysicsRigidBody> balls = new ArrayList<>();

    private void createPhysics()
    {
        physicsSpace = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);

        // Add a static horizontal plane at y=-1.
        float mass;
        addPlane(Vector3f.UNIT_Y, -1f);
        addPlane(Vector3f.UNIT_Y.mult(-1), -64f);
        addPlane(Vector3f.UNIT_X, -160f);
        addPlane(Vector3f.UNIT_Z, -160f);
        addPlane(Vector3f.UNIT_X.mult(-1), -160f);
        addPlane(Vector3f.UNIT_Z.mult(-1), -160f);

        for (int i = 0; i < 1000; i++)
        {
            // Add a sphere-shaped, dynamic, rigid body at the origin.
            float radius = RandomUtil.randomFloat(0.25f, 0.75f);
            CollisionShape ballShape = new SphereCollisionShape(radius);
            mass = 1f;
            PhysicsRigidBody ball = new PhysicsRigidBody(ballShape, mass);
            ball.setRestitution(2f);
            physicsSpace.add(ball);
            ball.setPhysicsLocation(new Vector3f(RandomUtil.randomFloat(-160, 160), RandomUtil.randomFloat(4, 16), RandomUtil.randomFloat(-160, 160)));
            balls.add(ball);
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
        FlightFrame flightFrame = frames.get(frameInfo.frameIndex);
        // Update

        physicsSpace.update(frameInfo.frameTime);

        var globalUBO = UBO.STATIC_BB_MODEL_UBO.create(frameInfo.camera.getProjectionMatrix(), frameInfo.camera.getViewMatrix());

        flightFrame.uboBuffer.writeToBuffer(UBO.STATIC_BB_MODEL_UBO::memcpy, globalUBO);
        flightFrame.uboBuffer.flush();

        updateSbo(flightFrame.sboBuffer);

        pipeline.bind(frameInfo.commandBuffer);

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer,
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline.pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            null);

        Struct push = Push.STATIC.create(0);

        Push.STATIC.push(push, frameInfo.commandBuffer, pipeline.pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0);

        Model model = Registries.STATIC_MODEL.get(Key.defaultNamespace("blockbench/static/ball"));

        model.bind(frameInfo.commandBuffer);
        model.draw(frameInfo.commandBuffer, 32768);
    }

    private void updateSbo(VkBuffer sboBuffer)
    {
        List<Matrix4f> transforms = new ArrayList<>();
        for (PhysicsRigidBody ball : balls)
        {
            Transform transform = new Transform();
            ball.getTransform(transform);
            com.jme3.math.Matrix4f t = transform.toTransformMatrix();
            Matrix4f mat = new Matrix4f(
                t.m00, t.m10, t.m20, t.m30,   // First row
                t.m01, t.m11, t.m21, t.m31,   // Second row
                t.m02, t.m12, t.m22, t.m32,   // Third row
                t.m03, t.m13, t.m23, t.m33    // Fourth row
            );
            mat.scale(((SphereCollisionShape) ball.getCollisionShape()).getRadius() * 2f);
            transforms.add(mat);
        }

//        for (int i = 0; i < 32; i++)
//        {
//            for (int j = 0; j < 32; j++)
//            {
//                for (int k = 0; k < 32; k++)
//                {
//                    transforms.add(new Matrix4f().translate(i - 16, j - 16, k - 16));
//                }
//            }
//        }

        var sbo = SBO.TRANSFORMATIONS.create((Object) transforms.toArray(Matrix4f[]::new));

        sboBuffer.writeToBuffer(SBO.TRANSFORMATIONS::memcpy, sbo);
        sboBuffer.flush();
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

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        VkBuffer sboBuffer;
        long descriptorSet;
    }
}
