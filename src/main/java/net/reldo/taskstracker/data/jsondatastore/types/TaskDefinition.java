package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Represents a task with various attributes
 */
@Data
public class TaskDefinition
{
	/**
	 * Struct id for task data
	 */
	private Integer structId;

	/**
	 * Sort id based on the sort order in the game's UI
	 */
	private Integer sortId;

	/**
	 * Skills required for the task.
	 */
	private List<TaskDefinitionSkill> skills;

	/**
	 * Metadata related to the task that isn't represented in the Struct/params
	 * May or may not be used for task filters
	 * Examples:
	 * - notes = extra description like "a magic cabbage is a cabbage picked at Draynor Manor"
	 * - category = an extra category type that isn't a param
	 */
	private Map<String, Object> metadata;
}
