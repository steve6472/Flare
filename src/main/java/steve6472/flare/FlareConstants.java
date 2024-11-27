package steve6472.flare;

import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.core.registry.Key;

import java.io.File;

/**
 * Created by steve6472
 * Date: 8/18/2024
 * Project: Flare <br>
 */
public class FlareConstants
{
    public static final Vector3f CAMERA_UP = new Vector3f(0, 1, 0);

    /// Global clear color
    public static final Vector4f CLEAR_COLOR = new Vector4f(0.1f, 0.1f, 0.1f, 1.0f);

    /// Note: This makes models be unable to be saved unless they are scaled in opposite direction before saving
    public static final float BB_MODEL_SCALE = 1f / 16f;

    /// Constant stolen from Math.DEGREES_TO_RADIANS
    public static final float DEG_TO_RAD = 0.017453292519943295f;

    /// Blockbench texture atlas
    public static final Key BLOCKBENCH_TEXTURE = Key.defaultNamespace("blockbench_main");

    /// Used for code-generated resources
    public static final String ENGINE_NAMESPACE = "flare";

    /// Error texture id
    public static final String ERROR_TEXTURE = "*error_texture*";

    /*
     * File paths
     */

    /// File to save visual settings
    public static final File VISUAL_SETTINGS_FILE = new File("settings" + File.separator + "flare_visual_settings.json");

    /// Root folder for generated resources
    public static final File GENERATED_FOLDER = new File("generated");

    /// Root folder for generated resources
    public static final File GENERATED_FLARE = new File(GENERATED_FOLDER, ENGINE_NAMESPACE);

    /// Folder for generated debug files
    public static final File FLARE_DEBUG_FOLDER = new File(GENERATED_FLARE, "debug");

    /// Folder for generated debug files
    public static final File MSDF_EXE = new File(GENERATED_FLARE, "msdf_atlas_gen.exe");

    /// Root folder for modules
    /// TODO: replace all resources references with this
    public static final File MODULES = new File("modules");

    public static final File FLARE_MODULE = new File(MODULES, ENGINE_NAMESPACE);
}
