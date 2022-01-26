package net.reldo.taskstracker.tasktypes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.swing.SwingUtilities;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.TaskDataClient;
import net.reldo.taskstracker.data.TaskSave;
import net.reldo.taskstracker.data.TrackerDataStore;

public abstract class AbstractTaskManager
{
	protected final TrackerDataStore trackerDataStore;
	private TaskDataClient taskDataClient;
	private final TasksTrackerPlugin plugin;
	public TaskType taskType;
	public ArrayList<Task> tasks = new ArrayList<>();
	public int maxTaskCount;

	public AbstractTaskManager(TaskType taskType, TasksTrackerPlugin plugin, TrackerDataStore trackerDataStore, TaskDataClient taskDataClient)
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
			applyTrackerSave();
		});
	}

	public void applyTrackerSave()
	{
		HashMap<String, TaskSave> loadedTasks = trackerDataStore.currentData.tasksByType.get(taskType);
		if (loadedTasks == null)
		{
			return;
		}
		tasks.forEach(task -> {
			TaskSave taskSave = loadedTasks.get(task.getName());
			if (taskSave == null)
			{
				task.setTrackedOn(0);
				task.setCompletedOn(0);
				task.setIgnoredOn(0);
				return;
			}
			task.setTrackedOn(taskSave.getTrackedOn());
			task.setCompletedOn(taskSave.getCompletedOn());
			task.setIgnoredOn(taskSave.getIgnoredOn());
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

	private void sendTaskUpdateMessage(LinkedHashMap<String, Boolean> taskProgress)
	{
		String taskCount = String.valueOf(taskProgress.size());
		String helpMessage = " (remove filters to get full export)";
		Color messageColor = Color.decode("#940B00");
		if (maxTaskCount > 0)
		{
			taskCount += "/" + maxTaskCount;
			if (maxTaskCount == taskProgress.size())
			{
				messageColor = Color.decode("#007517");
				helpMessage = "";
			}
		}
		plugin.sendChatMessage(taskCount + " tasks stored for export" + helpMessage, messageColor);
	}
}
