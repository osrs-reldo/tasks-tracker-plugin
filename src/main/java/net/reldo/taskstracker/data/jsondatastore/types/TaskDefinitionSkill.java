package net.reldo.taskstracker.data.jsondatastore.types;

import lombok.Data;

/**
 * Represents a skill required for a task.
 */
@Data
public class TaskDefinitionSkill
{
	/**
	 * The skill
	 */
	private String skill;

	/**
	 * The level required
	 */
	private int level;
}
