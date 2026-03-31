package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

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
	/** Unique ID for this route. */
	@Expose
	@NonNull
	private String id;

	/** Unique name identifying this route. */
	@Expose
	@NonNull
	private String name;

	/** The task type this route applies to (e.g., "COMBAT", "EXPLORATION"). */
	@Expose
	private String taskType;

	@Expose
	private String author;

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

	public int getTaskCount()
	{
		return getFlattenedOrder().size();
	}

	public int getItemCount()
	{
		return getFlattenedItems().size();
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
	public CustomRouteItem insertCustomItem(int taskId, CustomRouteItem customItem, boolean insertAfter)
	{
		if (sections == null)
		{
			return null;
		}
		for (RouteSection section : sections)
		{
			CustomRouteItem result = section.insertCustomItem(taskId, customItem, insertAfter);
			if (result != null)
			{
				return result;
			}
		}
		return null;
	}

	/** Returns all custom item IDs across all sections (for duplicate detection). */
	public List<String> getAllCustomItemIds()
	{
		if (sections == null)
		{
			return List.of();
		}
		List<String> ids = new ArrayList<>();
		for (RouteSection section : sections)
		{
			for (CustomRouteItem ci : section.getCustomItems())
			{
				ids.add(ci.getId());
			}
		}
		return ids;
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
