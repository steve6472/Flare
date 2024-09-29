package steve6472.volkaniums.settings;

import steve6472.core.registry.StringValue;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Volkaniums <br>
 */
public enum ValidationLevel implements StringValue
{
    NONE("None"),
    ERROR("Error"),
    WARNING("Warning"),
    INFO("Info"),
    VERBOSE("Verbose");

    private final String value;

    ValidationLevel(String value)
    {
        this.value = value;
    }

    @Override
    public String stringValue()
    {
        return value;
    }
}
