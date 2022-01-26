package net.reldo.taskstracker.tasktypes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
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

	public HashMap<Integer, Integer> getVarbits()
	{
		return new HashMap<>();
	}

	public HashMap<Integer, Integer> getVarps()
	{
		return new HashMap<>();
	}

	public void redraw()
	{
		SwingUtilities.invokeLater(() -> plugin.pluginPanel.redraw());
	}

	public void refresh(Task task)
	{
		SwingUtilities.invokeLater(() -> plugin.pluginPanel.refresh(task));
	}

	public void completeTask(String taskName)
	{
		String processedTaskName = taskName.trim();
		Optional<Task> first = tasks.stream().filter(t -> t.getName().equalsIgnoreCase(processedTaskName)).findFirst();
		first.ifPresent(task -> {
			task.setTracked(false);
			task.setCompleted(true);
			if (plugin.selectedTaskType == taskType)
			{
				refresh(task);
			}
			trackerDataStore.saveTask(task);
		});
	}

	public void updateTaskProgress(LinkedHashMap<String, Boolean> taskProgress)
	{
		// TODO: Hacky, come up with more performant solution & consider case sensitivity
		for (Task task : tasks)
		{
			if (taskProgress.containsKey(task.getName()))
			{
				task.setCompleted(taskProgress.get(task.getName()));
				trackerDataStore.saveTask(task);
			}
		}

		sendTaskUpdateMessage(taskProgress);
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
