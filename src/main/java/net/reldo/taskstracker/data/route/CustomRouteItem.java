package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import lombok.Data;
import java.util.UUID;

/**
 * Represents a non-task item in a route, such as a bank stop, teleport, or other waypoint.
 * Custom items allow route authors to include helpful reminders between tasks.
 * Each item has a unique ID (for tracking completion) and a type (for display/behavior).
 */
@Data
public class CustomRouteItem
{
	/** Unique identifier for this item instance (8-char UUID prefix). */
	@Expose
	private String id;

	/** The type of custom item (e.g., "bank", "home_teleport", "fairy_ring"). */
	@Expose
	private String type;

	/** Returns a human-readable display name for this item type. */
	public String getDisplayName()
	{
		if (type == null || type.isEmpty())
		{
			return "Unknown";
		}
		switch (type)
		{
			case "bank":
				return "Bank";
			case "home_teleport":
				return "Home Teleport";
			case "fairy_ring":
				return "Fairy Ring";
			default:
				return type.substring(0, 1).toUpperCase()
					+ type.substring(1).replace("_", " ");
		}
	}

	/** Factory method to create a new custom item with a generated unique ID. */
	public static CustomRouteItem create(String type)
	{
		CustomRouteItem item = new CustomRouteItem();
		item.setId(UUID.randomUUID().toString().substring(0, 8));
		item.setType(type);
		return item;
	}
}
