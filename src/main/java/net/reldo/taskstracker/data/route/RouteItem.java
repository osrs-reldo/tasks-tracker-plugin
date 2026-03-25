package net.reldo.taskstracker.data.route;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import java.util.List;
import lombok.Data;

/**
 * A single item in a route section. Can be either a task reference (by taskId)
 * or a custom item (waypoint, reminder, etc.).
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

	/** GrootsLeagueMap: map pin coordinates. Stored opaquely for re-export. */
	@Expose
	private JsonElement pinCoords;

	/** GrootsLeagueMap: per-item notes/comments. Joined and applied as task note on import. */
	@Expose
	private List<String> comments;

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

	public static RouteItem forTask(int taskId)
	{
		RouteItem item = new RouteItem();
		item.setTaskId(taskId);
		return item;
	}

	public static RouteItem forCustom(CustomRouteItem customItem)
	{
		RouteItem item = new RouteItem();
		item.setCustomItem(customItem);
		return item;
	}

	public static RouteItem forCustomType(String type)
	{
		return forCustom(CustomRouteItem.create(type));
	}
}
