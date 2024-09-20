package steve6472.volkaniums.render;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.volkaniums.*;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.UBO;
import steve6472.volkaniums.struct.def.Vertex;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.volkaniums.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public class DebugLineRenderSystem extends RenderSystem
{
    private DescriptorPool globalPool;
    private DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frame = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    public List<Model3d> models = new ArrayList<>();
    public List<Model3d> frameModels = new ArrayList<>();

    public DebugLineRenderSystem(MasterRenderer masterRenderer, Pipeline pipeline)
    {
        super(masterRenderer, pipeline);

        createModel(masterRenderer.getCommands(), masterRenderer.getGraphicsQueue());

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, MAX_FRAMES_IN_FLIGHT)
            .build();

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            frame.add(new FlightFrame());

            VkBuffer global = new VkBuffer(
                device,
                UBO.DEBUG_LINE.sizeof(),
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
                    .build();
            }
        }
    }

    private void createModel(Commands commands, VkQueue graphicsQueue)
    {
        final Vector4f RED = new Vector4f(1, 0, 0, 1);
        final Vector4f GREEN = new Vector4f(0, 1, 0, 1);
        final Vector4f BLUE = new Vector4f(0, 0, 1, 1);

        List<Struct> vertices = new ArrayList<>();
        vertices.add(Vertex.POS3F_COL4F.create(new Vector3f(0, 0, 0), RED));
        vertices.add(Vertex.POS3F_COL4F.create(new Vector3f(1, 0, 0), RED));

        vertices.add(Vertex.POS3F_COL4F.create(new Vector3f(0, 0, 0), GREEN));
        vertices.add(Vertex.POS3F_COL4F.create(new Vector3f(0, 1, 0), GREEN));

        vertices.add(Vertex.POS3F_COL4F.create(new Vector3f(0, 0, 0), BLUE));
        vertices.add(Vertex.POS3F_COL4F.create(new Vector3f(0, 0, 1), BLUE));

        Model3d model3d = new Model3d();
        model3d.createVertexBuffer(device, commands, graphicsQueue, vertices, Vertex.POS3F_COL4F);
//        models.add(model3d);
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

//        var globalUBO = UBO.DEBUG_LINE.create(frameInfo.camera.getProjectionMatrix(), new Matrix4f().translate(0, 0, -2));
        var globalUBO = UBO.DEBUG_LINE.create(frameInfo.camera.getProjectionMatrix(), frameInfo.camera.getViewMatrix());

        flightFrame.uboBuffer.writeToBuffer(UBO.DEBUG_LINE::memcpy, globalUBO);
        flightFrame.uboBuffer.flush();

        pipeline.bind(frameInfo.commandBuffer);

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer,
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline.pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            null);

        for (Model3d model3d : models)
        {
            if (model3d.vertexBuffer == null)
                continue;

            model3d.bind(frameInfo.commandBuffer);
            model3d.draw(frameInfo.commandBuffer);
        }

        for (Model3d frameModel : frameModels)
        {
            frameModel.bind(frameInfo.commandBuffer);
            frameModel.draw(frameInfo.commandBuffer);
            frameModel.destroy();
        }
        frameModels.clear();
    }

    @Override
    public void cleanup()
    {
        for (Model3d model : models)
        {
            if (model.vertexBuffer == null)
                continue;

            model.destroy();
        }
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
