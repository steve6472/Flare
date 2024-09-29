package steve6472.volkaniums.vr;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openvr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.core.log.Log;
import steve6472.volkaniums.*;
import steve6472.volkaniums.render.debug.DebugRender;
import steve6472.volkaniums.settings.Settings;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.lwjgl.openvr.VR.*;
import static org.lwjgl.openvr.VRSystem.VRSystem_GetRecommendedRenderTargetSize;
import static org.lwjgl.openvr.VRSystem.VRSystem_GetStringTrackedDeviceProperty;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/26/2024
 * Project: Volkaniums <br>
 */
public class VrData
{
    private static final Logger LOGGER = Log.getLogger(VrData.class);

    private static final boolean DEBUG_RENDER_EYES = false;
    private static final boolean DEBUG_RENDER_HMD = false;
    private static final boolean DEBUG_RENDER_OTHER = false;

    public static boolean VR_ON = false;
    private int token;
    public VrRenderPass vrRenderPass = VrRenderPass.NONE;

    private VkQueue graphicsQueue;

    private int width, height;
    private List<FrameBuffer> leftEyeBuffer;
    private List<FrameBuffer> rightEyeBuffer;

    private int validPoseCount;
    private Matrix4f HMDPose;
    private Matrix4f leftEyeProjection, leftEyePose;
    private Matrix4f rightEyeProjection, rightEyePose;

    public VrData()
    {
        if (!Settings.VR.get() || !VR.VR_IsRuntimeInstalled() || !VR.VR_IsHmdPresent())
            return;

        initVr();
    }

    /*
     * Setup
     */

    public void initVr()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer peError = stack.mallocInt(1);

            token = VR_InitInternal(peError, EVRApplicationType_VRApplication_Scene);
            if (peError.get(0) == 0)
            {
                OpenVR.create(token);

                LOGGER.finer("Model Number : " + VRSystem_GetStringTrackedDeviceProperty(k_unTrackedDeviceIndex_Hmd, ETrackedDeviceProperty_Prop_ModelNumber_String, peError));
                LOGGER.finer("Serial Number: " + VRSystem_GetStringTrackedDeviceProperty(k_unTrackedDeviceIndex_Hmd, ETrackedDeviceProperty_Prop_SerialNumber_String, peError));

                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                VRSystem_GetRecommendedRenderTargetSize(w, h);
                width = w.get(0);
                height = h.get(0);
                LOGGER.finer("Recommended width : " + w.get(0));
                LOGGER.finer("Recommended height: " + h.get(0));
                VR_ON = true;
            } else
            {
                LOGGER.warning("INIT ERROR SYMBOL: " + VR_GetVRInitErrorAsSymbol(peError.get(0)));
                LOGGER.warning("INIT ERROR  DESCR: " + VR_GetVRInitErrorAsEnglishDescription(peError.get(0)));
            }
        }
    }

    public void createVkResources(VkDevice device, VkQueue graphicsQueue)
    {
        if (!VR_ON) return;

        this.graphicsQueue = graphicsQueue;

        leftEyeBuffer = new ArrayList<>();
        rightEyeBuffer = new ArrayList<>();

        for (int i = 0; i < SwapChain.MAX_FRAMES_IN_FLIGHT; i++)
        {
            leftEyeBuffer.add(new FrameBuffer(device, width, height, VK_FORMAT_B8G8R8A8_UNORM, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT));
            rightEyeBuffer.add(new FrameBuffer(device, width, height, VK_FORMAT_B8G8R8A8_UNORM, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT));
        }
        setupCameras();
    }

    private void setupCameras()
    {
        leftEyeProjection = new Matrix4f();
        leftEyePose = new Matrix4f();
        rightEyeProjection = new Matrix4f();
        rightEyePose = new Matrix4f();
    }

    public long renderPass()
    {
        return leftEyeBuffer.getFirst().renderPass();
    }

    public int width()
    {
        return width;
    }

    public int height()
    {
        return height;
    }

    /*
     * Helpers
     */

    public void updateHDMMatrixPose()
    {
        if (!VR_ON)
            return;

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            TrackedDevicePose.Buffer posesBuffer = TrackedDevicePose.calloc(k_unMaxTrackedDeviceCount, stack);
            VRCompositor.VRCompositor_WaitGetPoses(posesBuffer, null);

            validPoseCount = 0;
            for (int deviceId = 0; deviceId < k_unMaxTrackedDeviceCount; deviceId++)
            {
                TrackedDevicePose trackedDevicePose = posesBuffer.get(deviceId);
                if (trackedDevicePose.bPoseIsValid())
                {
                    validPoseCount++;

                    Matrix4f transform = VrUtil.convertSteamVRMatrixToMatrix4f(trackedDevicePose.mDeviceToAbsoluteTracking());
                    DeviceType type = DeviceType.getDeviceType(VRSystem.VRSystem_GetTrackedDeviceClass(deviceId));

                    if (deviceId == k_unTrackedDeviceIndex_Hmd)
                        HMDPose = transform;

                    if (DEBUG_RENDER_HMD || (deviceId != k_unTrackedDeviceIndex_Hmd && DEBUG_RENDER_OTHER))
                    {
                        DebugRender.addDebugObjectForFrame(DebugRender.lineCube(new Vector3f(0, 0, 0), 0.25f * 0.5f, type.debugColor), transform);
                        DebugRender.addDebugObjectForFrame(DebugRender.cross(new Vector3f(0, 0, -0.25f * 0.5f), 0.1f, DebugRender.SKY_BLUE), transform);
                    }
                }
            }
        }
    }

    public void updateEyes(Camera camera)
    {
        if (!VR_ON) return;
        if (HMDPose == null) return;

        float near = camera.near();
        float far = camera.far();

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            HmdMatrix34.Buffer eyes = HmdMatrix34.calloc(2, stack);
            VRSystem.VRSystem_GetEyeToHeadTransform(EVREye_Eye_Left, eyes.get(0));
            VRSystem.VRSystem_GetEyeToHeadTransform(EVREye_Eye_Right, eyes.get(1));
            leftEyePose = VrUtil.convertSteamVRMatrixToMatrix4f(eyes.get(0));
            rightEyePose = VrUtil.convertSteamVRMatrixToMatrix4f(eyes.get(1));

            HMDPose.mul(leftEyePose, leftEyePose);
            HMDPose.mul(rightEyePose, rightEyePose);

            leftEyePose.invert();
            rightEyePose.invert();

            HmdMatrix44.Buffer projections = HmdMatrix44.calloc(2, stack);
            VRSystem.VRSystem_GetProjectionMatrix(EVREye_Eye_Left, near, far, projections.get(0));
            VRSystem.VRSystem_GetProjectionMatrix(EVREye_Eye_Right, near, far, projections.get(1));
            leftEyeProjection = VrUtil.convertSteamVRMatrixToMatrix4f(projections.get(0));
            rightEyeProjection = VrUtil.convertSteamVRMatrixToMatrix4f(projections.get(1));

            if (DEBUG_RENDER_EYES)
            {
                DebugRender.addDebugObjectForFrame(DebugRender.cross(new Vector3f(0, 0, 0), 0.1f, DebugRender.ROYAL_BLUE), new Matrix4f(leftEyePose).invert());
                DebugRender.addDebugObjectForFrame(DebugRender.cross(new Vector3f(0, 0, 0), 0.1f, DebugRender.ROYAL_BLUE), new Matrix4f(rightEyePose).invert());
            }
        }
    }

    /*
     * Cleanup
     */

    public void cleanup()
    {
        if (!VR_ON)
            return;

        leftEyeBuffer.forEach(FrameBuffer::cleanup);
        rightEyeBuffer.forEach(FrameBuffer::cleanup);

        VR_ShutdownInternal();
    }

    /*
     * Rendering
     */

    private void renderStereoTargets(FrameInfo frameInfo, MasterRenderer renderer)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            // Set viewport and scissor
            VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
            viewport.x(0f);
            viewport.y(0f);
            viewport.width(width);
            viewport.height(height);
            viewport.minDepth(0f);
            viewport.maxDepth(1f);
            vkCmdSetViewport(frameInfo.commandBuffer, 0, viewport);

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
            scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
            scissor.extent(VkExtent2D.calloc(stack).set(width, height));
            vkCmdSetScissor(frameInfo.commandBuffer, 0, scissor);

            // Render stuff to buffers
            frameInfo.camera.getProjectionMatrix().set(leftEyeProjection);
            frameInfo.camera.getViewMatrix().set(leftEyePose);
            FrameBuffer leftEyeBuffer = this.leftEyeBuffer.get(frameInfo.frameIndex);
            vrRenderPass = VrRenderPass.LEFT_EYE;

            transitionImageLayout(stack, frameInfo.commandBuffer, leftEyeBuffer);
            renderer.beginRenderPass(frameInfo.commandBuffer, stack, leftEyeBuffer.renderPass(), VkExtent2D.calloc(stack).set(width, height), leftEyeBuffer.framebuffer());
            renderer.render(frameInfo, stack);
            renderer.endRenderPass(frameInfo.commandBuffer);
            transitionImageLayoutBack(stack, frameInfo.commandBuffer, leftEyeBuffer);

            frameInfo.camera.getProjectionMatrix().set(rightEyeProjection);
            frameInfo.camera.getViewMatrix().set(rightEyePose);
            FrameBuffer rightEyeBuffer = this.rightEyeBuffer.get(frameInfo.frameIndex);
            vrRenderPass = VrRenderPass.RIGHT_EYE;

            transitionImageLayout(stack, frameInfo.commandBuffer, rightEyeBuffer);
            renderer.beginRenderPass(frameInfo.commandBuffer, stack, rightEyeBuffer.renderPass(), VkExtent2D.calloc(stack).set(width, height), rightEyeBuffer.framebuffer());
            renderer.render(frameInfo, stack);
            renderer.endRenderPass(frameInfo.commandBuffer);
            transitionImageLayoutBack(stack, frameInfo.commandBuffer, rightEyeBuffer);

            vrRenderPass = VrRenderPass.NONE;
        }
    }

    private void renderCompanion(FrameInfo frameInfo, MasterRenderer renderer)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            // Set viewport and scissor
            VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
            // Set viewport and scissor
            viewport.x(0f);
            viewport.y(renderer.getSwapChain().swapChainExtent.height());
            viewport.width(renderer.getSwapChain().swapChainExtent.width());
            viewport.height(-renderer.getSwapChain().swapChainExtent.height());
            viewport.minDepth(0f);
            viewport.maxDepth(1f);
            vkCmdSetViewport(frameInfo.commandBuffer, 0, viewport);

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
            scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
            scissor.extent(VkExtent2D.calloc(stack).set(width, height));
            vkCmdSetScissor(frameInfo.commandBuffer, 0, scissor);

            transitionImageLayoutForSubmit(stack, frameInfo.commandBuffer, leftEyeBuffer.get(frameInfo.frameIndex));
            transitionImageLayoutForSubmit(stack, frameInfo.commandBuffer, rightEyeBuffer.get(frameInfo.frameIndex));
        }
    }

    public void frame(VkDevice device, VkInstance instance, MasterRenderer renderer, FrameInfo frameInfo)
    {
        if (!VR_ON) return;
        if (HMDPose == null) return;

        renderStereoTargets(frameInfo, renderer);
        renderCompanion(frameInfo, renderer);

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VRTextureBounds bounds = VRTextureBounds.calloc(stack);
            bounds.uMin(0.0f);
            bounds.uMax(1.0f);
            bounds.vMin(0.0f);
            bounds.vMax(1.0f);

            VRVulkanTextureData textureData = VRVulkanTextureData.calloc(stack);
            textureData.m_nImage(leftEyeBuffer.get(frameInfo.frameIndex).image());
            textureData.m_pDevice(device.address());
            textureData.m_pPhysicalDevice(device.getPhysicalDevice().address());
            textureData.m_pInstance(instance.address());
            textureData.m_pQueue(graphicsQueue.address());
            textureData.m_nQueueFamilyIndex();

            textureData.m_nWidth(width);
            textureData.m_nHeight(height);
            textureData.m_nFormat(VK_FORMAT_B8G8R8A8_UNORM);

            Texture eyeTexture = Texture.calloc(stack);
            eyeTexture.set(textureData.address(), ETextureType_TextureType_Vulkan, EColorSpace_ColorSpace_Auto);

            int error = VRCompositor.VRCompositor_Submit(EVREye_Eye_Left, eyeTexture, bounds, 0);
            if (error != 0)
            {
                System.out.println("Submit Error Left: " + error);
            }

            textureData.m_nImage(rightEyeBuffer.get(frameInfo.frameIndex).image());
            eyeTexture.set(textureData.address(), ETextureType_TextureType_Vulkan, EColorSpace_ColorSpace_Auto);
            error = VRCompositor.VRCompositor_Submit(EVREye_Eye_Right, eyeTexture, bounds, 0);
            if (error != 0)
            {
                System.out.println("Submit Error Right: " + error);
            }
        }
    }

    // TODO: merge the next three methods, possibly create a VulkanUtil method
    private void transitionImageLayout(MemoryStack stack, VkCommandBuffer commandBuffer, FrameBuffer frameBuffer)
    {
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
        barrier.srcAccessMask(VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_TRANSFER_READ_BIT);
        barrier.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
        barrier.oldLayout(frameBuffer.imageLayout());
        barrier.newLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
        barrier.image(frameBuffer.image());
        barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        barrier.subresourceRange().baseMipLevel(0);
        barrier.subresourceRange().levelCount(1);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(1);
        barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);

        vkCmdPipelineBarrier(commandBuffer,
            VK_PIPELINE_STAGE_TRANSFER_BIT | VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
            VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
            0,
            null,
            null,
            barrier);
        frameBuffer.imageLayout(barrier.newLayout());

        if (frameBuffer.depthImageLayout() == VK_IMAGE_LAYOUT_UNDEFINED)
        {
            barrier.image(frameBuffer.depthImage());
            barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);
            barrier.srcAccessMask(0);
            barrier.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
            barrier.oldLayout(frameBuffer.depthImageLayout());
            barrier.newLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
            vkCmdPipelineBarrier(commandBuffer,
                VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
                VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT,
                0,
                null,
                null,
                barrier);
            frameBuffer.depthImageLayout(barrier.newLayout());
        }
    }

    private void transitionImageLayoutBack(MemoryStack stack, VkCommandBuffer commandBuffer, FrameBuffer frameBuffer)
    {
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
        barrier.srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
        barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
        barrier.oldLayout(frameBuffer.imageLayout());
        barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        barrier.image(frameBuffer.image());
        barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        barrier.subresourceRange().baseMipLevel(0);
        barrier.subresourceRange().levelCount(1);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(1);
        barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);

        vkCmdPipelineBarrier(commandBuffer,
            VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
            VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
            0,
            null,
            null,
            barrier);
        frameBuffer.imageLayout(barrier.newLayout());
    }

    private void transitionImageLayoutForSubmit(MemoryStack stack, VkCommandBuffer commandBuffer, FrameBuffer frameBuffer)
    {
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
        barrier.srcAccessMask(VK_ACCESS_SHADER_READ_BIT);
        barrier.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
        barrier.oldLayout(frameBuffer.imageLayout());
        barrier.newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
        barrier.image(frameBuffer.image());
        barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        barrier.subresourceRange().baseMipLevel(0);
        barrier.subresourceRange().levelCount(1);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(1);
        barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);

        vkCmdPipelineBarrier(commandBuffer,
            VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
            VK_PIPELINE_STAGE_TRANSFER_BIT,
            0,
            null,
            null,
            barrier);
        frameBuffer.imageLayout(barrier.newLayout());
    }
}
