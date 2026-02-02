package net.reldo.taskstracker.data.jsondatastore.types;

import lombok.Data;

/**
 * Thresholds based on number of tasks completed
 * Primarily used for League region unlocks based on completed tasks.
 *
 * Resolution priority:
 * 1. If varbitId > 0: try reading from game varbit
 * 2. If game returns 0 or varbitId <= 0: use hardcoded tasks
 * 3. If tasks is -1: display as "Unknown"
 */
@Data
public class TaskCompletionCountTierDefinition
{
	/**
	 * Varbit ID to read threshold from game. Set to -1 if unknown.
	 */
	private int varbitId;

	/**
	 * Display label (e.g., "Starter", "1st Unlock").
	 * If null, use "Unlock {index+1}".
	 */
	private String label;

	/**
	 * Tasks required for this unlock. Set to -1 if unknown.
	 */
	private int tasks;

	public boolean hasVarbit()
	{
		return varbitId > 0;
	}

	public boolean hasKnownTasks()
	{
		return tasks > -1;
	}

	/**
	 * Gets display label, falling back to "Unlock {n}" if not set.
	 */
	public String getDisplayLabel(int index)
	{
		return label != null ? label : "Unlock " + (index + 1);
	}
}
