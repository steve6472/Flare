package steve6472.volkaniums.assets.model.blockbench.anim.ik;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.assets.model.blockbench.LoadedModel;
import steve6472.volkaniums.assets.model.blockbench.SkinData;
import steve6472.volkaniums.assets.model.blockbench.anim.AnimationController;
import steve6472.volkaniums.assets.model.blockbench.element.LocatorElement;
import steve6472.volkaniums.assets.model.blockbench.element.NullObjectElement;
import steve6472.volkaniums.assets.model.blockbench.outliner.OutlinerElement;
import steve6472.volkaniums.assets.model.blockbench.outliner.OutlinerUUID;

import java.util.*;

import static steve6472.volkaniums.render.debug.DebugRender.*;

/**
 * Created by steve6472
 * Date: 9/19/2024
 * Project: Volkaniums <br>
 */
public class IkThing
{
    AnimationController controller;
    UUID effectorUUID;
    UUID targetUUID;
    FabrikChain chain;
    List<OutlinerElement> elements;
    private final LoadedModel model;

    public IkThing(LoadedModel model, OutlinerUUID outliner, NullObjectElement nullObjectElement, LocatorElement ikTarget, AnimationController controller)
    {
        this.controller = controller;
        this.model = model;
        create(model, outliner, nullObjectElement, ikTarget);
        effectorUUID = nullObjectElement.uuid();
        targetUUID = ikTarget.uuid();
    }

    private void create(LoadedModel model, OutlinerUUID start, NullObjectElement effector, LocatorElement locator)
    {
        Optional<OutlinerUUID> chainStop = model.getOutlinerByChildElementUUID(effector.uuid());

        OutlinerUUID parent = start;
        List<OutlinerElement> outlinerChainToTarget = new ArrayList<>();

        while (parent != null)
        {
            if (!(parent instanceof OutlinerElement outlinerElement))
                throw new RuntimeException("Some error idk");
            if (chainStop.get().equals(parent))
                break;
            outlinerChainToTarget.add(outlinerElement);

            parent = parent.parent();
        }
        Collections.reverse(outlinerChainToTarget);
        elements = outlinerChainToTarget;

        List<Vector3f> points = new ArrayList<>();
        Matrix4f parentTransform = new Matrix4f();

        // Get from skindata ?
        for (OutlinerElement el : outlinerChainToTarget)
        {
            Matrix4f transformMatrix = new Matrix4f(parentTransform);
            transformMatrix.translate(el.origin());
            transformMatrix.rotateZ(el.rotation().z);
            transformMatrix.rotateY(el.rotation().y);
            transformMatrix.rotateX(el.rotation().x);
            transformMatrix.translate(-el.origin().x, -el.origin().y, -el.origin().z);
            parentTransform = transformMatrix;

            Vector3f point = new Vector3f(el.origin());
            transformMatrix.transformPosition(point);
            points.add(point);

        }
        Vector3f point = new Vector3f(locator.position());
        parentTransform.transformPosition(point);
        points.add(point);

        chain = FabrikChain.fromVerticies(points.toArray(Vector3f[]::new));
    }

    public void tick(Vector3f endEffector, SkinData skinData)
    {
        model.getElementByUUIDWithType(LocatorElement.class, targetUUID).ifPresent(locator -> {
            FabrikChain copy = chain.copy();
            copy.solve(endEffector);

            Matrix4f parentTransform = new Matrix4f();
            Vector3f backRot = new Vector3f();

            for (int i = 0; i < elements.size(); i++)
            {
                FabrikSegment segment = copy.segments()[i];

                addDebugObjectForFrame(lineCube(segment.pos(), 0.02f, CYAN));
                addDebugObjectForFrame(lineCube(segment.other(), 0.04f, DARK_CYAN));

                OutlinerElement el = elements.get(i);
                Matrix4f transformMatrix = new Matrix4f(parentTransform);
                transformMatrix.translate(el.origin());
                transformMatrix.rotateZ(el.rotation().z);
                transformMatrix.rotateY(el.rotation().y);
                transformMatrix.rotateX(el.rotation().x);

                Matrix4f rotationMatrix = calculateRotationMatrix(segment.pos(), segment.other());
                Vector3f newRot = new Vector3f();
                rotationMatrix.getEulerAnglesZYX(newRot);
//                newRot.add((float) Math.PI, 0, 0);
                newRot.sub(backRot);
                backRot.add(newRot);
                transformMatrix.rotateZYX(newRot);

                transformMatrix.translate(-el.origin().x, -el.origin().y, -el.origin().z);

                parentTransform = transformMatrix;

                skinData.transformations.get(el.uuid()).getSecond().mul(transformMatrix);
            }
        });
    }

    private static Matrix4f calculateRotationMatrix(Vector3f pointA, Vector3f pointB)
    {
        Vector3f dir = new Vector3f();
        pointA.sub(pointB, dir);
        dir.normalize();
//        Vector3f normalizedA = new Vector3f(0, 1, 0);
        Vector3f normalizedA = Constants.CAMERA_UP;
        Vector3f axis = new Vector3f();
        normalizedA.cross(dir, axis).normalize();
        float angle = (float) Math.acos(normalizedA.dot(dir));

        // Create the rotation matrix using the axis and angle
        Matrix4f rotationMatrix = new Matrix4f().identity();
        rotationMatrix.rotate(angle, axis); // Rotate around the axis by the angle

        return rotationMatrix;
    }
}
