package steve6472.flare.vr.input;

import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRActiveActionSet;
import org.lwjgl.openvr.VRInput;
import org.lwjgl.system.MemoryStack;
import steve6472.core.log.Log;
import steve6472.flare.vr.VrErrorNames;

import java.nio.LongBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 10/16/2024
 * Project: Flare <br>
 */
public class VrActionSet
{
    private static final Logger LOGGER = Log.getLogger(VrActionSet.class);

    private final Set<VrAction<?>> actions = new HashSet<>();
    private final long handle;
    private final String actionSetName;

    public VrActionSet(String actionSetName)
    {
        this.actionSetName = actionSetName;
        this.handle = getHandle(actionSetName);
    }

    private static long getHandle(String actionSetName)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer pHandle = stack.mallocLong(1);

            int err = VRInput.VRInput_GetActionSetHandle(actionSetName, pHandle);
            if (err != VR.EVRInputError_VRInputError_None)
            {
                LOGGER.severe("Error while getting action set handle for " + actionSetName + " error: " + VrErrorNames.EVRInputError(err));
                return 0;
            }

            return pHandle.get(0);
        }
    }

    public String name()
    {
        return actionSetName;
    }

    public <T> VrAction<T> addAction(String actionName, InputType<T> inputType)
    {
        VrAction<T> action = new VrAction<>(actionName, inputType);
        actions.add(action);
        return action;
    }

    public void updateAll(float prediction)
    {
        if (!updateActionState())
            return;

        actions.forEach(action -> action.update(prediction));
    }

    public void updateAll()
    {
        updateAll(0);
    }

    /// @return false on error
    private boolean updateActionState()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VRActiveActionSet.Buffer calloc = VRActiveActionSet.calloc(1, stack);
            VRActiveActionSet vrActiveActionSet = calloc.get(0);
            vrActiveActionSet.ulActionSet(handle);
            int err;
            if ((err = VRInput.VRInput_UpdateActionState(calloc, calloc.sizeof())) != VR.EVRInputError_VRInputError_None)
            {
                LOGGER.severe("Error while updating action state for " + name() + " error: " + VrErrorNames.EVRInputError(err));
                return false;
            }
        }

        return true;
    }

    public void cleanup()
    {
        actions.forEach(VrAction::cleanup);
    }
}
