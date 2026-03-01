package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A named section within a route, containing an ordered list of items.
 * Sections allow routes to be organized into logical groups (e.g., by region or theme).
 *
 * Supports two JSON formats for backwards compatibility:
 * - Legacy: "taskIds" array containing only task IDs
 * - Current: "items" array containing RouteItem objects (tasks and custom items)
 *
 * When reading, the "items" format takes precedence if present.
 * When writing, only the "items" format is used (taskIds is cleared).
 */
@Data
public class RouteSection
{
	@Expose
	private String name;

	@Expose
	private String description;

	/** Legacy format: list of task IDs only. Prefer using items instead. */
	@Expose
	private List<Integer> taskIds;

	/** Current format: list of RouteItem (tasks and custom items interleaved). */
	@Expose
	private List<RouteItem> items;

	/**
	 * Returns all items in this section. If using the new "items" format, returns those directly.
	 * If using the legacy "taskIds" format, converts them to RouteItem objects.
	 */
	public List<RouteItem> getItems()
	{
		if (items != null && !items.isEmpty())
		{
			return items;
		}
		if (taskIds != null)
		{
			// Convert legacy taskIds to RouteItem format
			return taskIds.stream()
				.filter(id -> id != null)
				.map(id -> RouteItem.forTask(id))
				.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	/** Sets the items list and clears the legacy taskIds (migrating to new format). */
	public void setItems(List<RouteItem> items)
	{
		this.items = items;
		this.taskIds = null;
	}

	/** Extracts just the task IDs from this section (ignoring custom items). */
	public List<Integer> getTaskIds()
	{
		if (items != null && !items.isEmpty())
		{
			return items.stream()
				.filter(RouteItem::isTask)
				.map(RouteItem::getTaskId)
				.collect(Collectors.toList());
		}
		if (taskIds != null)
		{
			return taskIds.stream()
				.filter(id -> id != null)
				.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	/** Returns only the custom items (non-task items) in this section. */
	public List<CustomRouteItem> getCustomItems()
	{
		return getItems().stream()
			.filter(item -> !item.isTask())
			.map(RouteItem::getCustomItem)
			.collect(Collectors.toList());
	}

	/** Returns true if this section contains the given task ID. */
	public boolean containsTask(int taskId)
	{
		return getTaskIds().contains(taskId);
	}

	/** Returns the number of tasks (not including custom items) in this section. */
	public int getTaskCount()
	{
		return getTaskIds().size();
	}

	/**
	 * Inserts a new custom item before or after the specified task.
	 * Automatically migrates to the new "items" format if needed.
	 *
	 * @param taskId the task to insert relative to
	 * @param customType the type of custom item to create
	 * @param insertAfter true to insert after the task, false to insert before
	 * @return the created CustomRouteItem, or null if the task was not found
	 */
	public CustomRouteItem insertCustomItem(int taskId, String customType, boolean insertAfter)
	{
		List<RouteItem> currentItems = new ArrayList<>(getItems());
		int position = -1;

		for (int i = 0; i < currentItems.size(); i++)
		{
			RouteItem item = currentItems.get(i);
			if (item.isTask() && item.getTaskId() == taskId)
			{
				position = i;
				break;
			}
		}

		if (position == -1)
		{
			return null;
		}

		CustomRouteItem customItem = CustomRouteItem.create(customType);
		int insertPos = insertAfter ? position + 1 : position;
		currentItems.add(insertPos, RouteItem.forCustom(customItem));

		this.items = currentItems;
		this.taskIds = null;

		return customItem;
	}

	/** Removes a custom item by its unique ID. Returns true if found and removed. */
	public boolean removeCustomItem(String customItemId)
	{
		if (items == null)
		{
			return false;
		}
		return items.removeIf(item ->
			!item.isTask()
			&& item.getCustomItem() != null
			&& customItemId.equals(item.getCustomItem().getId()));
	}
}
