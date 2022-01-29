package net.reldo.taskstracker.tasktypes;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.TaskDataClient;
import net.reldo.taskstracker.data.TrackerDataStore;

public class TaskManager
{
	protected final TrackerDataStore trackerDataStore;
	private final TaskDataClient taskDataClient;
	private final TasksTrackerPlugin plugin;
	public TaskType taskType;
	public ArrayList<Task> tasks = new ArrayList<>();

	public TaskManager(TaskType taskType, TasksTrackerPlugin plugin, TrackerDataStore trackerDataStore, TaskDataClient taskDataClient)
	{
		this.taskType = taskType;
		this.plugin = plugin;
		this.trackerDataStore = trackerDataStore;
		this.taskDataClient = taskDataClient;
	}

	public void loadTaskSourceData()
	{
		taskDataClient.loadTaskSourceData(taskType, (tasks) -> {
			this.tasks = tasks;
			applyTrackerSave(trackerDataStore.currentData.tasksByType.get(taskType));
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

	public void redraw()
	{
		SwingUtilities.invokeLater(() -> plugin.pluginPanel.redraw());
	}

	public void refresh(Task task)
	{
		SwingUtilities.invokeLater(() -> plugin.pluginPanel.refresh(task));
	}
}
