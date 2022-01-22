package net.reldo.taskstracker;

import net.reldo.taskstracker.config.ConfigValues;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tasks-tracker")
public interface TasksTrackerConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "completedFilter",
            name = "Completed Tasks Filter",
            description = "Configures whether completed tasks should be displayed.",
            hidden = true
    )
    default ConfigValues.CompletedFilterValues completedFilter()
    {
        return ConfigValues.CompletedFilterValues.COMPLETE_AND_INCOMPLETE;
    }

    @ConfigItem(
            position = 1,
            keyName = "trackedFilter",
            name = "Tracked Tasks Filter",
            description = "Configures whether tracked tasks should be displayed.",
            hidden = true
    )
    default ConfigValues.TrackedFilterValues trackedFilter()
    {
        return ConfigValues.TrackedFilterValues.TRACKED_AND_UNTRACKED;
    }

    @ConfigItem(
            position = 2,
            keyName = "ignoredFilter",
            name = "Ignored Tasks Filter",
            description = "Configures whether ignored tasks should be displayed.",
            hidden = true
    )
    default ConfigValues.IgnoredFilterValues ignoredFilter()
    {
        return ConfigValues.IgnoredFilterValues.NOT_IGNORED;
    }

}
