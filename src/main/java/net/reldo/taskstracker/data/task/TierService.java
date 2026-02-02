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
	 * Resolve actual points for a tier.
	 * Priority: game varbit → hardcoded JSON → -1 (unknown)
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

	// Get the reward tier achieved for a point total.
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
				break;
			}
		}
		return result;
	}

	// Get the completion tier achieved for a task count.
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

	// Get the next points tier after the current one.
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

	// Get the next completion count tier after the current one.
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
