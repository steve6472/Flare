package steve6472.flare.render;

import org.joml.Vector2i;
import steve6472.flare.FlareConstants;
import steve6472.flare.assets.atlas.AnimationAtlas;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.flare.ui.textures.animation.SpriteAnimation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 7/19/2025
 * Project: Flare <br>
 */
public class AnimationTicker
{
    private final List<TickedAnimation> animations = new ArrayList<>();

    public AnimationTicker(AnimationAtlas animationAtlas)
    {
        long now = System.currentTimeMillis();
        animationAtlas.getSprites().forEach((key, spriteEntry) -> {
            // Skip error texture
            if (key.equals(FlareConstants.ERROR_TEXTURE))
                return;
            TickedAnimation anim = new TickedAnimation(spriteEntry);
            anim.start = now;
            anim.end = now + anim.getNextTimeOffset();
            animations.add(anim);
        });
    }

    public void tick(long now)
    {
        for (TickedAnimation animation : animations)
        {
            float progress = animation.calculateProgress(now);
            if (progress > 1.0)
            {
                animation.increaseFrameIndex(now);
            }
        }
    }

    public Struct createSbo(long now)
    {
        Struct[] arr = new Struct[animations.size()];
        for (TickedAnimation animation : animations)
        {
            arr[animation.spriteEntry.index()] = animation.toStruct(now);
        }
        return SBO.ANIMATION_ENTRIES.create((Object) arr);
    }

    private static int countPossibleFrames(SpriteEntry sprite)
    {
        // At this point we can safely assume that all sprites have an animation.. probably
        SpriteAnimation animation = sprite.data().animation().orElseThrow();

        if (animation.frames().isEmpty())
        {
            Vector2i size = sprite.pixelSize();
            return (size.x / animation.width()) * (size.y / animation.height());
        } else
        {
            return animation.frames().size();
        }
    }

    private static class TickedAnimation
    {
        private final SpriteEntry spriteEntry;
        private final SpriteAnimation animation;
        // millisecond resolution
        long start, end;
        int totalFrames;
        int frameIndex;

        public TickedAnimation(SpriteEntry spriteEntry)
        {
            this.spriteEntry = spriteEntry;
            this.animation = spriteEntry.data().animation().orElseThrow();
            totalFrames = countPossibleFrames(spriteEntry);
        }

        float calculateProgress(long now)
        {
            return (float) (now - start) / (end - start);
        }

        public long getNextTimeOffset()
        {
            if (animation.frames().isEmpty())
            {
                return animation.frametime();
            } else
            {
                return animation.frames().get(frameIndex).time().orElse(animation.frametime());
            }
        }

        public void increaseFrameIndex(long now)
        {
            frameIndex = (frameIndex + 1) % totalFrames;

            start = now;
            end = now + getNextTimeOffset();
        }

        int getSpriteIndex(int index)
        {
            if (animation.frames().isEmpty())
            {
                return index;
            } else
            {
                return animation.frames().get(index).index();
            }
        }

        public Struct toStruct(long now)
        {
            return spriteEntry.animationToStruct(calculateProgress(now), getSpriteIndex(frameIndex), getSpriteIndex((frameIndex + 1) % totalFrames));
        }
    }
}
