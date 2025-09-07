package steve6472.flare.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.registry.Key;
import steve6472.flare.*;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.model.VkModel;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.descriptors.DescriptorPool;
import steve6472.flare.descriptors.DescriptorSetLayout;
import steve6472.flare.descriptors.DescriptorWriter;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.primitive.PrimitiveSkinModel;
import steve6472.flare.assets.model.blockbench.anim.AnimationController;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Push;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.struct.def.UBO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.flare.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public class SkinRenderSystem extends RenderSystem
{
    VkModel model3d;

    private DescriptorPool globalPool;
    private DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    PrimitiveSkinModel primitiveSkinModel;
    AnimationController animationController;

    static final Key MODEL_KEY = Key.withNamespace("test", "blockbench/animated/debug_model_rotations");

    public SkinRenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
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
            .addPoolSize(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, MAX_FRAMES_IN_FLIGHT)
            .build();

//        texture.createTextureImage(device, "resources\\white_shaded.png", masterRenderer.getCommands().commandPool, masterRenderer.getGraphicsQueue());
//        texture.createTextureImage(device, "resources\\loony.png", masterRenderer.getCommands().commandPool, masterRenderer.getGraphicsQueue());
//        sampler = new TextureSampler(texture, device);
        model3d = FlareRegistries.ANIMATED_MODEL.get(MODEL_KEY);

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
                    .writeBuffer(0, stack, frame.uboBuffer)
                    .writeBuffer(1, stack, frame.sboBuffer)
                    .writeImage(2, stack, FlareRegistries.ATLAS.get(FlareConstants.ATLAS_BLOCKBENCH).getSampler())
                    .build();
            }
        }
    }

    private void createModel(Commands commands, VkQueue graphicsQueue)
    {
        LoadedModel loadedModel = FlareRegistries.ANIMATED_LOADED_MODEL.get(MODEL_KEY);
        primitiveSkinModel = loadedModel.toPrimitiveSkinModel();

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

//        animationController.timer.setSpeed(0.1);
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
        // Update

        var globalUBO = UBO.GLOBAL_UBO_TEST.create(frameInfo.camera().getProjectionMatrix(), frameInfo.camera().getViewMatrix());

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_UBO_TEST::memcpy, globalUBO);
        flightFrame.uboBuffer.flush();

        Matrix4f modelTransform = new Matrix4f();
        modelTransform.rotateY((float) Math.toRadians(12.5d));
        animationController.tick(modelTransform);

        Matrix4f[] array = animationController.skinData.toArray();
        var sbo = SBO.BONES.create((Object) array);

        flightFrame.sboBuffer.writeToBuffer(SBO.BONES::memcpy, List.of(sbo), array.length * 64L, 0);
        flightFrame.sboBuffer.flush(array.length * 64L, 0);

        pipeline().bind(frameInfo.commandBuffer());

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer(),
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline().pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            null);

        Struct struct = Push.SKIN.create(primitiveSkinModel.skinData.transformations.size());
        Push.SKIN.push(struct, frameInfo.commandBuffer(), pipeline().pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0);

        model3d.bind(frameInfo.commandBuffer());
        model3d.draw(frameInfo.commandBuffer());
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
