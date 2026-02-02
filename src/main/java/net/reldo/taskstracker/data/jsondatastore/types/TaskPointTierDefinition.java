package net.reldo.taskstracker.data.jsondatastore.types;

import lombok.Data;

/**
 * Point-based task thresholds.
 * Used for Combat Achievement tiers and Leagues relic tiers.
 *
 * Resolution priority:
 * 1. If varbitId > 0: try reading from game varbit
 * 2. If game returns 0 or varbitId <= 0: use hardcoded points
 * 3. If points is -1: display as "Unknown"
 */
@Data
public class TaskPointTierDefinition
{
	/**
	 * Varbit ID to read threshold from game. Set to -1 if unknown.
	 */
	private int varbitId;

	/**
	 * Display label (e.g., "Easy", "Tier 1").
	 * If null, use "Tier {index+1}".
	 */
	private String label;

	/**
	 * Points required for this tier. Set to -1 if unknown.
	 */
	private int points;

	public boolean hasVarbit()
	{
		return varbitId > 0;
	}

	public boolean hasKnownPoints()
	{
		return points > -1;
	}

	/**
	 * Gets display label, falling back to "Tier {n}" if not set.
	 */
	public String getDisplayLabel(int index)
	{
		return label != null ? label : "Tier " + (index + 1);
	}
}
