package steve6472.volkaniums.core;

import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.vulkan.*;
import steve6472.core.SteveCore;
import steve6472.core.log.Log;
import steve6472.core.setting.SettingsLoader;
import steve6472.volkaniums.*;
import steve6472.volkaniums.input.UserInput;
import steve6472.volkaniums.registry.RegistryCreators;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.settings.ValidationLevel;
import steve6472.volkaniums.settings.VisualSettings;
import steve6472.volkaniums.vr.VrData;
import steve6472.volkaniums.vr.VrUtil;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK13.*;
import static steve6472.volkaniums.render.debug.DebugRender.*;

public class Volkaniums
{
    private static final Logger LOGGER = Log.getLogger(Volkaniums.class);

    private static Volkaniums INSTANCE;

    // ======= FIELDS ======= //

    private VolkaniumsApp app;
    private Window window;
    private MasterRenderer renderer;
    private VkInstance instance;
    private VkPhysicalDevice physicalDevice;
    private long debugMessenger;
    private long surface;
    private VkDevice device;
    private VkQueue graphicsQueue;
    private VkQueue presentQueue;
    private VrData vrData;

    // ======= METHODS ======= //

    private Volkaniums() { }

    public static void start(VolkaniumsApp app)
    {
        if (INSTANCE != null)
            throw new RuntimeException("Volkaniums already started!");

        Configuration.HARFBUZZ_LIBRARY_NAME.set(FreeType.getLibrary());

        Volkaniums volkaniums = new Volkaniums();
        SteveCore.DEFAULT_KEY_NAMESPACE = app.defaultNamespace();
        INSTANCE = volkaniums;
        volkaniums.app = app;
        volkaniums.start();
    }

    private void start()
    {
        createGeneratedFolder();
        window = new Window(app.windowTitle());
        app.userInput = new UserInput(window);
        app.preInit();
        app.camera = app.setupCamera();
        initContent(); // initRegistries & loadSettings
        initVulkan(); // createRenderSystems
        app.postInit();
        mainLoop();
        cleanup();
    }

    private void createGeneratedFolder()
    {
        if (!Constants.GENERATED_FOLDER.exists())
        {
            if (!Constants.GENERATED_FOLDER.mkdirs())
            {
                LOGGER.severe("Could not create 'generated' folder at " + Constants.GENERATED_FOLDER.getAbsolutePath());
                throw new RuntimeException("Could not geenrate generated folder");
            }
        }
    }

    private void initContent()
    {
        RegistryCreators.init(VolkaniumsRegistries.VISUAL_SETTINGS);
        app.initRegistries();

        RegistryCreators.createContents();
        SettingsLoader.loadFromJsonFile(VolkaniumsRegistries.VISUAL_SETTINGS, Constants.VISUAL_SETTINGS_FILE);
        app.loadSettings();
    }

    private void initVulkan()
    {
        createInstance();
        debugMessenger = VulkanValidation.setupDebugMessenger(instance);
        createSurface();
        Commands commands = new Commands();
        vrData = new VrData();
        physicalDevice = PhysicalDevicePicker.pickPhysicalDevice(instance, surface, PhysicalDevicePicker.DEVICE_EXTENSIONS);
        createLogicalDevice();
        vrData.createVkResources(device, graphicsQueue);
        commands.createCommandPool(device, surface);
        RegistryCreators.createVkContents(device, commands, graphicsQueue);
        renderer = new MasterRenderer(window, device, graphicsQueue, presentQueue, commands, surface, vrData);
        app.createRenderSystems(renderer);
        renderer.builtinLast();
        renderer.getSwapChain().createSwapChainObjects();
    }

    private void mainLoop()
    {
        long currentTime = System.nanoTime();
        long secondCounter = currentTime;
        float lastFps = 0;
        while (!window.shouldWindowClose())
        {
            glfwPollEvents();

            long newTime = System.nanoTime();
            float frameTime = (newTime - currentTime) * 1e-9f;
            currentTime = newTime;

            VkCommandBuffer commandBuffer;
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer pImageIndex = stack.mallocInt(1);
                if ((commandBuffer = renderer.beginFrame(stack, pImageIndex)) != null)
                {
                    FrameInfo frameInfo = new FrameInfo();
                    frameInfo.frameTime = frameTime;
                    frameInfo.camera = app.camera;
                    frameInfo.frameIndex = renderer.getCurrentFrameIndex();
                    frameInfo.commandBuffer = commandBuffer;
                    frameInfo.camera.cameraIndex = 0;

                    if (VisualSettings.RENDER_CENTER_POINT.get())
                        addDebugObjectForFrame(cross(new Vector3f(0, 0, 0), 0.5f, DARK_GRAY));

                    // Render
                    renderer.totalRenderCount = 0;

                    app.render(frameInfo, stack);

                    renderer.beginSwapChainRenderPass(commandBuffer, stack);
                    renderer.render(frameInfo, stack);
                    renderer.endRenderPass(commandBuffer);

                    vrData.frame(device, instance, renderer, frameInfo);
                    renderer.maxRenderCount = renderer.totalRenderCount;

                    renderer.endFrame(stack, pImageIndex);

                    if (System.nanoTime() - secondCounter > 1e9)
                    {
                        lastFps = 1000f / (frameTime * 1e3f);
                        secondCounter = System.nanoTime();
                    }

                    if (VisualSettings.TITLE_FPS.get())
                        window.setWindowTitle("FPS: %.4f,  Frame time: %.4fms %n".formatted(lastFps, frameTime * 1e3f));

                    vrData.updateHDMMatrixPose();
                    vrData.updateEyes(frameInfo.camera);
                }
            }
        }

        // Wait for the device to complete all operations before release resources
        vkDeviceWaitIdle(device);
    }

    private void cleanup()
    {
        app.saveSettings();
        // Save settings
        SettingsLoader.saveToJsonFile(VolkaniumsRegistries.VISUAL_SETTINGS, Constants.VISUAL_SETTINGS_FILE);

        LOGGER.fine("Cleanup");

        renderer.cleanup();
        vrData.cleanup();
        app.cleanup();

        VolkaniumsRegistries.STATIC_MODEL.keys().forEach(key -> VolkaniumsRegistries.STATIC_MODEL.get(key).destroy());
        VolkaniumsRegistries.ANIMATED_MODEL.keys().forEach(key -> VolkaniumsRegistries.ANIMATED_MODEL.get(key).destroy());
        VolkaniumsRegistries.SAMPLER.keys().forEach(key -> VolkaniumsRegistries.SAMPLER.get(key).cleanup(device));

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
        if (VisualSettings.VALIDATION_LEVEL.get() != ValidationLevel.NONE && !VulkanValidation.checkValidationLayerSupport())
        {
            LOGGER.warning("Validation is enabled in settings, but no Validation Layer Support exists!");
        }

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack);

            appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
            appInfo.pApplicationName(stack.UTF8Safe(app.windowTitle()));
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
            deviceFeatures.samplerAnisotropy(true);
            deviceFeatures.wideLines(VisualSettings.ENABLE_WIDE_LINES.get());

            VkPhysicalDeviceVulkan11Features vulkan11Features = VkPhysicalDeviceVulkan11Features.calloc(stack);
            vulkan11Features.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_1_FEATURES);
            vulkan11Features.shaderDrawParameters(true);

            VkPhysicalDeviceVulkan12Features vulkan12features = VkPhysicalDeviceVulkan12Features.calloc(stack);
            vulkan12features.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_2_FEATURES);
            vulkan12features.runtimeDescriptorArray(true);

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
            createInfo.pNext(vulkan11Features);
            createInfo.pNext(vulkan12features);
            createInfo.pQueueCreateInfos(queueCreateInfos);
            // queueCreateInfoCount is automatically set

            createInfo.pEnabledFeatures(deviceFeatures);

            Collection<String> extensions = new HashSet<>(PhysicalDevicePicker.DEVICE_EXTENSIONS);

            if (VrData.VR_ON)
            {
                String requiredExtensions = VrUtil.getRequiredExtensions(physicalDevice.address());
                Collections.addAll(extensions, requiredExtensions.split(" "));
            }

            createInfo.ppEnabledExtensionNames(VulkanUtil.asPointerBuffer(stack, extensions));

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
}