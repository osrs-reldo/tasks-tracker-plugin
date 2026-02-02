package net.reldo.taskstracker.data.task;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.types.TaskCompletionCountTierDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.TaskPointTierDefinition;
import net.runelite.api.Client;

/**
 * Service for resolving tier and unlock thresholds.
 *
 * Resolution priority:
 * 1. Game varbit (if varbitId > 0 and returns > 0)
 * 2. Hardcoded JSON value
 * 3. -1 (unknown)
 */
@Slf4j
@Singleton
public class TierService
{
	@Inject
	private Client client;

	/**
	 * Resolve actual points for a reward tier.
	 * Priority: game varbit → hardcoded JSON → -1 (unknown)
	 *
	 * @param tier The tier definition
	 * @return Resolved points, or -1 if unknown
	 */
	public int resolvePoints(TaskPointTierDefinition tier)
	{
		if (tier == null)
		{
			return -1;
		}

		if (tier.hasVarbit())
		{
			try
			{
				int gameValue = client.getVarbitValue(tier.getVarbitId());
				if (gameValue > 0)
				{
					log.debug("Resolved {} from varbit {}: {}", tier.getLabel(), tier.getVarbitId(), gameValue);
					return gameValue;
				}
			}
			catch (Exception e)
			{
				log.debug("Failed to read varbit {}: {}", tier.getVarbitId(), e.getMessage());
			}
		}

		return tier.getPoints(); // Returns -1 if unknown
	}

	/**
	 * Resolve actual task count for an area unlock.
	 * Priority: game varbit → hardcoded JSON → -1 (unknown)
	 *
	 * @param unlock The unlock definition
	 * @return Resolved task count, or -1 if unknown
	 */
	public int resolveTasks(TaskCompletionCountTierDefinition unlock)
	{
		if (unlock == null)
		{
			return -1;
		}

		if (unlock.hasVarbit())
		{
			try
			{
				int gameValue = client.getVarbitValue(unlock.getVarbitId());
				if (gameValue > 0)
				{
					log.debug("Resolved {} from varbit {}: {}", unlock.getLabel(), unlock.getVarbitId(), gameValue);
					return gameValue;
				}
			}
			catch (Exception e)
			{
				log.debug("Failed to read varbit {}: {}", unlock.getVarbitId(), e.getMessage());
			}
		}

		return unlock.getTasks(); // Returns -1 if unknown
	}

	/**
	 * Get the reward tier achieved for a point total.
	 *
	 * @param tiers List of tier definitions (assumed sorted by points ascending)
	 * @param totalPoints Player's total points
	 * @return The highest tier achieved, or null if none
	 */
	public TaskPointTierDefinition getTierForPoints(List<TaskPointTierDefinition> tiers, int totalPoints)
	{
		TaskPointTierDefinition result = null;
		for (TaskPointTierDefinition tier : tiers)
		{
			int threshold = resolvePoints(tier);
			if (threshold >= 0 && totalPoints >= threshold)
			{
				result = tier;
			}
			else if (threshold >= 0)
			{
				break; // Tiers are ordered, stop when we don't meet threshold
			}
		}
		return result;
	}

	/**
	 * Get the area unlock achieved for a task count.
	 *
	 * @param unlocks List of unlock definitions (assumed sorted by tasks ascending)
	 * @param completedTasks Player's completed task count
	 * @return The highest unlock achieved, or null if none
	 */
	public TaskCompletionCountTierDefinition getUnlockForTasks(List<TaskCompletionCountTierDefinition> unlocks, int completedTasks)
	{
		TaskCompletionCountTierDefinition result = null;
		for (TaskCompletionCountTierDefinition unlock : unlocks)
		{
			int threshold = resolveTasks(unlock);
			if (threshold >= 0 && completedTasks >= threshold)
			{
				result = unlock;
			}
			else if (threshold >= 0)
			{
				break;
			}
		}
		return result;
	}

	/**
	 * Get the next reward tier after the current one.
	 *
	 * @param tiers List of tier definitions
	 * @param currentPoints Player's current points
	 * @return The next tier to achieve, or null if at max
	 */
	public TaskPointTierDefinition getNextTier(List<TaskPointTierDefinition> tiers, int currentPoints)
	{
		for (TaskPointTierDefinition tier : tiers)
		{
			int threshold = resolvePoints(tier);
			if (threshold > currentPoints)
			{
				return tier;
			}
		}
		return null; // At max tier
	}

	/**
	 * Get the next area unlock after the current one.
	 *
	 * @param unlocks List of unlock definitions
	 * @param completedTasks Player's completed task count
	 * @return The next unlock to achieve, or null if at max
	 */
	public TaskCompletionCountTierDefinition getNextUnlock(List<TaskCompletionCountTierDefinition> unlocks, int completedTasks)
	{
		for (TaskCompletionCountTierDefinition unlock : unlocks)
		{
			int threshold = resolveTasks(unlock);
			if (threshold > completedTasks)
			{
				return unlock;
			}
		}
		return null; // At max unlock
	}

	/**
	 * Get points needed to reach next tier.
	 *
	 * @param tiers List of tier definitions
	 * @param currentPoints Player's current points
	 * @return Points needed, or 0 if at max tier
	 */
	public int getPointsToNextTier(List<TaskPointTierDefinition> tiers, int currentPoints)
	{
		TaskPointTierDefinition next = getNextTier(tiers, currentPoints);
		if (next == null)
		{
			return 0;
		}
		int threshold = resolvePoints(next);
		return threshold > 0 ? threshold - currentPoints : 0;
	}

	/**
	 * Get tasks needed to reach next unlock.
	 *
	 * @param unlocks List of unlock definitions
	 * @param completedTasks Player's completed task count
	 * @return Tasks needed, or 0 if at max unlock
	 */
	public int getTasksToNextUnlock(List<TaskCompletionCountTierDefinition> unlocks, int completedTasks)
	{
		TaskCompletionCountTierDefinition next = getNextUnlock(unlocks, completedTasks);
		if (next == null)
		{
			return 0;
		}
		int threshold = resolveTasks(next);
		return threshold > 0 ? threshold - completedTasks : 0;
	}
}
