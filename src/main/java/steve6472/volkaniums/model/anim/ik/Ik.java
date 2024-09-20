package steve6472.volkaniums.model.anim.ik;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.model.LoadedModel;
import steve6472.volkaniums.model.SkinData;
import steve6472.volkaniums.model.anim.AnimationController;
import steve6472.volkaniums.model.anim.KeyframeType;
import steve6472.volkaniums.model.element.LocatorElement;
import steve6472.volkaniums.model.element.NullObjectElement;
import steve6472.volkaniums.model.outliner.OutlinerElement;
import steve6472.volkaniums.model.outliner.OutlinerUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static steve6472.volkaniums.model.anim.AnimationController.BLUE;

/**
 * Created by steve6472
 * Date: 9/19/2024
 * Project: Volkaniums <br>
 */
public class Ik
{
    AnimationController controller;
    private Map<UUID, IkThing> map = new HashMap<>();
    private LoadedModel model;

    public Ik(LoadedModel model, AnimationController controller)
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

    public void tick(OutlinerUUID child, Matrix4f transform, double animTime, SkinData skinData)
    {
        model.getElementByUUIDWithType(NullObjectElement.class, child.uuid()).ifPresent(element -> {
            Matrix4f newerTransform = new Matrix4f(transform);
            controller.animateBone(element.uuid().toString(), KeyframeType.POSITION, animTime, newerTransform, true);
            Vector3f endEffector = new Vector3f(element.position());
            newerTransform.transformPosition(endEffector);
            controller.addCube(BLUE, endEffector, 0.04f);

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
