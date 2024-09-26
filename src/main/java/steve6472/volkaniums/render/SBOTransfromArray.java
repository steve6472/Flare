package steve6472.volkaniums.render;

import org.joml.Matrix4f;
import steve6472.volkaniums.assets.model.VkModel;
import steve6472.volkaniums.util.Preconditions;

import java.util.*;
import java.util.function.ToIntFunction;

/**
 * Created by steve6472
 * Date: 9/25/2024
 * Project: Volkaniums <br>
 */
public class SBOTransfromArray<T extends VkModel>
{
    public static final int MAX_TRANSFORMS = 32768;

    private final Matrix4f[] transforms = new Matrix4f[MAX_TRANSFORMS];
    private final Area rootArea;
    private final LinkedHashMap<T, Area> areas = new LinkedHashMap<>(16);
    private int totalIndex;

    /// @param rootModel Use error model
    public SBOTransfromArray(T rootModel)
    {
        for (int i = 0; i < transforms.length; i++)
        {
            transforms[i] = new Matrix4f();
        }

        rootArea = new Area(rootModel);
    }

    public void start()
    {
        getAreas().forEach(Area::start);
        totalIndex = 0;
    }

    public Object getTransformsArray()
    {
        return transforms;
    }

    public Collection<Area> getAreas()
    {
        return areas.values();
    }

    public void addArea(T type)
    {
        Preconditions.checkTrue(isMapped(type), "Area already exists");
        Area lastArea = getLastArea(rootArea);
        Area newArea = new Area(type);
        newArea.index = lastArea.index + 1;
        lastArea.rightArea = newArea;
        areas.put(type, newArea);
    }

    public <A> void sort(List<A> objs, ToIntFunction<A> keyExtractor)
    {
        objs.sort(Comparator.comparingInt(keyExtractor));
    }

    private Area getLastArea(Area area)
    {
        if (area.rightArea != null)
            return getLastArea(area.rightArea);
        return area;
    }

    public boolean isMapped(T type)
    {
        return getAreaByType(type) != null;
    }

    public Area getAreaByType(T type)
    {
        return areas.get(type);
    }

    public Area getAreaByIndex(int index)
    {
        Optional<Area> first = getAreas().stream().filter(a -> a.index == index).findFirst();
        return first.orElse(null);
    }

    public class Area
    {
        T modelType;
        Area rightArea;
        int index;
        int toRender;

        private Area(T modelType)
        {
            this.modelType = modelType;
        }

        public void updateTransform(Matrix4f mat)
        {
            transforms[totalIndex].set(mat);
            toRender++;
            totalIndex++;
        }

        private void start()
        {
            toRender = 0;
        }
    }
}
