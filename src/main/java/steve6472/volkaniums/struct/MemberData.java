package steve6472.volkaniums.struct;

import org.lwjgl.vulkan.VK13;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
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

        public Builder<T> memcpy(BiConsumer<ByteBuffer, T> memcpy)
        {
            this.memcpy = (buffer, offset, data) -> memcpy.accept(buffer, data);
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
