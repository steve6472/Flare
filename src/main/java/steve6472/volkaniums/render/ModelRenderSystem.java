package steve6472.volkaniums.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.volkaniums.Commands;
import steve6472.volkaniums.FrameInfo;
import steve6472.volkaniums.Model3d;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.model.LoadedModel;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Push;
import steve6472.volkaniums.struct.def.Vertex;
import steve6472.volkaniums.util.MathUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public class ModelRenderSystem extends RenderSystem
{
    Model3d model3d;

    public ModelRenderSystem(VkDevice device, Pipeline pipeline, Commands commands, VkQueue graphicsQueue)
    {
        super(device, pipeline);

        createModel(commands, graphicsQueue);
    }

    private void createModel(Commands commands, VkQueue graphicsQueue)
    {
        final String PATH = "C:\\Users\\Steve\\Desktop\\model.bbmodel";
        final File file = new File(PATH);

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        JsonElement jsonElement = JsonParser.parseReader(reader);
        DataResult<Pair<LoadedModel, JsonElement>> decode = LoadedModel.CODEC.decode(JsonOps.INSTANCE, jsonElement);

        model3d = new Model3d();
        model3d.createVertexBuffer(device, commands, graphicsQueue, decode.getOrThrow().getFirst().toPrimitiveModel().toVkVertices(), Vertex.POS3F_COL3F_UV);
    }

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        pipeline.bind(frameInfo.commandBuffer);

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer,
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline.pipelineLayout(),
            0,
            stack.longs(frameInfo.globalDescriptorSet),
            null);

        for (int j = 0; j < 4; j++)
        {
            Struct push = Push.PUSH.create(new Matrix4f()
                    .translate(j - 1.5f, 0.75f, 0)
                    .rotateY((float) MathUtil.animateRadians(4d))
                    .rotateZ((float) Math.toRadians(180)) // Todo: flip the view probably ?
                    .scale(0.05f),
                new Vector4f(0.3f, 0.3f, 0.3f, 1.0f),
                j);

            Push.PUSH.push(push, frameInfo.commandBuffer, pipeline.pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 0);

            model3d.bind(frameInfo.commandBuffer);
            model3d.draw(frameInfo.commandBuffer);
        }
    }

    @Override
    public void cleanup()
    {
        model3d.destroy();
    }
}
