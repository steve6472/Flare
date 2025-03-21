package steve6472.flare.ui.font.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import steve6472.flare.ui.font.Font;
import steve6472.flare.ui.font.layout.GlyphInfo;
import steve6472.flare.ui.font.layout.Metrics;
import steve6472.flare.ui.font.style.FontStyleEntry;

import java.text.BreakIterator;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Created by steve6472
 * Date: 11/17/2024
 * Project: Flare <br>
 */
public record Text(List<TextPart> parts, float textSize, float maxWidth, float maxHeight, Anchor2D anchor, Align align, float lineGapOffset, boolean forceSingleLine, VerticalAnchorMode verticalAnchor)
{
    public static final Codec<Text> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        TextPart.CODEC.listOf().optionalFieldOf("parts", List.of()).forGetter(Text::parts),
        Codec.FLOAT.optionalFieldOf("text_size", 8f).forGetter(Text::textSize),
        Codec.FLOAT.optionalFieldOf("max_width", 0f).forGetter(Text::maxWidth),
        Codec.FLOAT.optionalFieldOf("max_height", 0f).forGetter(Text::maxHeight),
        Anchor2D.CODEC.optionalFieldOf("anchor", Anchor2D.MIDDLE_CENTER).forGetter(Text::anchor),
        Align.CODEC.optionalFieldOf("align", Align.LEFT).forGetter(Text::align),
        Codec.FLOAT.optionalFieldOf("line_gap_offset", 0f).forGetter(Text::lineGapOffset),
        Codec.BOOL.optionalFieldOf("force_single_line", false).forGetter(Text::forceSingleLine),
        VerticalAnchorMode.CODEC.optionalFieldOf("vertical_anchor", VerticalAnchorMode.MAX_HEIGHT).forGetter(Text::verticalAnchor)
    ).apply(instance, Text::new));

    public void iterateCharacters(MessageCharIterator info)
    {
        int lineIndex = 0;
        int indexWithinLine = 0;
        for (int i = 0; i < len(); i++)
        {
            TextPart textLine = parts.get(lineIndex);
            float glyphSize = textLine.size() == TextPart.MESSAGE_SIZE ? textSize : textLine.size();

            char c = textLine.text().charAt(indexWithinLine);
            MessageChar nextChar = null;
            if (indexWithinLine + 1 < textLine.text().length())
            {
                char nextC = textLine.text().charAt(indexWithinLine + 1);
                FontStyleEntry style = textLine.style();
                nextChar = new MessageChar(style.style().font().glyphInfo(nextC), style, glyphSize);
            } else if (parts.size() < lineIndex + 1 && !parts.get(lineIndex + 1).text().isEmpty())
            {
                TextPart nextLine = parts.get(lineIndex + 1);
                char nextC = nextLine.text().charAt(0);
                FontStyleEntry style = nextLine.style();
                float nextGlyphSize = nextLine.size() == TextPart.MESSAGE_SIZE ? textSize : nextLine.size();
                nextChar = new MessageChar(style.style().font().glyphInfo(nextC), style, nextGlyphSize);
            }

            FontStyleEntry style = textLine.style();
            info.iterate(new MessageChar(style.style().font().glyphInfo(c), style, glyphSize), nextChar);

            indexWithinLine++;
            if (indexWithinLine >= textLine.text().length())
            {
                indexWithinLine = 0;
                lineIndex++;
            }
        }
    }

    public int len()
    {
        return parts.stream().mapToInt(line -> line.text().length()).sum();
    }

    public float getWidth(int start, int end)
    {
        float[] tempWidthSum = {0};
        MessageChar[] tempLastChar = {null};

        iterateCharacters(start, end, nextChar -> {
            if (tempLastChar[0] == null)
            {
                tempLastChar[0] = nextChar;
                tempWidthSum[0] += nextChar.glyph().advance() * nextChar.size();
                return;
            }

            MessageChar lastChar = tempLastChar[0];
            Font font = lastChar.style().style().font();

            if (font == nextChar.style().style().font())
            {
                tempWidthSum[0] += font.kerningAdvance((char) lastChar.glyph().index(), (char) nextChar.glyph().index()) * nextChar.size();
            }

            tempLastChar[0] = nextChar;
            tempWidthSum[0] += nextChar.glyph().advance() * nextChar.size();
        });
        return tempWidthSum[0];
    }

    public String rawString(int start, int end)
    {
        StringBuilder bob = new StringBuilder();
        iterateCharacters(start, end, character -> bob.append((char) character.glyph().index()));
        return bob.toString();
    }

    public List<TextRenderSegment> createSegments()
    {
        if (parts.isEmpty())
            return List.of();

        BreakIterator breakIterator = BreakIterator.getLineInstance();
        StringBuilder bobTheBuilder = new StringBuilder();
        parts().forEach(l -> bobTheBuilder.append(l.text()));
        breakIterator.setText(bobTheBuilder.toString());

        List<TextRenderSegment> messageSegments = new ArrayList<>();

        // TODO: optimize this when forceSingleLine == true
        // Calculate break indicies - character after which new line should be created
        int current = breakIterator.next();
        IntList breakIndicies = new IntArrayList(8);
        int previous = 0;
        float totalWidth = 0;
        while (current != BreakIterator.DONE)
        {
            int trimmedCurrent = previous + rawString(previous, current).stripTrailing().length();
            float trimmedWidth = getWidth(previous, trimmedCurrent);
            float width = getWidth(previous, current);

            if (!forceSingleLine && (totalWidth + trimmedWidth) > maxWidth())
            {
                breakIndicies.add(previous);
                totalWidth = width;
            } else
            {
                totalWidth += width;
            }

            previous = current;
            current = breakIterator.next();
        }

        // Fixes the "new line when just one long word is over lenght" problem
        if (breakIndicies.isEmpty() || breakIndicies.getInt(0) != 0)
            breakIndicies.addFirst(0);
        breakIndicies.add(bobTheBuilder.length());

        // Calculate message segment values for rendering
        for (int i = 0; i < breakIndicies.size() - 1; i++)
        {
            TextRenderSegment segment = new TextRenderSegment();
            segment.start = breakIndicies.getInt(i);
            segment.end = breakIndicies.getInt(i + 1);

            String string = rawString(segment.start, segment.end).stripTrailing();
            int strippedEnd = segment.start + string.length();
            segment.width = getWidth(segment.start, strippedEnd);

            characterStream(segment.start, strippedEnd).forEach(ch ->
            {
                Metrics metrics = ch.style().style().font().getMetrics();
                float size = ch.size();

                segment.fontHeight = Math.max(segment.fontHeight, metrics.lineHeight() * size);
                segment.minAscender = Math.min(segment.minAscender, metrics.ascender() * size);
                segment.maxLineHeight = Math.max(segment.maxLineHeight, metrics.lineHeight() * size);

                if (!ch.glyph().isInvisible())
                {
                    if (ch.glyph().planeBounds().bottom() >= 0)
                    {
                        segment.minDescent = Math.min(segment.minDescent, ch.glyph().planeBounds().bottom() * size);
                    }
                    segment.maxDescent = Math.max(segment.maxDescent, ch.glyph().planeBounds().bottom() * size);
                }
            });

            messageSegments.add(segment);
        }

        return messageSegments;
    }

    public void iterateCharacters(int start, int end, Consumer<MessageChar> character)
    {
        if (start == end)
            return;

        int totalLen = 0;
        int lineIndex = -1;
        int indexWithinLine = -1;
        for (int i = 0; i < parts.size(); i++)
        {
            TextPart line = parts.get(i);
            if (start < line.text().length() + totalLen)
            {
                lineIndex = i;
                indexWithinLine = start - totalLen;
                break;
            } else
            {
                totalLen += line.text().length();
            }
        }

        if (lineIndex == -1)
            throw new RuntimeException("Out of bounds!");

        for (int i = 0; i < end - start; i++)
        {
            TextPart textLine = parts.get(lineIndex);
            float glyphSize = textLine.size() == TextPart.MESSAGE_SIZE ? textSize : textLine.size();

            char c = textLine.text().charAt(indexWithinLine);
            GlyphInfo glyphInfo = textLine.style().style().font().glyphInfo(c);
            character.accept(new MessageChar(glyphInfo, textLine.style(), glyphSize));

            indexWithinLine++;
            if (indexWithinLine >= textLine.text().length())
            {
                indexWithinLine = 0;
                lineIndex++;
            }

            if (lineIndex > parts.size())
                throw new RuntimeException("Out of bounds!");
        }
    }

    public Stream<MessageChar> characterStream(int start, int end)
    {
        if (start == end)
            return Stream.empty();

        int totalLen = 0;
        int lineIndex = -1;
        int indexWithinLine = -1;
        for (int i = 0; i < parts.size(); i++)
        {
            TextPart line = parts.get(i);
            if (start < line.text().length() + totalLen)
            {
                lineIndex = i;
                indexWithinLine = start - totalLen;
                break;
            } else
            {
                totalLen += line.text().length();
            }
        }

        if (lineIndex == -1)
            throw new RuntimeException("Out of bounds!");

        MessageChar[] characters = new MessageChar[end - start];

        for (int i = 0; i < end - start; i++)
        {
            TextPart textLine = parts.get(lineIndex);
            float glyphSize = textLine.size() == TextPart.MESSAGE_SIZE ? textSize : textLine.size();

            char c = textLine.text().charAt(indexWithinLine);
            GlyphInfo glyphInfo = textLine.style().style().font().glyphInfo(c);
            characters[i] = new MessageChar(glyphInfo, textLine.style(), glyphSize);

            indexWithinLine++;
            if (indexWithinLine >= textLine.text().length())
            {
                indexWithinLine = 0;
                lineIndex++;
            }

            if (lineIndex > parts.size())
                throw new RuntimeException("Out of bounds!");
        }

        return Stream.of(characters);
    }

    /*
     * Builder
     */

    public Text copy()
    {
        return new Text(parts(), textSize(), maxWidth(), maxHeight(), anchor(), align(), lineGapOffset(), forceSingleLine(), verticalAnchor());
    }

    public Text withMutableParts()
    {
        return new Text(new ArrayList<>(parts()), textSize(), maxWidth(), maxHeight(), anchor(), align(), lineGapOffset(), forceSingleLine(), verticalAnchor());
    }

    public Text withParts(List<TextPart> parts)
    {
        return new Text(parts, textSize(), maxWidth(), maxHeight(), anchor(), align(), lineGapOffset(), forceSingleLine(), verticalAnchor());
    }

    public Text withTextSize(float textSize)
    {
        return new Text(parts(), textSize, maxWidth(), maxHeight(), anchor(), align(), lineGapOffset(), forceSingleLine(), verticalAnchor());
    }

    public Text withMaxWidth(float maxWidth)
    {
        return new Text(parts(), textSize(), maxWidth, maxHeight(), anchor(), align(), lineGapOffset(), forceSingleLine(), verticalAnchor());
    }

    public Text withMaxHeight(float maxHeight)
    {
        return new Text(parts(), textSize(), maxWidth(), maxHeight, anchor(), align(), lineGapOffset(), forceSingleLine(), verticalAnchor());
    }

    public Text withAnchor(Anchor2D anchor)
    {
        return new Text(parts(), textSize(), maxWidth(), maxHeight(), anchor, align(), lineGapOffset(), forceSingleLine(), verticalAnchor());
    }

    public Text withAlign(Align align)
    {
        return new Text(parts(), textSize(), maxWidth(), maxHeight(), anchor(), align, lineGapOffset(), forceSingleLine(), verticalAnchor());
    }

    public Text withLineGapOffset(float lineGapOffset)
    {
        return new Text(parts(), textSize(), maxWidth(), maxHeight(), anchor(), align(), lineGapOffset, forceSingleLine(), verticalAnchor());
    }

    public Text withForceSingleLine(boolean forceSingleLine)
    {
        return new Text(parts(), textSize(), maxWidth(), maxHeight(), anchor(), align(), lineGapOffset(), forceSingleLine, verticalAnchor());
    }

    public Text withVerticalAnchor(VerticalAnchorMode verticalAnchor)
    {
        return new Text(parts(), textSize(), maxWidth(), maxHeight(), anchor(), align(), lineGapOffset(), forceSingleLine(), verticalAnchor);
    }
}
