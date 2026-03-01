package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A user-defined or imported route for completing tasks in a specific order.
 * Routes are scoped to a task type (e.g., COMBAT, EXPLORATION) and consist
 * of named sections, each containing an ordered list of tasks and custom items.
 *
 * Routes are stored per-taskType and can be selected independently per tab.
 */
@Data
public class CustomRoute
{
	/** Unique name identifying this route (used as lookup key). */
	@Expose
	private String name;

	/** The task type this route applies to (e.g., "COMBAT", "EXPLORATION"). */
	@Expose
	private String taskType;

	/** Optional author name for imported routes. */
	@Expose
	private String author;

	/** Optional description of the route's purpose or strategy. */
	@Expose
	private String description;

	/** Ordered list of sections making up this route. */
	@Expose
	private List<RouteSection> sections;

	/** Returns all task IDs in route order, flattened across all sections. */
	public List<Integer> getFlattenedOrder()
	{
		if (sections == null)
		{
			return List.of();
		}
		return sections.stream()
			.flatMap(s -> s.getTaskIds().stream())
			.collect(Collectors.toList());
	}

	/** Returns all items (tasks and custom) in route order, flattened across all sections. */
	public List<RouteItem> getFlattenedItems()
	{
		if (sections == null)
		{
			return List.of();
		}
		return sections.stream()
			.flatMap(s -> s.getItems().stream())
			.collect(Collectors.toList());
	}

	/** Finds and returns the section containing the given task ID, or null if not found. */
	public RouteSection getSectionForTask(int taskId)
	{
		if (sections == null)
		{
			return null;
		}
		return sections.stream()
			.filter(s -> s.containsTask(taskId))
			.findFirst()
			.orElse(null);
	}

	/** Returns the total number of tasks across all sections. */
	public int getTaskCount()
	{
		return getFlattenedOrder().size();
	}

	/** Returns true if the given task is the first task in its section (useful for rendering section headers). */
	public boolean isFirstTaskInSection(int taskId)
	{
		if (sections == null)
		{
			return false;
		}
		for (RouteSection section : sections)
		{
			List<Integer> ids = section.getTaskIds();
			if (!ids.isEmpty() && ids.get(0) == taskId)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Inserts a custom item before or after the specified task.
	 * Searches all sections to find the task.
	 */
	public CustomRouteItem insertCustomItem(int taskId, String customType, boolean insertAfter)
	{
		if (sections == null)
		{
			return null;
		}
		for (RouteSection section : sections)
		{
			if (section.containsTask(taskId))
			{
				return section.insertCustomItem(taskId, customType, insertAfter);
			}
		}
		return null;
	}

	/** Finds a custom item by its unique ID across all sections. */
	public CustomRouteItem findCustomItem(String customItemId)
	{
		if (sections == null)
		{
			return null;
		}
		for (RouteSection section : sections)
		{
			for (CustomRouteItem ci : section.getCustomItems())
			{
				if (customItemId.equals(ci.getId()))
				{
					return ci;
				}
			}
		}
		return null;
	}

	/** Removes a custom item by ID from whichever section contains it. Returns true if found. */
	public boolean removeCustomItem(String customItemId)
	{
		if (sections == null)
		{
			return false;
		}
		for (RouteSection section : sections)
		{
			if (section.removeCustomItem(customItemId))
			{
				return true;
			}
		}
		return false;
	}
}
