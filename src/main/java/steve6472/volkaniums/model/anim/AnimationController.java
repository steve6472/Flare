package steve6472.volkaniums.model.anim;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.volkaniums.Commands;
import steve6472.volkaniums.Model3d;
import steve6472.volkaniums.model.*;
import steve6472.volkaniums.model.anim.ik.Ik;
import steve6472.volkaniums.model.element.LocatorElement;
import steve6472.volkaniums.model.outliner.OutlinerElement;
import steve6472.volkaniums.model.outliner.OutlinerUUID;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Vertex;
import steve6472.volkaniums.util.Log;
import steve6472.volkaniums.util.Preconditions;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/10/2024
 * Project: Volkaniums <br>
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

    public static final Vector4f RED = new Vector4f(1, 0, 0, 1);
    public static final Vector4f ORANGE = new Vector4f(1, 0.6f, 0, 1);
    public static final Vector4f GREEN = new Vector4f(0, 1, 0, 1);
    public static final Vector4f BLUE = new Vector4f(0, 0, 1, 1);
    public static final Vector4f PURPLE = new Vector4f(1, 0, 1, 1);
    public static final Vector4f YELLOW = new Vector4f(1, 1, 0, 1);
    public static final Vector4f CYAN = new Vector4f(0, 1, 1, 1);
    private Commands commands;
    private VkQueue graphicsQueue;
    private VkDevice device;
    private List<Struct> vertices;
    public Model3d debugModel;
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
        debugModel = new Model3d();
    }

    private void updateModel()
    {
        Preconditions.checkNotNull(debugModel, "Debug not enabled");
        debugModel.createVertexBuffer(device, commands, graphicsQueue, vertices, Vertex.POS3F_COL4F);
    }

    public void tick()
    {

        double currentAnimationTime = timer.calculateTime(System.currentTimeMillis());

        // TODO: this should be all in timer
        if (currentAnimationTime >= animationLength) timer.setRunning(false);
        if (timer.hasEnded() && timer.isLooping()) timer.start();
        if (timer.hasEnded() && timer.isStayAtLastFrame()) currentAnimationTime = animationLength;

        skinData = masterSkinData.copy();

//        if (System.currentTimeMillis() % 200 != 0)
//            return;
//        System.out.println("--TICK--");

//        LOGGER.finest("last: %.4f edit: %.4f stopped: %s".formatted(lastCurAdwdawd, currentAnimationTime, stopped));

        for (OutlinerUUID outlinerUUID : model.outliner())
        {
            Matrix4f transform = new Matrix4f();
            recursiveAnimation(skinData, outlinerUUID, transform, currentAnimationTime);
        }

        for (Vector3f vector3f : pointsToRender)
        {
            addCube(YELLOW, vector3f, 0.05f);
        }

        if (vertices != null && !vertices.isEmpty())
        {
            updateModel();
            vertices.clear();
        }
    }

    private void recursiveAnimation(SkinData skinData, OutlinerUUID parent, Matrix4f transform, double animTime)
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
                recursiveAnimation(skinData, child, newTransform, animTime);

                if (child instanceof OutlinerElement outElChild)
                {
                    Vector3f translation_ = new Vector3f(outElChild.origin());
                    transform.transformPosition(translation_);
                    line(RED, translation_, translation);
                } else
                {
                    model.getElementByUUIDWithType(LocatorElement.class, child.uuid()).ifPresent(element ->
                    {
                        Vector3f translation_ = new Vector3f(element.position());
                        transform.transformPosition(translation_);
                        line(ORANGE, translation_, translation);
                    });

                    /*
                     * Debug rendering
                     */
                    ik.tick(child, transform, animTime, skinData);

                    model.getElementByUUIDWithType(LocatorElement.class, child.uuid()).ifPresent(element -> {
                        Matrix4f newerTransform = new Matrix4f(transform);
                        animateBone(element.uuid().toString(), KeyframeType.POSITION, animTime, newerTransform, true);
                        Vector3f translation_ = new Vector3f(element.position());
                        newerTransform.transformPosition(translation_);
                        addCube(GREEN, translation_, 0.08f);
                    });
                }
            }
            addCube(PURPLE, translation, 0.1f);

            skinData.transformations.get(parent.uuid()).getSecond().mul(newTransform);
        }
    }

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

    private void line(Vector4f color, Vector3f from, Vector3f to)
    {
        if (debugModel == null) return;
        vertices.add(Vertex.POS3F_COL4F.create(from, color));
        vertices.add(Vertex.POS3F_COL4F.create(to, color));
    }

    public void addCube(Vector4f color, Vector3f pos, float size)
    {
        if (debugModel == null) return;
        // Half size for centering the cube around pos
        float halfSize = size / 2.0f;

        // Calculate the 8 corner points of the cube
        Vector3f p0 = new Vector3f(pos.x - halfSize, pos.y - halfSize, pos.z - halfSize);
        Vector3f p1 = new Vector3f(pos.x + halfSize, pos.y - halfSize, pos.z - halfSize);
        Vector3f p2 = new Vector3f(pos.x + halfSize, pos.y + halfSize, pos.z - halfSize);
        Vector3f p3 = new Vector3f(pos.x - halfSize, pos.y + halfSize, pos.z - halfSize);

        Vector3f p4 = new Vector3f(pos.x - halfSize, pos.y - halfSize, pos.z + halfSize);
        Vector3f p5 = new Vector3f(pos.x + halfSize, pos.y - halfSize, pos.z + halfSize);
        Vector3f p6 = new Vector3f(pos.x + halfSize, pos.y + halfSize, pos.z + halfSize);
        Vector3f p7 = new Vector3f(pos.x - halfSize, pos.y + halfSize, pos.z + halfSize);

        // Front face (p0, p1, p2, p3)
        vertices.add(Vertex.POS3F_COL4F.create(p0, color));
        vertices.add(Vertex.POS3F_COL4F.create(p1, color));

        vertices.add(Vertex.POS3F_COL4F.create(p1, color));
        vertices.add(Vertex.POS3F_COL4F.create(p2, color));

        vertices.add(Vertex.POS3F_COL4F.create(p2, color));
        vertices.add(Vertex.POS3F_COL4F.create(p3, color));

        vertices.add(Vertex.POS3F_COL4F.create(p3, color));
        vertices.add(Vertex.POS3F_COL4F.create(p0, color));

        // Back face (p4, p5, p6, p7)
        vertices.add(Vertex.POS3F_COL4F.create(p4, color));
        vertices.add(Vertex.POS3F_COL4F.create(p5, color));

        vertices.add(Vertex.POS3F_COL4F.create(p5, color));
        vertices.add(Vertex.POS3F_COL4F.create(p6, color));

        vertices.add(Vertex.POS3F_COL4F.create(p6, color));
        vertices.add(Vertex.POS3F_COL4F.create(p7, color));

        vertices.add(Vertex.POS3F_COL4F.create(p7, color));
        vertices.add(Vertex.POS3F_COL4F.create(p4, color));

        // Connect front and back faces
        vertices.add(Vertex.POS3F_COL4F.create(p0, color));
        vertices.add(Vertex.POS3F_COL4F.create(p4, color));

        vertices.add(Vertex.POS3F_COL4F.create(p1, color));
        vertices.add(Vertex.POS3F_COL4F.create(p5, color));

        vertices.add(Vertex.POS3F_COL4F.create(p2, color));
        vertices.add(Vertex.POS3F_COL4F.create(p6, color));

        vertices.add(Vertex.POS3F_COL4F.create(p3, color));
        vertices.add(Vertex.POS3F_COL4F.create(p7, color));
    }

    /*
     * Get helpers
     */
}
