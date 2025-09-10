package steve6472.flare.assets.model.blockbench.animation.ik;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.SkinData;
import steve6472.flare.assets.model.blockbench.animation.controller.AnimationTicker;
import steve6472.flare.assets.model.blockbench.animation.keyframe.KeyframeType;
import steve6472.flare.assets.model.blockbench.element.LocatorElement;
import steve6472.flare.assets.model.blockbench.element.NullObjectElement;
import steve6472.flare.assets.model.blockbench.outliner.OutlinerUUID;
import steve6472.orlang.OrlangEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static steve6472.flare.render.debug.DebugRender.*;

/**
 * Created by steve6472
 * Date: 9/19/2024
 * Project: Flare <br>
 */
public class Ik
{
    AnimationTicker controller;
    private Map<UUID, IkThing> map = new HashMap<>();
    private LoadedModel model;

    public Ik(LoadedModel model, AnimationTicker controller)
    {
        this.model = model;
        this.controller = controller;
        create();
    }

    private void create()
    {
        // TODO: Either locator or outliner
        for (NullObjectElement nullObjectElement : model.getElementsWithType(NullObjectElement.class))
        {
            if (nullObjectElement.ikTarget().isBlank()) continue;

            // Get target
            model.getElementByUUID(UUID.fromString(nullObjectElement.ikTarget())).ifPresent(ikTarget -> {
                // Get targets parent outliner element
                model.getOutlinerByChildElementUUID(ikTarget.uuid()).ifPresent(outliner -> {
                    IkThing thing = new IkThing(model, outliner, nullObjectElement, ((LocatorElement) ikTarget), controller);
                    map.put(nullObjectElement.uuid(), thing);
                });
            });
        }
    }

    public void tick(OutlinerUUID child, Matrix4f transform, double animTime, SkinData skinData, OrlangEnvironment env)
    {
//        IkThing ikThing = map.get(child.uuid());
//        if (ikThing != null)
//        {
//            Matrix4f newerTransform = new Matrix4f(transform);
//            controller.animateBone(child.uuid().toString(), KeyframeType.POSITION, animTime, newerTransform, true);
//            Vector3f endEffector = new Vector3f(noe.position());
//            newerTransform.transformPosition(endEffector);
//            controller.addCube(BLUE, endEffector, 0.04f);
//            ikThing.tick(endEffector, skinData);
//        }

        model.getElementByUUIDWithType(NullObjectElement.class, child.uuid()).ifPresent(element -> {
            Matrix4f newerTransform = new Matrix4f(transform);
            controller.animateBone(element.uuid().toString(), KeyframeType.POSITION, animTime, newerTransform, true, env);
            Vector3f endEffector = new Vector3f(element.position());
            newerTransform.transformPosition(endEffector);

            addDebugObjectForFrame(lineCube(endEffector, 0.04f, BLUE));

            if (!element.ikTarget().isBlank())
            {
                IkThing thing = map.get(element.uuid());
                if (thing != null)
                {
                    thing.tick(endEffector, skinData);
                }
            }
        });
    }
}
