package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.HashMap;
import lombok.Data;

/**
 * Configuration specific to DBROW-based task types (bingo / Grid Master / future leagues).
 * Stored in task-types.json under the "dbrowConfig" key.
 */
@Data
public class DbRowConfig
{
	/**
	 * The specific bingo_events DBRow ID for this season.
	 * Used by the scraper to traverse the event hierarchy and compute flat indexes.
	 */
	private int eventDbRowId;

	/**
	 * DBTable IDs for the event/grid/action hierarchy.
	 * Keys: "event", "grid", "action"
	 */
	private HashMap<String, Integer> tableIds;
}
