package steve6472.volkaniums.util;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.regex.Pattern;

/**********************
 * Created by steve6472
 * On date: 4/3/2021
 * Project: StevesGameEngine
 *
 ***********************/
public class MathUtil
{
	public static final float INV_255 = 1f / 255f;

	private static final float FLOAT_COMPARE_PRECISION = 0.00000001f;
	private static final Pattern IS_DECIMAL = Pattern.compile("([+-]?\\d*(\\.\\d+)?)+");
	private static final Pattern IS_INTEGER = Pattern.compile("([+-]?\\d)+");

	/*
	 * Clamp
	 */

	public static double clamp(double number, double min, double max)
	{
		return Math.min(Math.max(number, min), max);
	}

	public static float clamp(float number, float min, float max)
	{
		return Math.min(Math.max(number, min), max);
	}

	public static int clamp(int number, int min, int max)
	{
		return Math.min(Math.max(number, min), max);
	}

	/*
	 * Is number in range
	 */

	public static boolean isNumberInRange(double number, double min, double max)
	{
		return (number >= min && number <= max);
	}

	public static boolean isNumberInRange(float number, float min, float max)
	{
		return (number >= min && number <= max);
	}

	public static boolean isNumberInRange(int number, int min, int max)
	{
		return (number >= min && number <= max);
	}

	/*
	 * Compare float
	 */

	public static boolean compareFloat(float f1, float f2)
	{
		return Math.abs(f1 - f2) < FLOAT_COMPARE_PRECISION;
	}

	public static boolean compareFloat(float f1, float f2, float epsilon)
	{
		return Math.abs(f1 - f2) < epsilon;
	}

	/*
	 * Animation Util
	 */

	public static double time(double start, double end, double time)
	{
		// Prevent NaN
		if (time - start == 0 || end - start == 0)
			return 0;

		return 1.0 / ((end - start) / (time - start));
	}

	public static double lerp(double start, double end, double value)
	{
		return start + value * (end - start);
	}

	public static double catmullLerp(double p0, double p1, double p2, double p3, double t)
	{
		return 0.5 * ((2.0 * p1) + (p2 - p0) * t + (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t * t + (3.0 * p1 - p0 - 3.0 * p2 + p3) * t * t * t);
	}

	/*
	 * Geometry
	 */

	/*
	 * 3D
	 */

	public static Vector3f yawPitchToVector(float yaw, float pitch)
	{
		float xzLen = (float) Math.cos(pitch);
		return new Vector3f(xzLen * (float) Math.cos(yaw), (float) Math.sin(pitch), xzLen * (float) Math.sin(-yaw));
	}

	public static Vector3f calculateMouseRay(Matrix4f viewMatrix, Matrix4f projectionMatrix, int mouseX, int mouseY, int screenWidth, int screenHeight)
	{
		Vector2f normalizedCoords = getNormalisedDeviceCoordinates(mouseX, mouseY, screenWidth, screenHeight);
		Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
		Vector4f eyeCoords = toEyeCoords(clipCoords, projectionMatrix);
		return toWorldCoords(eyeCoords, viewMatrix);
	}

	public static Vector3f toWorldCoords(Vector4f eyeCoords, Matrix4f viewMatrix)
	{
		Matrix4f invertedView = new Matrix4f(viewMatrix).invert();
		Vector4f rayWorld = new Vector4f();
		invertedView.transform(eyeCoords, rayWorld);
		Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
		mouseRay.normalize();
		return mouseRay;
	}

	public static Vector4f toEyeCoords(Vector4f clipCoords, Matrix4f projectionMatrix)
	{
		Matrix4f invertedProjection = new Matrix4f(projectionMatrix).invert();
		Vector4f eyeCoords = new Vector4f();
		invertedProjection.transform(clipCoords, eyeCoords);
		return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
	}

	public static Vector2f getNormalisedDeviceCoordinates(float mouseX, float mouseY, int screenWidth, int screenHeight)
	{
		float x = (2.0f * mouseX) / (float) screenWidth - 1f;
		float y = (2.0f * mouseY) / (float) screenHeight - 1f;
		return new Vector2f(x, -y);
	}

	public static void toScreenPos(Matrix4f viewMatrix, Matrix4f projectionMatrix, Vector3f worldPos, int screenWidth, int screenHeight, Vector2f destination)
	{
		Vector4f clipSpacePos = new Vector4f(worldPos, 1.0f).mul(viewMatrix).mul(projectionMatrix);
		destination.set(((clipSpacePos.x / clipSpacePos.w) / 2.0f) * (float) screenWidth, ((clipSpacePos.y / clipSpacePos.w) / 2.0f) * (float) screenHeight);
	}

	/*
	 * 2D
	 */

	public static boolean isInRectangle(int rminx, int rminy, int rmaxx, int rmaxy, int px, int py)
	{
		return px >= rminx && px <= rmaxx && py >= rminy && py <= rmaxy;
	}

	public static boolean isInRectangle(double rminx, double rminy, double rmaxx, double rmaxy, double px, double py)
	{
		return px >= rminx && px <= rmaxx && py >= rminy && py <= rmaxy;
	}

	public static float normalize(float x, float minX, float maxX)
	{
		return (x - minX) / (maxX - minX);
	}

	public static double normalize(double x, double minX, double maxX)
	{
		return (x - minX) / (maxX - minX);
	}

	/*
	 * Other
	 */

	public static boolean isEven(int number)
	{
		return number % 2 == 0;
	}

	public static boolean isInteger(String text)
	{
		return IS_INTEGER.matcher(text).matches();
	}

	public static boolean isDecimal(String text)
	{
		return IS_DECIMAL.matcher(text).matches();
	}

	/**
	 * @param rotTime time it takes to make 1 revolution of a circle in seconds
	 * @return radians
	 */
	public static double animateRadians(double rotTime)
	{
		rotTime = rotTime / Math.PI;
		return Math.toRadians((System.currentTimeMillis() % (3600 * rotTime)) / (10.0 * rotTime));
	}

	public static Matrix4f createProjectionMatrix(float width, float height, float farPlane, float fov)
	{
		final float NEAR_PLANE = 0.1f;
		float aspectRatio = width / height;

		return new Matrix4f().perspective((float) Math.toRadians(fov), aspectRatio, NEAR_PLANE, farPlane);
	}

	public static float smoothstep(float edge0, float edge1, float x)
	{
		// Scale, bias and saturate x to 0..1 range
		x = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
		// Evaluate polynomial
		return x * x * (3 - 2 * x);
	}
}
