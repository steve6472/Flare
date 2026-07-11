package steve6472.test;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.log.Log;
import steve6472.core.registry.Holder;
import steve6472.core.registry.Key;
import steve6472.flare.*;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationController;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationQuery;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.registry.BuiltInFlareRegistries;
import steve6472.flare.render.Reloadable;
import steve6472.flare.render.common.CommonBuilder;
import steve6472.flare.render.common.CommonRenderSystem;
import steve6472.flare.render.common.FlightFrame;
import steve6472.flare.render.debug.DebugRender;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Push;
import steve6472.flare.struct.def.SBO;
import steve6472.orlang.*;

import java.util.List;
import java.util.logging.Logger;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public class SkinRenderSystem extends CommonRenderSystem implements Reloadable
{
    private static final Logger LOGGER = Log.getLogger(SkinRenderSystem.class);
    Holder<Model> model3d;

    AnimationController animationController;
    OrlangEnvironment environment;

//    static final Key MODEL_KEY = Key.withNamespace("test", "blockbench/animated/debug_model_rotations");
    static final Key MODEL_KEY = Key.withNamespace("test", "blockbench/animated/long_chain");
    static final AST.Node.Identifier FLAG_ID = new AST.Node.Identifier(VarContext.VARIABLE, "flag");
    public boolean flag = false;

    public SkinRenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
    {
        super(masterRenderer, pipeline, CommonBuilder.create()
            .entrySBO(SBO.BONES.sizeof(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_SHADER_STAGE_VERTEX_BIT)
            .entryImage(BuiltInFlareRegistries.ATLAS.get(FlareConstants.ATLAS_BLOCKBENCH).orElseThrow().value().getSampler()));

        LoadedModel loadedModel = BuiltInFlareRegistries.ANIMATED_LOADED_MODEL.get(MODEL_KEY).orElseThrow().value();
        environment = new OrlangEnvironment();
        environment.queryFunctionSet = new AnimQuery();

        animationController = BuiltInFlareRegistries.ANIMATION_CONTROLLER.get(Key.withNamespace("test", "long_chain")).orElseThrow().value().createForModel(loadedModel);
        environment.setValue(FLAG_ID, OrlangValue.bool(false));

        model3d = BuiltInFlareRegistries.ANIMATED_MODEL.get(MODEL_KEY).orElseThrow();
    }

    @Override
    protected void render(FlightFrame flightFrame, FrameInfo frameInfo, MemoryStack stack)
    {
//        System.out.println("---------");
        if (TestKeybinds.F.isActive())
        {
            flag = !flag;
            environment.setValue(FLAG_ID, OrlangValue.bool(flag));
//            animationController.controllers().get("button_1").forceTransition("pressed");
        }
//        if (TestKeybinds.G.isActive())
//        {
//            animationController.controllers().get("button_2").forceTransition("pressed");
//        }

        // Update

        Matrix4f modelTransform = new Matrix4f();
        modelTransform.translate(0, 0, 0);
        animationController.tick(modelTransform, environment);

        DebugRender.addDebugObjectForFrame(DebugRender.lineSphere(0.05f, 8, DebugRender.BEIGE), new Matrix4f().translate(animationController.getLocator("locator").position()));
        Matrix4f[] array = animationController.getTransformations();
        var sbo = SBO.BONES.create((Object) array);

        flightFrame.getBuffer(0).writeToBuffer(SBO.BONES::memcpy, List.of(sbo), array.length * 64L, 0);
        flightFrame.getBuffer(0).flush(array.length * 64L, 0);

        Struct struct = Push.SKIN.create(array.length, 0);
        Push.SKIN.push(struct, frameInfo.commandBuffer(), pipeline().pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0);

        model3d.value().bind(frameInfo.commandBuffer());
        model3d.value().draw(frameInfo.commandBuffer());
    }

    @Override
    protected void updateData(FlightFrame flightFrame, FrameInfo frameInfo)
    {
    }

    private static final class AnimQuery extends QueryFunctionSet implements AnimationQuery
    {
        private double animTime;
        private boolean anyAnimFinished, allAnimsFinished;

        public AnimQuery()
        {
            functions.put("anim_time", OrlangValue.func(() -> animTime));
            functions.put("any_animation_finished", OrlangValue.func(() -> anyAnimFinished));
            functions.put("all_animations_finished", OrlangValue.func(() -> allAnimsFinished));
        }

        @Override
        public void setAnimTime(double animTime)
        {
            this.animTime = animTime;
        }

        @Override
        public void setAnyAnimationFinished(boolean flag)
        {
            anyAnimFinished = flag;
        }

        @Override
        public void setAllAnimationsFinished(boolean flag)
        {
            allAnimsFinished = flag;
        }
    }
}
