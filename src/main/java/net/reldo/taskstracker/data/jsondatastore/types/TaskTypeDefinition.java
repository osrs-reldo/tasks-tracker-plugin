package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;

/**
 * Represents a task type with relevant configuration for UI display and task management
 */
@Data
public class TaskTypeDefinition
{

	/**
	 * Name of the task type for UI display
	 */
	private String name;

	/**
	 * Description of the task type
	 */
	private String description;

	/**
	 * Is the task type enabled?
	 */
	private boolean isEnabled;

	/**
	 * Filename for the task JSON found in the tasks directory of task-json-store
	 * Extension not included.
	 */
	private String taskJsonName;

	/**
	 * Filters for the task type
	 */
	private ArrayList<FilterConfig> filters;

	/**
	 * A dictionary of parameters relevant to the task, with required id, name, description, tier
	 * The key is the plain English name for the parameter
	 * The value is an array of OSRS cache Struct ParamIDs that match with the plain English parameter
	 * Generally, there is only 1 value in the array, but multiple are available for fallback
	 */
	private HashMap<String, Integer> intParamMap;

	/**
	 * A dictionary of parameters relevant to the task, with required id, name, description, tier
	 * The key is the plain English name for the parameter
	 * The value is an array of OSRS cache Struct ParamIDs that match with the plain English parameter
	 * Generally, there is only 1 value in the array, but multiple are available for fallback
	 */
	private HashMap<String, Integer> stringParamMap;

	/**
	 * A dictionary of integer enums relevant to the task type
	 * The key is the plain English name describing the enum
	 * The value is an integer representing the enum id
	 * e.g. "tierSprites": 3213 (tier id maps to a sprite id)
	 */
	private HashMap<String, Integer> intEnumMap;

	/**
	 * A dictionary of string enums relevant to the task type
	 * The key is the plain English name describing the enum
	 * The value is an integer representing the enum id
	 * e.g. "tierNames": 4757 (tier id maps to a sprite id)
	 */
	private HashMap<String, Integer> stringEnumMap;

	/**
	 * A dictionary of tier sprite ids
	 * The key is a string representation of the tier id integer
	 * The value is an integer representing the sprite id
	 */
	private HashMap<String, Integer> tierSpriteIdMap = new HashMap<>();

	/**
	 * Varps used to store task progress
	 * Used for exports from the plugin
	 */
	private ArrayList<Integer> taskVarps;

	/**
	 * Other varps used for the task type
	 * Used for exports from the plugin
	 * Examples in the past: League Points, Sage Renown
	 */
	private int[] otherVarps;

	/**
	 * Varbits used for the task type
	 * Used for exports from the plugin
	 * Examples in the past: Relics chosen, Tasks completed, unlocks, Fragment xp
	 */
	private int[] varbits;

	/**
	 * The script id used to parse the completion of a task
	 * This is a rs2asm script
	 * Example: Combat achievements = script 4834
	 */
	private int taskCompletedScriptId;
}
