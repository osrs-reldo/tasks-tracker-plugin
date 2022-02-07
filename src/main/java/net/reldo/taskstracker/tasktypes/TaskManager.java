package net.reldo.taskstracker.tasktypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import net.reldo.taskstracker.data.CallbackCommand;
import net.reldo.taskstracker.data.TaskDataClient;

public class TaskManager
{
	private final TaskDataClient taskDataClient;
	public TaskType taskType;
	public HashMap<Integer, Task> tasks = new HashMap<>();

	public TaskManager(TaskType taskType, TaskDataClient taskDataClient)
	{
		this.taskType = taskType;
		this.taskDataClient = taskDataClient;
	}

	public void asyncLoadTaskSourceData(CallbackCommand<ArrayList<Task>> callback)
	{
		taskDataClient.loadTaskSourceData(taskType, (tasks) -> {
			this.tasks = tasks.stream().collect(Collectors.toMap(Task::getId, v -> v, (prev, next) -> next, HashMap::new));

			callback.execute(tasks);
		});
	}

	public void applyTrackerSave(HashMap<Integer, Task> loadedTasks)
	{
		if (loadedTasks == null)
		{
			return;
		}

		tasks.values().forEach(task -> {
			Task loadedTask = loadedTasks.get(task.getId());
			if (loadedTask == null)
			{
				return;
			}
			task.loadSave(loadedTask);
		});
	}
}
