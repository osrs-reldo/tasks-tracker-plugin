package net.reldo.taskstracker.tasktypes;

import java.util.Collection;

public class TasksSummary
{
	public int trackedTasksCount = 0;
	public int trackedTasksPoints = 0;

	public TasksSummary(Collection<Task> values)
	{
		values.forEach(task -> {
			if (task.isTracked()) {
				trackedTasksCount++;
				System.out.printf("%s %s%n", task.getName(), task.getPoints());
				trackedTasksPoints += task.getPoints();
			}
		});
	}
}
