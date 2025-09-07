package steve6472.flare.assets.model.blockbench.anim;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.log.Log;
import steve6472.core.util.Preconditions;
import steve6472.flare.Commands;
import steve6472.flare.assets.model.VkModel;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.SkinData;
import steve6472.flare.assets.model.blockbench.anim.ik.Ik;
import steve6472.flare.assets.model.blockbench.outliner.OutlinerElement;
import steve6472.flare.assets.model.blockbench.outliner.OutlinerUUID;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Vertex;

import java.util.*;
import java.util.logging.Logger;

import static steve6472.flare.render.debug.DebugRender.*;

/**
 * Created by steve6472
 * Date: 9/10/2024
 * Project: Flare <br>
 */
public class AnimationController
{
    private static final Logger LOGGER = Log.getLogger(AnimationController.class);

    private final SkinData masterSkinData;
    private final double animationLength;
    private final LoadedModel model;
    public AnimationTimer timer;
    public SkinData skinData;
    public Timeline timeline;

    /*
     * Debug
     */

    private Commands commands;
    private VkQueue graphicsQueue;
    private VkDevice device;
    private List<Struct> vertices;
    public VkModel debugModel;
    private List<Vector3f> pointsToRender = new ArrayList<>();
    private Ik ik;
    // Null Object UUID

    public AnimationController(Animation animation, SkinData skinData, LoadedModel model)
    {
        this.animationLength = animation.length();
        this.masterSkinData = skinData.copy();
        this.timer = new AnimationTimer();
        this.timeline = new Timeline(animation);
        this.model = model;
        ik = new Ik(model, this);
    }

    public void debugModel(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        this.commands = commands;
        this.graphicsQueue = graphicsQueue;
        this.device = device;
        this.vertices = new ArrayList<>();
        debugModel = new VkModel();
    }

    private void updateModel()
    {
        Preconditions.checkNotNull(debugModel, "Debug not enabled");
        debugModel.createVertexBuffer(device, commands, graphicsQueue, vertices, Vertex.POS3F_COL4F);
    }

    public void tick(Matrix4f modelTransform)
    {

        double currentAnimationTime = timer.calculateTime(System.currentTimeMillis());

        // TODO: this should be all in timer
        if (currentAnimationTime >= animationLength) timer.setRunning(false);
        if (timer.hasEnded() && timer.isLooping()) timer.start();
        if (timer.hasEnded() && timer.isStayAtLastFrame()) currentAnimationTime = animationLength;

        skinData = masterSkinData.copy();

//        if (true)
//            return;

//        if (System.currentTimeMillis() % 200 != 0)
//            return;
//        System.out.println("--TICK--");

//        LOGGER.finest("last: %.4f edit: %.4f stopped: %s".formatted(lastCurAdwdawd, currentAnimationTime, stopped));

        for (OutlinerUUID outlinerUUID : model.outliner())
        {
            Matrix4f transform = new Matrix4f();
            recursiveAnimation(skinData, outlinerUUID, transform, modelTransform, currentAnimationTime);
        }

        for (Vector3f vector3f : pointsToRender)
        {
            addDebugObjectForFrame(lineCube(vector3f, 0.05f, YELLOW));
        }

        if (vertices != null && !vertices.isEmpty())
        {
            updateModel();
            vertices.clear();
        }
    }

    private void recursiveAnimation(SkinData skinData, OutlinerUUID parent, Matrix4f transform, Matrix4f modelTransform, double animTime)
    {
        if (parent instanceof OutlinerElement outEl)
        {
            String boneName = parent.uuid().toString();
            Matrix4f newTransform = new Matrix4f(transform);

            newTransform.translate(outEl.origin());
            animateBone(boneName, KeyframeType.POSITION, animTime, newTransform, false);
            animateBone(boneName, KeyframeType.ROTATION, animTime, newTransform, false);
            animateBone(boneName, KeyframeType.SCALE, animTime, newTransform, false);
            newTransform.translate(-outEl.origin().x, -outEl.origin().y, -outEl.origin().z);

            Vector3f translation = new Vector3f(outEl.origin());
            newTransform.transformPosition(translation);

            for (OutlinerUUID child : outEl.children())
            {
                recursiveAnimation(skinData, child, newTransform, modelTransform, animTime);

                if (child instanceof OutlinerElement outElChild)
                {
                    Vector3f translation_ = new Vector3f(outElChild.origin());
                    transform.transformPosition(translation_);
                    addDebugObjectForFrame(line(translation_, translation, RED));
                } else
                {
                    /*
                     * Debug rendering
                     */

//                    model.getElementByUUIDWithType(LocatorElement.class, child.uuid()).ifPresent(element ->
//                    {
//                        Vector3f translation_ = new Vector3f(element.position());
//                        transform.transformPosition(translation_);
//                        line(ORANGE, translation_, translation);
//                    });
//
//                    model.getElementByUUIDWithType(LocatorElement.class, child.uuid()).ifPresent(element -> {
//                        Matrix4f newerTransform = new Matrix4f(transform);
//                        animateBone(element.uuid().toString(), KeyframeType.POSITION, animTime, newerTransform, true);
//                        Vector3f translation_ = new Vector3f(element.position());
//                        newerTransform.transformPosition(translation_);
//                        addCube(GREEN, translation_, 0.08f);
//                    });
                }

                if (child instanceof OutlinerUUID outlinerUUID)
                {
                    ik.tick(outlinerUUID, transform, animTime, skinData);
                }
            }
            addDebugObjectForFrame(lineCube(translation, 0.1f, PURPLE));

            skinData.transformations.get(parent.uuid()).getSecond().mul(newTransform).mulLocal(modelTransform);
        }
    }

    // TODO: some way to programmatically set the end effector for specific IK

    public void animateBone(String boneName, KeyframeType<?> type, double currentAnimationTime, Matrix4f transform, boolean invert)
    {
        KeyframeChannel<?> lastKeyframe = timeline.getLastKeyframe(type, currentAnimationTime, boneName);
        KeyframeChannel<?> nextKeyframe = timeline.getNextKeyframe(type, currentAnimationTime, boneName);

        if (lastKeyframe == null || nextKeyframe == null)
            return;

//        System.out.println("keyframes for bone " + boneName + " type: " + type.key().id() + " at " + "%.4f ".formatted(currentAnimationTime) + lastKeyframe + " -> " + nextKeyframe);

        if (!(lastKeyframe instanceof KeyframeChannel.AnimationKeyframeChannel lastAnimChannel))
            throw new RuntimeException("Unknown keyframe type");

        double ticks = timeline.calculateTicks(currentAnimationTime, lastKeyframe.time(), nextKeyframe.time());
        lastAnimChannel.processKeyframe(lastKeyframe.dataPoints().getFirst(), nextKeyframe.dataPoints().getFirst(), ticks, transform, invert);
    }

    private void processEffect(KeyframeChannel<?> channel)
    {
        // Remember that they have to activate only once per their frame
    }

    /*
     * Get helpers
     */
}
