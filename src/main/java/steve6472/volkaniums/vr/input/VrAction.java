package steve6472.volkaniums.vr.input;

import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRInput;
import org.lwjgl.system.MemoryStack;
import steve6472.core.log.Log;
import steve6472.volkaniums.vr.VrErrorNames;

import java.nio.LongBuffer;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 10/16/2024
 * Project: Volkaniums <br>
 */
public class VrAction<T>
{
    private static final Logger LOGGER = Log.getLogger(VrAction.class);

    private final long handle;
    private final String actionName;
    private final InputType<T> inputType;

    final T actionData;

    VrAction(String actionName, InputType<T> inputType)
    {
        this.actionName = actionName;
        this.handle = getHandle(actionName);
        this.inputType = inputType;
        this.actionData = inputType.construct();
    }

    private static long getHandle(String actionName)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer pHandle = stack.mallocLong(1);

            int err = VRInput.VRInput_GetActionHandle(actionName, pHandle);
            if (err != VR.EVRInputError_VRInputError_None)
            {
                LOGGER.severe("Error while getting action handle for " + actionName + " error: " + VrErrorNames.EVRInputError(err));
                return 0;
            }

            return pHandle.get(0);
        }
    }

    public long handle()
    {
        return handle;
    }

    public String name()
    {
        return actionName;
    }

    public T get()
    {
        return actionData;
    }

    public void update(float prediction)
    {
        inputType.update(this, prediction);
    }

    public void update()
    {
        update(0);
    }

    void cleanup()
    {
        inputType.cleanup(this);
    }
}
