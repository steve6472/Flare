package steve6472.volkaniums.ui.font.render;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.volkaniums.ui.font.style.FontStyleEntry;

import java.util.List;

/**
 * Created by steve6472
 * Date: 11/17/2024
 * Project: Volkaniums <br>
 */
public record TextMessage(List<TextLine> lines, float textSize, float maxWidth, Anchor anchor, Billboard billboard, Align align)
{
    private static final Anchor DEFAULT_ANCHOR = Anchor.CENTER;
    private static final Billboard DEFAULT_BILLBOARD = Billboard.FIXED;
    private static final Align DEFAULT_ALIGN = Align.CENTER;

    public static final Codec<TextMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        TextLine.CODEC_MESSAGE.listOf().fieldOf("text").forGetter(TextMessage::lines),
        Codec.FLOAT.optionalFieldOf("text_size", 1f).forGetter(TextMessage::textSize), // in units
        Codec.FLOAT.optionalFieldOf("max_width", 4f).forGetter(TextMessage::maxWidth), // in units
        Anchor.CODEC.optionalFieldOf("anchor", DEFAULT_ANCHOR).forGetter(TextMessage::anchor),
        Billboard.CODEC.optionalFieldOf("billboard", DEFAULT_BILLBOARD).forGetter(TextMessage::billboard),
        Align.CODEC.optionalFieldOf("align", DEFAULT_ALIGN).forGetter(TextMessage::align)
    ).apply(instance, TextMessage::new));

    public void iterateCharacters(MessageCharIterator info)
    {
        int lineIndex = 0;
        int indexWithinLine = 0;
        for (int i = 0; i < len(); i++)
        {
            TextLine textLine = lines.get(lineIndex);
            float glyphSize = textLine.size() == TextLine.MESSAGE_SIZE ? textSize : textLine.size();

            char c = textLine.charEntries()[indexWithinLine];
            MessageChar nextChar = null;
            if (indexWithinLine + 1 < textLine.charEntries().length)
            {
                char nextC = textLine.charEntries()[indexWithinLine + 1];
                FontStyleEntry style = textLine.style();
                nextChar = new MessageChar(style.style().font().glyphInfo(nextC), style, glyphSize);
            } else if (lines.size() < lineIndex + 1 && lines.get(lineIndex + 1).charEntries().length > 0)
            {
                TextLine nextLine = lines.get(lineIndex + 1);
                char nextC = nextLine.charEntries()[0];
                FontStyleEntry style = nextLine.style();
                float nextGlyphSize = nextLine.size() == TextLine.MESSAGE_SIZE ? textSize : nextLine.size();
                nextChar = new MessageChar(style.style().font().glyphInfo(nextC), style, nextGlyphSize);
            }

            FontStyleEntry style = textLine.style();
            info.iterate(new MessageChar(style.style().font().glyphInfo(c), style, glyphSize), nextChar);

            indexWithinLine++;
            if (indexWithinLine >= textLine.charEntries().length)
            {
                indexWithinLine = 0;
                lineIndex++;
            }
        }
    }

    public int len()
    {
        return lines.stream().mapToInt(line -> line.charEntries().length).sum();
    }

    public Pair<Character, FontStyleEntry> getChar(int index)
    {
        int totalLen = 0;
        for (TextLine line : lines)
        {
            if (index < line.charEntries().length + totalLen)
            {
                char c = line.charEntries()[index - totalLen];
                return Pair.of(c, line.style());
            } else
            {
                totalLen += line.charEntries().length;
            }
        }

        return null;
    }

    public float getWidth(int start, int end)
    {
        int totalLen = 0;
        int lineIndex = -1;
        int indexWithinLine = -1;
        for (int i = 0; i < lines.size(); i++)
        {
            TextLine line = lines.get(i);
            if (start < line.charEntries().length + totalLen)
            {
                lineIndex = i;
                indexWithinLine = start - totalLen;
                break;
            } else
            {
                totalLen += line.charEntries().length;
            }
        }

        if (lineIndex == -1)
            throw new RuntimeException("Out of bounds!");

        float totalWidth = 0;
        for (int i = 0; i < end - start; i++)
        {
            TextLine textLine = lines.get(lineIndex);
            float glyphSize = textLine.size() == TextLine.MESSAGE_SIZE ? textSize : textLine.size();

            char c = textLine.charEntries()[indexWithinLine];
            totalWidth += textLine.style().style().font().glyphInfo(c).advance() * glyphSize;

            indexWithinLine++;
            if (indexWithinLine >= textLine.charEntries().length)
            {
                indexWithinLine = 0;
                lineIndex++;
            }

            if (lineIndex > lines.size())
                throw new RuntimeException("Out of bounds!");
        }

        return totalWidth;
    }
}
