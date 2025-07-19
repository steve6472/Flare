package steve6472.flare.util;

/**
 * Created by steve6472
 * Date: 7/18/2025
 * Project: Flare <br>
 */
public class Obj<T>
{
    public T value;

    public Obj(T value)
    {
        this.value = value;
    }

    public static <T> Obj<T> of(T value)
    {
        return new Obj<>(value);
    }

    public static <T> Obj<T> empty()
    {
        return new Obj<>(null);
    }

    public T get()
    {
        return value;
    }

    public void set(T value)
    {
        this.value = value;
    }
}
