package steve6472.flare.registry;

import steve6472.core.registry.RegistryCore;
import steve6472.core.registry.RegistryGroup;
import steve6472.flare.FlareConstants;

/**
 * Created by steve6472
 * Date: 7/3/2026
 * Project: Flare <br>
 *
 */
public class FlareRegistryGroups
{
    public static final RegistryGroup VULKAN_RESOURCE = RegistryCore.createGroup(FlareConstants.key("vulkan_resource"), true);

    public static void bootstrap() {}
}
