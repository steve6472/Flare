package steve6472.volkaniums.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkQueue;
import steve6472.volkaniums.*;
import steve6472.volkaniums.assets.Texture;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.assets.model.VkModel;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.assets.model.blockbench.LoadedModel;
import steve6472.volkaniums.assets.model.primitive.PrimitiveSkinModel;
import steve6472.volkaniums.assets.model.blockbench.anim.AnimationController;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Push;
import steve6472.volkaniums.struct.def.SBO;
import steve6472.volkaniums.struct.def.UBO;
import steve6472.volkaniums.struct.def.Vertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.volkaniums.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public class SkinRenderSystem extends RenderSystem
{
    VkModel model3d;

    private DescriptorPool globalPool;
    private DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    Texture texture;
    TextureSampler sampler;
    PrimitiveSkinModel primitiveSkinModel;
    AnimationController animationController;

    public SkinRenderSystem(MasterRenderer masterRenderer, Pipeline pipeline)
    {
        super(masterRenderer, pipeline);

        createModel(masterRenderer.getCommands(), masterRenderer.getGraphicsQueue());

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, MAX_FRAMES_IN_FLIGHT)
            .build();

        texture = new Texture();
//        texture.createTextureImage(device, "resources\\white_shaded.png", masterRenderer.getCommands().commandPool, masterRenderer.getGraphicsQueue());
//        texture.createTextureImage(device, "resources\\loony.png", masterRenderer.getCommands().commandPool, masterRenderer.getGraphicsQueue());
//        sampler = new TextureSampler(texture, device);

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            FlightFrame frame = new FlightFrame();
            frames.add(frame);

            VkBuffer global = new VkBuffer(
                device,
                UBO.GLOBAL_UBO_TEST.sizeof(),
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            global.map();
            frame.uboBuffer = global;

            VkBuffer sbo = new VkBuffer(
                device,
                SBO.BONES.sizeof(),
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
                    .writeBuffer(1, frame.sboBuffer, stack)
                    .writeImage(2, sampler, stack)
                    .build();
            }
        }
    }

    private void createModel(Commands commands, VkQueue graphicsQueue)
    {
//        final String PATH = "resources\\robot_arm.bbmodel";
//        final String PATH = "resources\\small_chain.bbmodel";
        final String PATH = "resources\\model.bbmodel";
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

        model3d = new VkModel();
        LoadedModel loadedModel = decode.getOrThrow().getFirst();
        primitiveSkinModel = loadedModel.toPrimitiveSkinModel();
        model3d.createVertexBuffer(device, commands, graphicsQueue, primitiveSkinModel);

//        animationController = new AnimationController(loadedModel.getAnimationByName("looping_chain"), primitiveSkinModel.skinData, loadedModel);
//        animationController = new AnimationController(loadedModel.getAnimationByName("grab_loop"), primitiveSkinModel.skinData, loadedModel);
//        animationController = new AnimationController(loadedModel.getAnimationByName("flip"), primitiveSkinModel.skinData, loadedModel);
//        animationController = new AnimationController(loadedModel.getAnimationByName("straight"), primitiveSkinModel.skinData, loadedModel);
        animationController = new AnimationController(loadedModel.getAnimationByName("idle"), primitiveSkinModel.skinData, loadedModel);
        animationController.timer.setLoop(true);
        animationController.timer.start();
        animationController.debugModel(device, commands, graphicsQueue);
//        if (animationController.debugModel != null)
//            getMasterRenderer().debugLines().models.add(animationController.debugModel);

        animationController.timer.setSpeed(0.1);
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

        var globalUBO = UBO.GLOBAL_UBO_TEST.create(frameInfo.camera.getProjectionMatrix(), frameInfo.camera.getViewMatrix());

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_UBO_TEST::memcpy, globalUBO);
        flightFrame.uboBuffer.flush();

        animationController.tick();

        var sbo = SBO.BONES.create((Object) animationController.skinData.toArray());

        flightFrame.sboBuffer.writeToBuffer(SBO.BONES::memcpy, sbo);
        flightFrame.sboBuffer.flush();

        pipeline.bind(frameInfo.commandBuffer);

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer,
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline.pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            null);

        Struct struct = Push.SKIN.create(primitiveSkinModel.skinData.transformations.size());
        Push.SKIN.push(struct, frameInfo.commandBuffer, pipeline.pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0);

        model3d.bind(frameInfo.commandBuffer);
        model3d.draw(frameInfo.commandBuffer);
    }

    @Override
    public void cleanup()
    {
        model3d.destroy();
        globalSetLayout.cleanup();
        globalPool.cleanup();
        texture.cleanup(device);
        sampler.cleanup(device);

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
