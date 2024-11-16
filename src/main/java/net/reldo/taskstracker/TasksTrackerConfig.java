package net.reldo.taskstracker;

import net.reldo.taskstracker.config.ConfigValues;
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
            position = 11,
            keyName = "filterPanelCollapsible",
            name = "Filter Panels Collapsible",
            description = "Shows button that allows filter panels to be hidden."
    )
    default boolean filterPanelCollapsible()
    {
        return true;
    }

    @ConfigItem(
            position = 12,
            keyName = "saveSubFilterState", //@todo generalise this to all sub-filters
            name = "Save Filter State",
            description = "Configures whether the state of area filters should be saved and recalled when switching task type or restarting the plugin."
    )
    default boolean saveSubFilterState()
    {
        return true;
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
		position = 106,
		keyName = "taskTypeName",
		name = "Task Type",
		description = "Configures the task type which is displayed in the panel.",
		hidden = true
	)
	default String taskTypeName()
	{
		return "COMBAT";
	}

	@ConfigItem(
		position = 109,
		keyName = "dropdownFilter",
		name = "Dropdown Filter",
		description = "Configures the dropdown to filter tasks on.",
		hidden = true
	)
	default String dropdownFilter()
	{
		return "";
	}

    @ConfigItem(
            position = 110,
            keyName = "sortCriteria",
            name = "Sort Criteria",
            description = "Configures the criteria to sort tasks on.",
            hidden = true
    )
    default String sortCriteria()
    {
        return "Default";
    }

    @ConfigItem(
            position = 111,
            keyName = "sortDirection",
            name = "Sort Direction",
            description = "Configures the direction to sort tasks.",
            hidden = true
    )
    default ConfigValues.SortDirections sortDirection()
    {
        return ConfigValues.SortDirections.ASCENDING;
    }
}
