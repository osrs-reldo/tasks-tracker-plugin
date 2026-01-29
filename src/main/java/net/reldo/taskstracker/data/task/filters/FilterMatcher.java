package net.reldo.taskstracker.data.task.filters;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.config.ConfigValues.CompletedFilterValues;
import net.reldo.taskstracker.config.ConfigValues.IgnoredFilterValues;
import net.reldo.taskstracker.config.ConfigValues.TrackedFilterValues;
import net.reldo.taskstracker.data.jsondatastore.types.FilterType;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.data.task.TaskType;
import net.runelite.client.config.ConfigManager;

/**
 * Centralized filter matching logic for tasks.
 */
@Slf4j
public class FilterMatcher
{
	private final ConfigManager configManager;
	private final TasksTrackerConfig config;
	private final List<Filter> subFilters;

	public FilterMatcher(ConfigManager configManager, TasksTrackerConfig config, TaskType taskType)
	{
		this.configManager = configManager;
		this.config = config;
		this.subFilters = createSubFilters(taskType);
	}

	/**
	 * Builds the list of sub-filters for a task type, from its JSON-defined filter config.
	 * These correspond to the buttons/dropdowns in SubFilterPanel.
	 */
	private List<Filter> createSubFilters(TaskType taskType)
	{
		List<Filter> filters = new ArrayList<>();

		if (taskType == null)
		{
			return filters;
		}

		taskType.getFilters().forEach((filterConfig) -> {
			String paramName = filterConfig.getValueName();
			String configKey = taskType.getFilterConfigPrefix() + filterConfig.getConfigKey();

			if (filterConfig.getFilterType().equals(FilterType.BUTTON_FILTER))
			{
				filters.add(new ParamButtonFilter(configManager, paramName, configKey));
			}
			else if (filterConfig.getFilterType().equals(FilterType.DROPDOWN_FILTER))
			{
				filters.add(new ParamDropdownFilter(configManager, paramName, configKey));
			}
		});

		return filters;
	}

	/**
	 * Checks if a task meets current UI filters.
	 * Show all by default, each filter then restricts. Fast fail out.
     *
	 * @param task The task to check
	 * @param taskTextFilter The current text search filter, already lowercased (can be null)
	 * @return true if the task passes all filters, false otherwise
	 */
	public boolean meetsFilterCriteria(TaskFromStruct task, String taskTextFilter)
	{
		// Text filter - search in name and description
		if (taskTextFilter != null && !taskTextFilter.isEmpty())
		{
			String nameLowercase = task.getName().toLowerCase();
			String descriptionLowercase = task.getDescription().toLowerCase();

			if (!nameLowercase.contains(taskTextFilter) && !descriptionLowercase.contains(taskTextFilter))
			{
				return false;
			}
		}

		// Sub-filters (area, skill, UI tier buttons/dropdowns from SubFilterPanel)
		for (Filter filter : subFilters)
		{
			if (!filter.meetsCriteria(task))
			{
				return false;
			}
		}

		// Completion Status Filter
		if (config.completedFilter().equals(CompletedFilterValues.INCOMPLETE) && task.isCompleted())
		{
			return false;
		}
		if (config.completedFilter().equals(CompletedFilterValues.COMPLETE) && !task.isCompleted())
		{
			return false;
		}

		// Ignored filter
		if (config.ignoredFilter().equals(IgnoredFilterValues.NOT_IGNORED) && task.isIgnored())
		{
			return false;
		}
		if (config.ignoredFilter().equals(IgnoredFilterValues.IGNORED) && !task.isIgnored())
		{
			return false;
		}

		// Tracked filter
		if (config.trackedFilter().equals(TrackedFilterValues.UNTRACKED) && task.isTracked())
		{
			return false;
		}
		if (config.trackedFilter().equals(TrackedFilterValues.TRACKED) && !task.isTracked())
		{
			return false;
		}

		return true;
	}
}
