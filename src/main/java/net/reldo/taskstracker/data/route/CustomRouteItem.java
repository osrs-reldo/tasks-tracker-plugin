package net.reldo.taskstracker.data.route;

import com.google.gson.annotations.Expose;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Represents a non-task item in a route, such as a bank stop, teleport, or custom step.
 * Generic data model: label + icon + name, no hardcoded types.
 * Factory presets provide convenience for common items.
 */
@Data
@NoArgsConstructor
public class CustomRouteItem
{
	/** Sprite archive ID for bank map icon. */
	public static final int BANK_ICON_SPRITE = 1453;

	/** Sprite archive ID for transportation map icon. */
	public static final int TELEPORT_ICON_SPRITE = 1504;

	/** Unique identifier for this item instance (8-char UUID prefix). */
	@Expose
	@NonNull
	private String id;

	/** Display category (e.g., "Bank", "Teleport", or any text). */
	@Expose
	@NonNull
	private String label;

	/** Sprite archive ID for the icon. Null means generate text fallback from label. */
	@Expose
	private Integer icon;

	/** Description text shown below the label. */
	@Expose
	private String description;

	/** Returns the label for display, falling back to "Custom" if empty. */
	public String getDisplayLabel()
	{
		if (label == null || label.isEmpty())
		{
			return "Custom";
		}
		return label;
	}

	private static String generateId()
	{
		return UUID.randomUUID().toString().substring(0, 8);
	}

	public static CustomRouteItem createBank(String description)
	{
		CustomRouteItem item = new CustomRouteItem();
		item.setId(generateId());
		item.setLabel("Bank");
		item.setIcon(BANK_ICON_SPRITE);
		item.setDescription(description);
		return item;
	}

	public static CustomRouteItem createTeleport(String description)
	{
		CustomRouteItem item = new CustomRouteItem();
		item.setId(generateId());
		item.setLabel("Teleport");
		item.setIcon(TELEPORT_ICON_SPRITE);
		item.setDescription(description);
		return item;
	}

	public static CustomRouteItem createCustom(String label, String description)
	{
		CustomRouteItem item = new CustomRouteItem();
		item.setId(generateId());
		item.setLabel(label);
		item.setIcon(null);
		item.setDescription(description);
		return item;
	}
}
