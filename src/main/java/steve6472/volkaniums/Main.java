package steve6472.volkaniums;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.settings.Settings;
import steve6472.volkaniums.util.Log;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK13.*;
import static steve6472.volkaniums.SwapChain.MAX_FRAMES_IN_FLIGHT;

public class Main
{
    private static final Logger LOGGER = Log.getLogger(Main.class);

    public static final String BASE_NAMESPACE = "base";
    private static final String APP_NAME = "Volkaniums";

    // ======= FIELDS ======= //

    private Window window;
    private UserInput userInput;
    private Renderer renderer;
    private DescriptorPool globalPool;
    private VkInstance instance;
    private VkPhysicalDevice physicalDevice;
    private DescriptorSetLayout globalSetLayout;
    private long debugMessenger;
    private long surface;
    private VkDevice device;
    private VkQueue graphicsQueue;
    private VkQueue presentQueue;
    private Camera camera;

    // ======= METHODS ======= //

    private void run()
    {
        camera = new Camera();

        window = new Window();
        userInput = new UserInput(window);
        initContent();
        initVulkan();
        mainLoop();
        cleanup();
    }

    private void initVulkan()
    {
        createInstance();
        debugMessenger = VulkanValidation.setupDebugMessenger(instance);
        createSurface();
        physicalDevice = PhysicalDevicePicker.pickPhysicalDevice(instance, surface);
        createLogicalDevice();
        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .build();
        renderer = new Renderer(window, device, graphicsQueue, presentQueue, surface, globalSetLayout.descriptorSetLayout);
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, MAX_FRAMES_IN_FLIGHT)
            .build();
    }

    private void initContent()
    {
        Registries.createContents();
    }

    private void mainLoop()
    {
        List<VkBuffer> uboBuffers = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            VkBuffer buffer = new VkBuffer(
                device,
                GlobalUBO.SIZEOF,
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            buffer.map();

            uboBuffers.add(buffer);
        }

        List<Long> descriptorSets = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
                long set = descriptorWriter.writeBuffer(0, uboBuffers.get(i), stack).build();
                descriptorSets.add(set);
            }
        }

        long currentTime = System.nanoTime();
        while (!window.shouldWindowClose())
        {
            glfwPollEvents();

            long newTime = System.nanoTime();
            float frameTime = (newTime - currentTime) * 1e-9f;
            currentTime = newTime;

            float aspect = renderer.getAspectRation();
            camera.setPerspectiveProjection(Settings.FOV.get(), aspect, 0.1f, 1024f);

            VkCommandBuffer commandBuffer;
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer pImageIndex = stack.mallocInt(1);
                if ((commandBuffer = renderer.beginFrame(stack, pImageIndex)) != null)
                {
                    FrameInfo frameInfo = new FrameInfo();
                    frameInfo.frameTime = frameTime;
                    frameInfo.camera = camera;
                    frameInfo.frameIndex = renderer.getCurrentFrameIndex();
                    frameInfo.commandBuffer = commandBuffer;
                    frameInfo.globalDescriptorSet = descriptorSets.get(frameInfo.frameIndex);
                    // Update

                    GlobalUBO globalUBO = new GlobalUBO();
                    globalUBO.projection.set(camera.getProjectionMatrix());
                    globalUBO.view.identity().translate(0, 0, -2);

                    uboBuffers.get(frameInfo.frameIndex).writeToBuffer(GlobalUBO.MEMCPY, globalUBO);
                    uboBuffers.get(frameInfo.frameIndex).flush();

                    // Render
                    renderer.beginSwapChainRenderPass(commandBuffer, stack);
                    renderer.recordCommandBuffer(frameInfo, stack);
                    renderer.endSwapChainRenderPass(commandBuffer);
                    renderer.endFrame(stack, pImageIndex);
                }
            }
        }

        // Wait for the device to complete all operations before release resources
        vkDeviceWaitIdle(device);

        globalSetLayout.cleanup();
        for (VkBuffer uboBuffer : uboBuffers)
            uboBuffer.cleanup();
    }

    private void cleanup()
    {
        LOGGER.fine("Cleanup");

        renderer.cleanup();
        globalPool.cleanup();

        vkDestroyDevice(device, null);

        if (VulkanValidation.ENABLE_VALIDATION_LAYERS)
            VulkanValidation.destroyDebugUtilsMessengerEXT(instance, debugMessenger, null);

        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
        vkDestroyInstance(instance, null);
        window.destroyWindow();

        glfwTerminate();
    }

    private void createInstance()
    {
        if (VulkanValidation.ENABLE_VALIDATION_LAYERS && !VulkanValidation.checkValidationLayerSupport())
            throw new RuntimeException(ErrorCode.VALIDATION_NOT_SUPPORTED.format());

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack);

            appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
            appInfo.pApplicationName(stack.UTF8Safe(APP_NAME));
            appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
            appInfo.pEngineName(stack.UTF8Safe("No Engine"));
            appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
            appInfo.apiVersion(VK_API_VERSION_1_2);

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
            createInfo.pApplicationInfo(appInfo);
            createInfo.ppEnabledExtensionNames(VulkanValidation.getRequiredExtensions(stack));

            if (VulkanValidation.ENABLE_VALIDATION_LAYERS)
            {
                createInfo.ppEnabledLayerNames(VulkanUtil.asPointerBuffer(stack, VulkanValidation.getValidationLayers()));

                VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
                VulkanValidation.populateDebugMessengerCreateInfo(debugCreateInfo);
                createInfo.pNext(debugCreateInfo.address());
            }

            // We need to retrieve the pointer of the created instance
            PointerBuffer instancePtr = stack.mallocPointer(1);

            if (vkCreateInstance(createInfo, null, instancePtr) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.FAILED_TO_CREATE_VULKAN_INSTANCE.format());
            }

            instance = new VkInstance(instancePtr.get(0), createInfo);
        }
    }

    private void createLogicalDevice()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            QueueFamilyIndices indices = QueueFamilyIndices.findQueueFamilies(physicalDevice, surface);

            int[] uniqueQueueFamilies = indices.unique();

            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(uniqueQueueFamilies.length, stack);

            for (int i = 0; i < uniqueQueueFamilies.length; i++)
            {
                VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i);
                queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
                queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
            }

            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
            createInfo.pQueueCreateInfos(queueCreateInfos);
            // queueCreateInfoCount is automatically set

            createInfo.pEnabledFeatures(deviceFeatures);

            createInfo.ppEnabledExtensionNames(VulkanUtil.asPointerBuffer(stack, PhysicalDevicePicker.DEVICE_EXTENSIONS));

            if (VulkanValidation.ENABLE_VALIDATION_LAYERS)
            {
                createInfo.ppEnabledLayerNames(VulkanUtil.asPointerBuffer(stack, VulkanValidation.getValidationLayers()));
            }

            PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);

            if (vkCreateDevice(physicalDevice, createInfo, null, pDevice) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.NO_LOGICAL_DEVICE.format());
            }

            device = new VkDevice(pDevice.get(0), physicalDevice, createInfo);

            PointerBuffer pQueue = stack.pointers(VK_NULL_HANDLE);

            vkGetDeviceQueue(device, indices.graphicsFamily, 0, pQueue);
            graphicsQueue = new VkQueue(pQueue.get(0), device);

            vkGetDeviceQueue(device, indices.presentFamily, 0, pQueue);
            presentQueue = new VkQueue(pQueue.get(0), device);
        }
    }

    private void createSurface()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);

            if (GLFWVulkan.glfwCreateWindowSurface(instance, window.window(), null, pSurface) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.WINDOW_SURFACE_CREATION.format());
            }

            surface = pSurface.get(0);
        }
    }

    public UserInput getUserInput()
    {
        return userInput;
    }

    public static void main(String[] args)
    {
        Main main = new Main();
        main.run();
    }
}