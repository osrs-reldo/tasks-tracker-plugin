package net.reldo.taskstracker.data;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.data.task.filters.FilterMatcher;
import net.reldo.taskstracker.data.task.filters.TextMatcher;

@Slf4j
public class TasksSummary
{
	// Tracked totals
	public int trackedTasksCount = 0;
	public int trackedTasksPoints = 0;

	// Filtered totals (tasks matching users current filter criteria)
	public int filteredTasksCount = 0;
	public int filteredTasksPoints = 0;

	// Tracked progress breakdown
	public int trackedCompletedCount = 0;
	public int trackedCompletedPoints = 0;
	public int trackedIncompleteCount = 0;
	public int trackedIncompletePoints = 0;

	// Total completed (all tasks, not just tracked) - for tier calculation
	public int totalCompletedCount = 0;
	public int totalCompletedPoints = 0;

	public TasksSummary(Collection<TaskFromStruct> tasks)
	{
		tasks.forEach(task -> {
			if (task.isTracked())
			{
				trackedTasksCount++;
				int points = task.getPoints();
				log.debug("TasksSummary {} {}", task.getName(), points);
				trackedTasksPoints += points;
			}
		});
	}

	/**
	 * New constructor - calculates filtered totals, tracked progress, and total completed.
	 *
	 * @param tasks         All tasks to process
	 * @param filterMatcher The filter matcher to determine which tasks pass current filters
	 * @param textMatcher   The text matcher for search filtering (can be null)
	 */
	public TasksSummary(Collection<TaskFromStruct> tasks, FilterMatcher filterMatcher, TextMatcher textMatcher)
	{
		tasks.forEach(task -> {
			int points = task.getPoints();

			// Calculate filtered totals
			if (filterMatcher.meetsFilterCriteria(task, textMatcher))
			{
				filteredTasksCount++;
				filteredTasksPoints += points;
			}

			// Calculate total completed
			if (task.isCompleted())
			{
				totalCompletedCount++;
				totalCompletedPoints += points;
			}

			// Calculate tracked totals with completion breakdown
			if (task.isTracked())
			{
				trackedTasksCount++;
				trackedTasksPoints += points;

				if (task.isCompleted())
				{
					trackedCompletedCount++;
					trackedCompletedPoints += points;
				}
				else
				{
					trackedIncompleteCount++;
					trackedIncompletePoints += points;
				}
			}
		});
	}

	public String formatChatMessage(String taskTypeName, boolean untrackUponCompletion)
	{
		StringBuilder message = new StringBuilder("Task Tracker: ");

		// Pt 1 - Filtered tasks count
		if (filteredTasksCount == 0)
		{
			message.append("No tasks match filter");
		}
		else
		{
			String taskWord = filteredTasksCount == 1 ? "task" : "tasks";
			message.append(String.format("%d filtered %s (%s)",
				filteredTasksCount,
				taskWord,
				formatPoints(filteredTasksPoints)));
		}

		message.append(" | ");

		// Pt 2 - Tracked tasks progress
		if (trackedTasksCount == 0)
		{
			message.append("No tracked tasks");
		}
		else if (untrackUponCompletion)
		{
			message.append(String.format("%d tracked tasks left, worth %s",
				trackedIncompleteCount,
				formatPoints(trackedIncompletePoints)));
		}
		else if (trackedCompletedCount == trackedTasksCount)
		{
			message.append("All tracked tasks done");
		}
		else
		{
			message.append(String.format("%d of %d tracked tasks left (%s)",
				trackedIncompleteCount,
				trackedTasksCount,
				formatPoints(trackedIncompletePoints)));
		}

		return message.toString();
	}

	// Pluralization
	private String formatPoints(int points)
	{
		return points == 1 ? "1 pt" : points + " pts";
	}
}
