package steve6472.flare.render;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.flare.*;
import steve6472.flare.assets.model.VkModel;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationController;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.render.common.CommonBuilder;
import steve6472.flare.render.common.CommonRenderSystem;
import steve6472.flare.render.common.FlightFrame;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Push;
import steve6472.flare.struct.def.SBO;
import steve6472.orlang.AST;
import steve6472.orlang.OrlangValue;
import steve6472.orlang.VarContext;
import steve6472.test.TestKeybinds;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public class SkinRenderSystem extends CommonRenderSystem
{
    private static final Logger LOGGER = Log.getLogger(SkinRenderSystem.class);
    VkModel model3d;

    AnimationController animationController;

//    static final Key MODEL_KEY = Key.withNamespace("test", "blockbench/animated/debug_model_rotations");
    static final Key MODEL_KEY = Key.withNamespace("test", "blockbench/animated/snail");
    static final AST.Node.Identifier FLAG_ID = new AST.Node.Identifier(VarContext.VARIABLE, "flag");
    public boolean flag = false;

    public SkinRenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
    {
        super(masterRenderer, pipeline, CommonBuilder.create()
            .entrySBO(SBO.BONES.sizeof(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_SHADER_STAGE_VERTEX_BIT)
            .entryImage(FlareRegistries.ATLAS.get(FlareConstants.ATLAS_BLOCKBENCH).getSampler()));

        LoadedModel loadedModel = FlareRegistries.ANIMATED_LOADED_MODEL.get(MODEL_KEY);

        animationController = FlareRegistries.ANIMATION_CONTROLLER.get(Key.withNamespace("test", "snail")).createForModel(loadedModel);
        animationController.environment().setValue(new AST.Node.Identifier(VarContext.VARIABLE, "flag"), OrlangValue.bool(false));

        model3d = FlareRegistries.ANIMATED_MODEL.get(MODEL_KEY);
    }

    @Override
    protected void render(FlightFrame flightFrame, FrameInfo frameInfo, MemoryStack stack)
    {
        if (TestKeybinds.G.isActive())
        {
            flag = !flag;
            animationController.environment().setValue(FLAG_ID, OrlangValue.bool(flag));
        }

        // Update

        Matrix4f modelTransform = new Matrix4f();
        modelTransform.translate(0, 0, 0);
        animationController.tick(modelTransform);
        Matrix4f[] array = animationController.getTransformations();
        var sbo = SBO.BONES.create((Object) array);

        flightFrame.getBuffer(0).writeToBuffer(SBO.BONES::memcpy, List.of(sbo), array.length * 64L, 0);
        flightFrame.getBuffer(0).flush(array.length * 64L, 0);

        Struct struct = Push.SKIN.create(array.length);
        Push.SKIN.push(struct, frameInfo.commandBuffer(), pipeline().pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0);

        model3d.bind(frameInfo.commandBuffer());
        model3d.draw(frameInfo.commandBuffer());
    }

    @Override
    protected void updateData(steve6472.flare.render.common.FlightFrame flightFrame, FrameInfo frameInfo)
    {
    }
}
