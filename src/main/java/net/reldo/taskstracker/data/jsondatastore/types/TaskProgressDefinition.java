package net.reldo.taskstracker.data.jsondatastore.types;

import lombok.Data;

/**
 * Describes a single progress bar to display on a task.
 * The {@link #type} discriminates which fields are used.
 */
@Data
public class TaskProgressDefinition
{
	/**
	 * The type of progress being tracked.
	 */
	private ProgressType type;

	/**
	 * For EXPERIENCE and LEVEL types: the skill name, matching the RuneLite Skill enum
	 * (e.g. "OVERALL", "HERBLORE", "SLAYER").
	 */
	private String skill;

	/**
	 * For VARP and VARBIT types: the VarPlayer or Varbit ID to read from the client.
	 */
	private Integer id;

	/**
	 * The value at which this progress bar is considered complete.
	 */
	private int target;
}
