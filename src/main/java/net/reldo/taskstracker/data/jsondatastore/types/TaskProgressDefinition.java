package net.reldo.taskstracker.data.jsondatastore.types;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;

/**
 * Describes a single progress bar to display on a task.
 * The {@link #type} discriminates which fields are used.
 */
@Slf4j
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

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private transient Skill resolvedSkill;
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private transient boolean skillResolved;

	/**
	 * The RuneLite skill named by {@link #skill}, or null if it isn't a valid skill name.
	 * Resolved once so that progress reads, which run every overlay frame, don't re-parse or re-log it.
	 */
	public Skill getResolvedSkill()
	{
		if (!skillResolved)
		{
			try
			{
				resolvedSkill = Skill.valueOf(skill.toUpperCase());
			}
			catch (IllegalArgumentException | NullPointerException ex)
			{
				log.warn("Unknown skill for {} progress: {}", type, skill);
			}
			skillResolved = true;
		}
		return resolvedSkill;
	}
}
