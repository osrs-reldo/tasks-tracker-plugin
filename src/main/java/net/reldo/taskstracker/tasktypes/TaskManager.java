package net.reldo.taskstracker.tasktypes;

import java.util.ArrayList;
import java.util.HashMap;
import net.reldo.taskstracker.data.CallbackCommand;
import net.reldo.taskstracker.data.TaskDataClient;

public class TaskManager
{
	private final TaskDataClient taskDataClient;
	public TaskType taskType;
	public ArrayList<Task> tasks = new ArrayList<>();

	public TaskManager(TaskType taskType, TaskDataClient taskDataClient)
	{
		this.taskType = taskType;
		this.taskDataClient = taskDataClient;
	}

	public void loadTaskSourceData( CallbackCommand<ArrayList<Task>> callback)
	{
		taskDataClient.loadTaskSourceData(taskType, (tasks) -> {
			this.tasks = tasks;
			callback.execute(tasks);
		});
	}

	public void applyTrackerSave(HashMap<Integer, Task> loadedTasks)
	{
		if (loadedTasks == null)
		{
			return;
		}

		tasks.forEach(task -> {
			Task loadedTask = loadedTasks.get(task.getId());
			if (loadedTask == null)
			{
				return;
			}
			task.loadSave(loadedTask);
		});
	}
}
