package steve6472.flare.tracy;

import io.github.benjaminamos.tracy.Tracy;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Optional;
import java.util.Set;

/**
 * Created by steve6472
 * Date: 11/5/2025
 * Project: Orbiter <br>
 */
public class TracyProfiler implements Profiler
{
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 5);

    private final String name;
    Stack<Tracy.ZoneContext> zones = new ObjectArrayList<>(8);

    public TracyProfiler(String name)
    {
        this.name = name;
    }

    @Override
    public void start()
    {
        // noop I guess ?
        push(name);
    }

    @Override
    public void end()
    {
        pop();
    }

    @Override
    public void push(String name, int color)
    {
        Optional<StackWalker.StackFrame> optional = STACK_WALKER.walk(stream -> stream.filter(frame -> frame.getDeclaringClass() != TracyProfiler.class).findFirst());

        int lineNumber = 0;
        String fileName = "", methodName = "";

        if (optional.isPresent())
        {
            StackWalker.StackFrame frame = optional.get();
            lineNumber = frame.getLineNumber();
            fileName = frame.getFileName();
            methodName = frame.getMethodName();
        }

        long handle = Tracy.allocSourceLocation(lineNumber, fileName, methodName, name, color);
        Tracy.ZoneContext zoneContext = Tracy.zoneBegin(handle, 1);
        zones.push(zoneContext);
    }

    @Override
    public void pop()
    {
        Tracy.ZoneContext pop = zones.pop();
        Tracy.zoneEnd(pop);
    }

    @Override
    public void push(String name)
    {
        push(name, 0);
    }

    @Override
    public void popPush(String name)
    {
        popPush(name, 0);
    }

    @Override
    public void popPush(String name, int color)
    {
        pop();
        push(name, color);
    }
}
