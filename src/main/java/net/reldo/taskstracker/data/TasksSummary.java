package net.reldo.taskstracker.data;

import java.util.Collection;
import net.reldo.taskstracker.data.task.TaskFromStruct;

public class TasksSummary
{
	public int trackedTasksCount = 0;
	public int trackedTasksPoints = 0;

	public TasksSummary(Collection<TaskFromStruct> tasks)
	{
		tasks.forEach(task -> {
			if (task.isTracked()) {
				trackedTasksCount++;
				System.out.printf("%s %s%n", task.getName(), task.getPoints());
				trackedTasksPoints += task.getPoints();
			}
		});
	}
}
