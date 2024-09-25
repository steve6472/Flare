package steve6472.volkaniums.render;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import steve6472.volkaniums.*;
import steve6472.volkaniums.assets.model.Model;
import steve6472.volkaniums.assets.model.blockbench.ErrorModel;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.registry.Key;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Push;
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
    private DescriptorPool globalPool;
    private DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frame = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);

    public BBStaticModelRenderSystem(MasterRenderer masterRenderer, Pipeline pipeline)
    {
        super(masterRenderer, pipeline);

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, MAX_FRAMES_IN_FLIGHT)
            .build();

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            frame.add(new FlightFrame());

            VkBuffer global = new VkBuffer(
                device,
                UBO.GLOBAL_UBO.sizeof(),
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            global.map();
            frame.get(i).uboBuffer = global;

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
                frame.get(i).descriptorSet = descriptorWriter
                    .writeBuffer(0, frame.get(i).uboBuffer, stack)
                    .writeImage(1, Registries.SAMPLER.get(Constants.BLOCKBENCH_TEXTURE), stack)
                    .build();
            }
        }
    }

    @Override
    public long[] setLayouts()
    {
        return new long[] {globalSetLayout.descriptorSetLayout};
    }

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        FlightFrame flightFrame = frame.get(frameInfo.frameIndex);
        // Update

        var globalUBO = UBO.GLOBAL_UBO.create(frameInfo.camera.getProjectionMatrix(), frameInfo.camera.getViewMatrix(), new Matrix4f[] {
//            new Matrix4f().translate(0, -1f, 0),
//            new Matrix4f(),
//            new Matrix4f().translate(0, 1f, 0),
//            new Matrix4f().rotateZ((float) (Math.PI * 0.25f))
            new Matrix4f().translate(-1.5f, -0.75f, 0).rotateY(0),
            new Matrix4f().translate(-0.5f, -0.75f, 0).rotateY((float) Math.PI * 0.5f),
            new Matrix4f().translate(0.5f, -0.75f, 0).rotateY((float) Math.PI),
            new Matrix4f().translate(1.5f, -0.75f, 0).rotateY((float) Math.PI * 1.5f)

//            new Matrix4f().translate(-1.5f, -0.75f, 0).scale(1f / 16f),
//            new Matrix4f().translate(-0.5f, -0.75f, 0).scale(1f / 16f),
//            new Matrix4f().translate(0.5f, -0.75f, 0).scale(1f / 16f),
//            new Matrix4f().translate(1.5f, -0.75f, 0).scale(1f / 16f)
        });

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_UBO::memcpy, globalUBO);
        flightFrame.uboBuffer.flush();

        pipeline.bind(frameInfo.commandBuffer);

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer,
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline.pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            null);

        for (int j = 0; j < 4; j++)
        {
            Struct push = Push.PUSH.create(new Matrix4f(),
                new Vector4f(0.3f, 0.3f, 0.3f, 1.0f),
                j);

            Push.PUSH.push(push, frameInfo.commandBuffer, pipeline.pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 0);

            Model model = Registries.STATIC_MODEL.get(Key.defaultNamespace("blockbench/static/mesh_pebble"));

            model.bind(frameInfo.commandBuffer);
            model.draw(frameInfo.commandBuffer);
        }
    }

    @Override
    public void cleanup()
    {
        globalSetLayout.cleanup();
        globalPool.cleanup();

        for (FlightFrame flightFrame : frame)
            flightFrame.uboBuffer.cleanup();
    }

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        long descriptorSet;
    }
}
