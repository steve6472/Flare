package steve6472.flare.assets.model.blockbench.animation.ik;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.SkinData;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationTicker;
import steve6472.flare.assets.model.blockbench.element.LocatorElement;
import steve6472.flare.assets.model.blockbench.element.NullObjectElement;
import steve6472.flare.assets.model.blockbench.outliner.OutlinerElement;
import steve6472.flare.assets.model.blockbench.outliner.OutlinerUUID;

import java.util.*;

import static steve6472.flare.render.debug.DebugRender.*;

/**
 * Created by steve6472
 * Date: 9/19/2024
 * Project: Flare <br>
 */
public class IkThing
{
    AnimationTicker controller;
    UUID effectorUUID;
    UUID targetUUID;
    FabrikChain chain;
    List<OutlinerElement> elements;
    private final LoadedModel model;

    public IkThing(LoadedModel model, OutlinerUUID outliner, NullObjectElement nullObjectElement, LocatorElement ikTarget, AnimationTicker controller)
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

            for (int i = 0; i < elements.size(); i++)
            {
                FabrikSegment segment = copy.segments()[i];

                if (Ik.DEBUG_RENDER)
                {
                    addDebugObjectForFrame(lineCube(segment.pos(), 0.02f, CYAN));
                    addDebugObjectForFrame(lineCube(segment.other(), 0.04f, DARK_CYAN));
                }

                OutlinerElement el = elements.get(i);
                Matrix4f transformMatrix = new Matrix4f(parentTransform);
                transformMatrix.translate(el.origin());
//                transformMatrix.rotateZ(el.rotation().z);
//                transformMatrix.rotateY(el.rotation().y);
//                transformMatrix.rotateX(el.rotation().x);

                Vector3f dir = new Vector3f(segment.other()).sub(segment.pos());
                if (dir.lengthSquared() > 1e-8f) dir.normalize();

                Matrix4f invParent = new Matrix4f(parentTransform).invert();
                invParent.transformDirection(dir).normalize();

                // TODO: calculate this from rest position or something
                Vector3f restDir = new Vector3f(0, -1, 0);

                Quaternionf rot = new Quaternionf().rotationTo(restDir, dir);
                transformMatrix.rotate(rot);
                transformMatrix.translate(-el.origin().x, -el.origin().y, -el.origin().z);

                parentTransform = transformMatrix;

                skinData.transformations.get(el.uuid()).getSecond().mul(transformMatrix);
            }
        });
    }
}
