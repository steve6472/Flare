package steve6472.volkaniums.render;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import steve6472.volkaniums.settings.VisualSettings;
import steve6472.volkaniums.ui.font.*;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.MasterRenderer;
import steve6472.volkaniums.VkBuffer;
import steve6472.volkaniums.core.FrameInfo;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.pipeline.builder.PipelineConstructor;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.UBO;
import steve6472.volkaniums.struct.def.Vertex;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.volkaniums.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public class FontRenderSystem extends RenderSystem
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frame = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    private final VkBuffer buffer;

    public FontRenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
    {
        super(masterRenderer, pipeline);

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, MAX_FRAMES_IN_FLIGHT)
            .build();

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            frame.add(new FlightFrame());

            VkBuffer global = new VkBuffer(
                device,
                UBO.GLOBAL_CAMERA_UBO.sizeof(),
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            global.map();
            frame.get(i).uboBuffer = global;

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
                frame.get(i).descriptorSet = descriptorWriter
                    .writeBuffer(0, frame.get(i).uboBuffer, stack, UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT)
                    .writeImage(1, VolkaniumsRegistries.SAMPLER.get(Constants.FONT_TEXTURE), stack)
                    .build();
            }
        }

        buffer = new VkBuffer(
            masterRenderer.getDevice(),
            Vertex.POS3F_COL4F_UV.sizeof(),
            32768 * 6, // max 32k characters at once, should be enough....
            VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        );
        buffer.map();
    }

    @Override
    public long[] setLayouts()
    {
        return new long[] {globalSetLayout.descriptorSetLayout};
    }

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        TextRender textRender = getMasterRenderer().textRender();
        //noinspection deprecation
        List<TextLine> lines = textRender.lines();

        if (lines.isEmpty())
            return;

        List<Struct> verticies = createFromChars(lines);

        FlightFrame flightFrame = frame.get(frameInfo.frameIndex());

        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(frameInfo.camera().getProjectionMatrix(), frameInfo.camera().getViewMatrix());
        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_CAMERA_UBO::memcpy, List.of(globalUBO), singleInstanceSize, singleInstanceSize * frameInfo.camera().cameraIndex);
        flightFrame.uboBuffer.flush(singleInstanceSize, (long) singleInstanceSize * frameInfo.camera().cameraIndex);

        pipeline().bind(frameInfo.commandBuffer());

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer(),
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline().pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            stack.ints(singleInstanceSize * frameInfo.camera().cameraIndex));

        buffer.writeToBuffer(Vertex.POS3F_COL4F_UV::memcpy, verticies);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), verticies.size(), 1, 0, 0);

        lines.clear();
    }

    private List<Struct> createFromChars(List<TextLine> lines)
    {
        List<Struct> structs = new ArrayList<>(lines.size() * 6 * 32);

        for (TextLine line : lines)
        {
            createTextLine(line, structs);
        }
        return structs;
    }

    private void createTextLine(TextLine line, List<Struct> structs)
    {
        float size = line.size() / (float) VisualSettings.FONT_QUALITY.get();
        float x = line.startPos().x;
        float y = line.startPos().y;
        float z = line.startPos().z;

        for (CharEntry charEntry : line.charEntries())
        {
            GlyphInfo glyphInfo = getMasterRenderer().textRender().glyphInfo(charEntry.character());
            if (glyphInfo == null)
                glyphInfo = getMasterRenderer().textRender().errorGlyph();

            if (glyphInfo == null)
                throw new RuntimeException("Not even Error char found! Abort the mission!");

            if (!glyphInfo.isInvisible())
                createChar(charEntry, glyphInfo, x, y, z, size, structs);

            x += (glyphInfo.advance() >> 6) * size;
        }
    }

    private void createChar(CharEntry entry, GlyphInfo glyphInfo, float x, float y, float z, float size, List<Struct> structs)
    {
        Vector4f color = entry.color();
        Vector2f tl = new Vector2f(glyphInfo.texturePos().x, glyphInfo.texturePos().y);
        Vector2f br = new Vector2f(glyphInfo.texturePos().z, glyphInfo.texturePos().w);

        float xpos = x + glyphInfo.bearing().x * size;
        float ypos = y - (glyphInfo.size().y - glyphInfo.bearing().y) * size;

        float w = glyphInfo.size().x * size;
        float h = glyphInfo.size().y * size;

        Vector3f vtl = new Vector3f(xpos, ypos + h, z);
        Vector3f vbl = new Vector3f(xpos, ypos, z);
        Vector3f vbr = new Vector3f(xpos + w, ypos, z);
        Vector3f vtr = new Vector3f(xpos + w, ypos + h, z);

        structs.add(Vertex.POS3F_COL4F_UV.create(vtl, color, new Vector2f(tl.x, tl.y)));
        structs.add(Vertex.POS3F_COL4F_UV.create(vbl, color, new Vector2f(tl.x, br.y)));
        structs.add(Vertex.POS3F_COL4F_UV.create(vbr, color, new Vector2f(br.x, br.y)));

        structs.add(Vertex.POS3F_COL4F_UV.create(vbr, color, new Vector2f(br.x, br.y)));
        structs.add(Vertex.POS3F_COL4F_UV.create(vtr, color, new Vector2f(br.x, tl.y)));
        structs.add(Vertex.POS3F_COL4F_UV.create(vtl, color, new Vector2f(tl.x, tl.y)));
    }

    @Override
    public void cleanup()
    {
        globalSetLayout.cleanup();
        globalPool.cleanup();

        if (buffer != null)
            buffer.cleanup();

        for (FlightFrame flightFrame : frame)
        {
            flightFrame.uboBuffer.cleanup();
            if (flightFrame.vertexBuffer != null)
            {
                flightFrame.vertexBuffer.cleanup();
            }
        }
    }

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        VkBuffer vertexBuffer;
        long descriptorSet;
    }
}
