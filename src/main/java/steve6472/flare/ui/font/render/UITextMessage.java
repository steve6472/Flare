package steve6472.flare.ui.font.render;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import steve6472.flare.ui.font.layout.GlyphInfo;
import steve6472.flare.ui.font.style.FontStyleEntry;
import steve6472.flare.util.FloatUtil;

import java.text.BreakIterator;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Created by steve6472
 * Date: 11/17/2024
 * Project: Flare <br>
 */
public record UITextMessage(List<UITextLine> lines, float textSize, float maxWidth, Anchor2D anchor, Align align, NewLineType newLineType, float lineGapOffset)
{
    private static final Anchor2D DEFAULT_ANCHOR = Anchor2D.CENTER;
    private static final Align DEFAULT_ALIGN = Align.CENTER;
    private static final NewLineType DEFAULT_NEW_LINE = NewLineType.MAX_HEIGHT;

    public static final Codec<UITextMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UITextLine.CODEC_MESSAGE.listOf().fieldOf("text").forGetter(UITextMessage::lines),
        Codec.FLOAT.optionalFieldOf("text_size", 12f).forGetter(UITextMessage::textSize),  // in pixels
        Codec.FLOAT.optionalFieldOf("max_width", 192f).forGetter(UITextMessage::maxWidth), // in pixels
        Anchor2D.CODEC.optionalFieldOf("anchor", DEFAULT_ANCHOR).forGetter(UITextMessage::anchor),
        Align.CODEC.optionalFieldOf("align", DEFAULT_ALIGN).forGetter(UITextMessage::align),
        NewLineType.CODEC.optionalFieldOf("new_line_type", DEFAULT_NEW_LINE).forGetter(UITextMessage::newLineType),
        Codec.FLOAT.optionalFieldOf("line_gap_offset", 0f).forGetter(UITextMessage::lineGapOffset)
    ).apply(instance, UITextMessage::new));

    public void iterateCharacters(MessageCharIterator info)
    {
        int lineIndex = 0;
        int indexWithinLine = 0;
        for (int i = 0; i < len(); i++)
        {
            UITextLine textLine = lines.get(lineIndex);
            float glyphSize = textLine.size() == UITextLine.MESSAGE_SIZE ? textSize : textLine.size();

            char c = textLine.text().charAt(indexWithinLine);
            MessageChar nextChar = null;
            if (indexWithinLine + 1 < textLine.text().length())
            {
                char nextC = textLine.text().charAt(indexWithinLine + 1);
                FontStyleEntry style = textLine.style();
                nextChar = new MessageChar(style.style().font().glyphInfo(nextC), style, glyphSize);
            } else if (lines.size() < lineIndex + 1 && !lines.get(lineIndex + 1).text().isEmpty())
            {
                UITextLine nextLine = lines.get(lineIndex + 1);
                char nextC = nextLine.text().charAt(0);
                FontStyleEntry style = nextLine.style();
                float nextGlyphSize = nextLine.size() == UITextLine.MESSAGE_SIZE ? textSize : nextLine.size();
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
        return lines.stream().mapToInt(line -> line.text().length()).sum();
    }

    public float maxHeight()
    {
        Optional<Float> max = lines
            .stream()
            .map(line -> line
                .style()
                .style()
                .font()
                .getMaxHeight(line.text(), line.size() == UITextLine.MESSAGE_SIZE ? textSize : line.size()))
            .max(Float::compare);
        return max.orElse(0f);
    }

    public Pair<Character, FontStyleEntry> getChar(int index)
    {
        int totalLen = 0;
        for (UITextLine line : lines)
        {
            if (index < line.text().length() + totalLen)
            {
                char c = line.text().charAt(index - totalLen);
                return Pair.of(c, line.style());
            } else
            {
                totalLen += line.text().length();
            }
        }

        return null;
    }

    // TODO: this is incorrect, it does not take descent into account
    public float getMaxHeight(int start, int end)
    {
        return characterStream(start, end).map(c -> c.glyph().planeBounds().height() * c.size()).max(Float::compare).orElse(0f);
    }

    public float getWidth(int start, int end)
    {
        return characterStream(start, end).collect(FloatUtil.summing(c -> c.glyph().advance() * c.size()));
    }

    public float getMinDescent(int start, int end)
    {
        return characterStream(start, end).filter(c -> !c.glyph().isInvisible()).map(c -> c.glyph().planeBounds().bottom() * c.size()).min(Float::compare).orElse(0f);
    }

    public float getMaxDescent(int start, int end)
    {
        return characterStream(start, end).filter(c -> !c.glyph().isInvisible()).map(c -> c.glyph().planeBounds().bottom() * c.size()).max(Float::compare).orElse(0f);
    }

    public String rawString(int start, int end)
    {
        StringBuilder bob = new StringBuilder();
        iterateCharacters(start, end, character -> bob.append((char) character.glyph().index()));
        return bob.toString();
    }

    public List<UIMessageSegment> createSegments()
    {
        BreakIterator breakIterator = BreakIterator.getLineInstance();
        StringBuilder bobTheBuilder = new StringBuilder();
        lines().forEach(l -> bobTheBuilder.append(l.text()));
        breakIterator.setText(bobTheBuilder.toString());

        List<UIMessageSegment> messageSegments = new ArrayList<>();

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

            if ((totalWidth + trimmedWidth) > maxWidth() && maxWidth() != -1)
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

        breakIndicies.addFirst(0);
        breakIndicies.add(bobTheBuilder.length());

        // Calculate message segment values for rendering
        for (int i = 0; i < breakIndicies.size() - 1; i++)
        {
            UIMessageSegment segment = new UIMessageSegment();
            segment.start = breakIndicies.getInt(i);
            segment.end = breakIndicies.getInt(i + 1);

            String string = rawString(segment.start, segment.end).stripTrailing();
            int strippedEnd = segment.start + string.length();
            segment.width = getWidth(segment.start, strippedEnd);
            segment.height = getMaxHeight(segment.start, strippedEnd);

            segment.minDescent = getMinDescent(segment.start, strippedEnd);
            segment.maxDescent = getMaxDescent(segment.start, strippedEnd);

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
        for (int i = 0; i < lines.size(); i++)
        {
            UITextLine line = lines.get(i);
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
            UITextLine textLine = lines.get(lineIndex);
            float glyphSize = textLine.size() == UITextLine.MESSAGE_SIZE ? textSize : textLine.size();

            char c = textLine.text().charAt(indexWithinLine);
            GlyphInfo glyphInfo = textLine.style().style().font().glyphInfo(c);
            character.accept(new MessageChar(glyphInfo, textLine.style(), glyphSize));

            indexWithinLine++;
            if (indexWithinLine >= textLine.text().length())
            {
                indexWithinLine = 0;
                lineIndex++;
            }

            if (lineIndex > lines.size())
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
        for (int i = 0; i < lines.size(); i++)
        {
            UITextLine line = lines.get(i);
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
            UITextLine textLine = lines.get(lineIndex);
            float glyphSize = textLine.size() == UITextLine.MESSAGE_SIZE ? textSize : textLine.size();

            char c = textLine.text().charAt(indexWithinLine);
            GlyphInfo glyphInfo = textLine.style().style().font().glyphInfo(c);
            characters[i] = new MessageChar(glyphInfo, textLine.style(), glyphSize);

            indexWithinLine++;
            if (indexWithinLine >= textLine.text().length())
            {
                indexWithinLine = 0;
                lineIndex++;
            }

            if (lineIndex > lines.size())
                throw new RuntimeException("Out of bounds!");
        }

        return Stream.of(characters);
    }
}
