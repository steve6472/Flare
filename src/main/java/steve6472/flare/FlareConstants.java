package steve6472.flare;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VkExtent2D;
import steve6472.core.SteveCore;
import steve6472.core.registry.Key;
import steve6472.flare.pipeline.Pipeline;

import java.io.File;

/**
 * Created by steve6472
 * Date: 8/18/2024
 * Project: Flare <br>
 */
public class FlareConstants
{
    /// Used for code-generated resources
    public static final String NAMESPACE = "flare";

    public static final Vector3f CAMERA_UP = new Vector3f(0, 1, 0);

    /// Global clear color
    public static final Vector4f CLEAR_COLOR = new Vector4f(0.1f, 0.1f, 0.1f, 1.0f);

    /// Null extent used by [Pipeline] to create a dummy Pipeline for the Vertex data
    public static final VkExtent2D NULL_EXTENT = VkExtent2D.malloc().set(1, 1);

    /// Note: This makes models be unable to be saved unless they are scaled in opposite direction before saving
    public static final float BB_MODEL_SCALE = 1f / 16f;

    /// Constant stolen from Math.DEGREES_TO_RADIANS
    public static final float DEG_TO_RAD = 0.017453292519943295f;

    /// Error texture id
    public static final Key ERROR_TEXTURE = key("error_texture");

    /// Atlas UI key reference
    public static final Key ATLAS_UI = key("ui");

    /// Atlas Blockbench key reference
    public static final Key ATLAS_BLOCKBENCH = key("model");

    // TODO: refine system properties
    public interface SystemProperties
    {
        String ENABLE_TRACY = "flare.enableTracy";

        static boolean booleanProperty(String property)
        {
            String val = System.getProperty(property);
            return val != null && (val.isEmpty() || Boolean.parseBoolean(val));
        }
    }

    /*
     * File paths
     */

    /// File to save visual settings
    public static final File VISUAL_SETTINGS_FILE = new File("settings" + File.separator + "flare_visual_settings.json");

    /// File to save visual settings
    public static final File FONT_DEBUG_SETTINGS_FILE = new File("settings" + File.separator + "font_debug_settings.json");

    /// Root folder for generated resources
    public static final File GENERATED_FOLDER = new File("generated");

    /// Root folder for generated resources
    public static final File GENERATED_FLARE = new File(GENERATED_FOLDER, NAMESPACE);

    /// Folder for generated debug files
    public static final File FLARE_DEBUG_FOLDER = new File(GENERATED_FLARE, "debug");

    /// Folder for generated debug atlases
    public static final File FLARE_DEBUG_ATLAS = new File(FLARE_DEBUG_FOLDER, "atlas");

    /// Exported executable for MSDF
    public static final File MSDF_EXE = new File(GENERATED_FLARE, "msdf_atlas_gen.exe");
    /// Exported natives for Tracy
    public static final File TRACY_NATIVE = new File(GENERATED_FLARE, "tracy-jni-amd64.dll");

    public static final File FLARE_MODULE = new File(SteveCore.MODULES, NAMESPACE);

    // TODO: replace all instances of flare key creation
    public static Key key(String id)
    {
        return Key.withNamespace(NAMESPACE, id);
    }
}
