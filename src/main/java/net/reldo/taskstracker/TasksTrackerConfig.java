package net.reldo.taskstracker;

import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(TasksTrackerPlugin.CONFIG_GROUP_NAME)
public interface TasksTrackerConfig extends Config
{
	@ConfigItem(
		position = 10,
		keyName = "untrackUponCompletion",
		name = "Untrack Tasks Upon Completion",
		description = "Configures whether completed tasks should also automatically untracked when the task is completed."
	)
	default boolean untrackUponCompletion()
	{
		return false;
	}

    @ConfigItem(
            position = 100,
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
            position = 101,
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
            position = 102,
            keyName = "ignoredFilter",
            name = "Ignored Tasks Filter",
            description = "Configures whether ignored tasks should be displayed.",
            hidden = true
    )
    default ConfigValues.IgnoredFilterValues ignoredFilter()
    {
        return ConfigValues.IgnoredFilterValues.NOT_IGNORED;
    }

    @ConfigItem(
            position = 103,
            keyName = "taskListTab",
            name = "Selected Task List Tab",
            description = "Configures the currently selected tab on the task list.",
            hidden = true
    )
    default ConfigValues.TaskListTabs taskListTab()
    {
        return ConfigValues.TaskListTabs.ALL;
    }

    @ConfigItem(
            position = 104,
            keyName = "skillFilter",
            name = "Skills Filter",
            description = "Configures the skills to filter tasks on.",
            hidden = true
    )
    default String skillFilter()
    {
        return "";
    }

    @ConfigItem(
            position = 105,
            keyName = "tierFilter",
            name = "Tier Filter",
            description = "Configures the tiers to filter tasks on.",
            hidden = true
    )
    default String tierFilter()
    {
        return "";
    }

	@ConfigItem(
		position = 106,
		keyName = "taskType",
		name = "Task Type",
		description = "Configures the task type which is displayed in the panel.",
		hidden = true
	)
	default TaskType taskType()
	{
		return TaskType.COMBAT;
	}

    @ConfigItem(
            position = 107,
            keyName = "areaFilter",
            name = "Area Filter",
            description = "Configures the areas to filter tasks on.",
            hidden = true
    )
    default String areaFilter()
    {
        return "";
    }

    @ConfigItem(
            position = 108,
            keyName = "categoryFilter",
            name = "Category Filter",
            description = "Configures the category to filter tasks on.",
            hidden = true
    )
    default String categoryFilter()
    {
        return "";
    }
}
