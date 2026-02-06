package net.reldo.taskstracker.data;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.task.TaskFromStruct;

@Slf4j
public class TasksSummary
{
	public int trackedTasksCount = 0;
	public int trackedTasksPoints = 0;

	public TasksSummary(Collection<TaskFromStruct> tasks)
	{
		tasks.forEach(task -> {
			if (task.isTracked()) {
				trackedTasksCount++;
				int points = task.getPoints();
				log.debug("TasksSummary {} {}", task.getName(), points);
				trackedTasksPoints += points;
			}
		});
	}
}
