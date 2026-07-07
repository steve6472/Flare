package steve6472.flare.core;

import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;

/**
 * Created by steve6472
 * Date: 6/13/2026
 * Project: Flare <br>
 *
 */
public class Setup
{
    private final Event<Void> createGroups;
    private final Event<Void> createRegistries;
    private final Event<Void> loadSettings;
    private final Event<Void> bootstrapRegistries;

    private String windowTitle;

    public Setup()
    {
        this.createRegistries = new SimpleEvent<>();
        this.loadSettings = new SimpleEvent<>();
        this.createGroups = new SimpleEvent<>();
        this.bootstrapRegistries = new SimpleEvent<>();
    }

    public void setWindowTitle(String windowTitle)
    {
        this.windowTitle = windowTitle;
    }

    public String getWindowTitle()
    {
        return windowTitle;
    }

    public Event<Void> createGroups()
    {
        return createGroups;
    }

    public Event<Void> createRegistries()
    {
        return createRegistries;
    }

    public Event<Void> loadSettings()
    {
        return loadSettings;
    }

    public Event<Void> bootstrapRegistries()
    {
        return bootstrapRegistries;
    }
}
