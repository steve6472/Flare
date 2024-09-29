package steve6472.volkaniums.struct;

import org.lwjgl.vulkan.VK13;
import steve6472.core.util.Preconditions;
import steve6472.volkaniums.AlignmentUtils;

import java.lang.reflect.Array;
import java.util.function.Supplier;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 */
public record MemberData<T>(Class<T> clazz, Supplier<T> constructor, int format, Memcpy<T> memcpy)
{
    public static <T> Builder<T> builder(Class<T> clazz)
    {
        return new Builder<>(clazz);
    }

    public MemberData<T[]> makeArray(final int arraySize)
    {
        Preconditions.checkTrue(clazz.isArray(), "Can not make an array type from array!");

        Class<T[]> arrayType = (Class<T[]>) clazz.arrayType();

        Supplier<T[]> construct = () -> {
            T[] array = (T[]) Array.newInstance(clazz, arraySize);
            for (int i = 0; i < arraySize; i++)
            {
                array[i] = constructor.get();
            }
            return array;
        };

        int sizeof = AlignmentUtils.sizeof(clazz);

        Memcpy<T[]> memcpyArr = (buff, offset, obj) -> {
            int arrayOffset = 0;
            for (T t : obj)
            {
                memcpy.accept(buff, offset + arrayOffset, t);
                arrayOffset += sizeof;
            }
        };
        return new MemberData<>(arrayType, construct, format, memcpyArr);
    }

    public static final class Builder<T>
    {
        private final Class<T> clazz;
        private Supplier<T> constructor;
        private int format = VK13.VK_FORMAT_UNDEFINED;
        private Memcpy<T> memcpy;

        private Builder(Class<T> clazz)
        {
            this.clazz = clazz;
        }

        public Builder<T> constructor(Supplier<T> constructor)
        {
            this.constructor = constructor;
            return this;
        }

        public Builder<T> format(int format)
        {
            this.format = format;
            return this;
        }

        public Builder<T> memcpy(Memcpy<T> memcpy)
        {
            this.memcpy = memcpy;
            return this;
        }

        public MemberData<T> build()
        {
            return new MemberData<>(clazz, constructor, format, memcpy);
        }
    }
}
