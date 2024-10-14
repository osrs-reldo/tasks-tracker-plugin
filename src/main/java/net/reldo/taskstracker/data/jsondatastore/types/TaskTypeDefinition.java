package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
	private Map<String, Integer> stringParamMap;

	/**
	 * TODO
	 */
	private HashMap<String, Integer> intEnumMap;

	/**
	 * TODO
	 */
	private HashMap<String, Integer> stringEnumMap;

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
}
