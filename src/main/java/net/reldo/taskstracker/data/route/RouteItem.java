package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import lombok.Data;

/**
 * A single item in a route section. Can be either a task reference (by taskId)
 * or a custom item (waypoint, reminder, etc.). This polymorphic design allows
 * routes to interleave tasks with helpful markers.
 *
 * Exactly one of taskId or customItem should be non-null.
 */
@Data
public class RouteItem
{
	/** The task ID if this item represents a task, null otherwise. */
	@Expose
	private Integer taskId;

	/** The custom item if this is not a task, null otherwise. */
	@Expose
	private CustomRouteItem customItem;

	/** Returns true if this item represents a task (vs a custom item). */
	public boolean isTask()
	{
		return taskId != null;
	}

	/** Returns a unique identifier string for this item (prefixed with "task:" or "custom:"). */
	public String getItemId()
	{
		if (isTask())
		{
			return "task:" + taskId;
		}
		if (customItem != null)
		{
			return "custom:" + customItem.getId();
		}
		return "invalid";
	}

	/** Factory method to create a RouteItem for a task. */
	public static RouteItem forTask(int taskId)
	{
		RouteItem item = new RouteItem();
		item.setTaskId(taskId);
		return item;
	}

	/** Factory method to create a RouteItem wrapping an existing custom item. */
	public static RouteItem forCustom(CustomRouteItem customItem)
	{
		RouteItem item = new RouteItem();
		item.setCustomItem(customItem);
		return item;
	}

	/** Factory method to create a RouteItem with a new custom item of the given type. */
	public static RouteItem forCustomType(String type)
	{
		return forCustom(CustomRouteItem.create(type));
	}
}
