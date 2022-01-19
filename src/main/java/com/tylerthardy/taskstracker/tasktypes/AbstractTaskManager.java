package com.tylerthardy.taskstracker.tasktypes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.data.TaskSave;
import com.tylerthardy.taskstracker.data.TrackerDataStore;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import javax.swing.SwingUtilities;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;

public abstract class AbstractTaskManager
{
    public TaskType taskType;
    public ArrayList<Task> tasks;
    public int maxTaskCount;

    private final TasksTrackerPlugin plugin;
	protected final TrackerDataStore trackerDataStore;

	public AbstractTaskManager(TaskType taskType, TasksTrackerPlugin plugin, TrackerDataStore trackerDataStore)
    {
        this.taskType = taskType;
        this.plugin = plugin;
		this.trackerDataStore = trackerDataStore;
		try (InputStream dataFile = TasksTrackerPlugin.class.getResourceAsStream(taskType.getDataFileName()))
		{
			assert dataFile != null;
			Type classType = taskType.getClassType();
			Type listType = TypeToken.getParameterized(ArrayList.class, classType).getType();
			tasks = new Gson().fromJson(new InputStreamReader(dataFile, StandardCharsets.UTF_8), listType);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		applyTrackerSave();
    }

	private void applyTrackerSave()
	{
		HashMap<String, TaskSave> loadedTasks = trackerDataStore.currentData.tasksByType.get(taskType);
		if (loadedTasks == null) return;
		tasks.forEach(task -> {
			TaskSave loadedTask = loadedTasks.get(task.getName());
			if (loadedTask == null) return;
			task.setTracked(loadedTask.isTracked());
			task.setCompleted(loadedTask.isCompleted());
			task.setIgnored(loadedTask.isIgnored());
		});
	}

	/**
	 * Method to be run any time a chat message is sent.
	 * All tasks will have a chat messages related to them - specifically completion chat messages.
	 * Hook into the chat messages by implementing this method.
	 * @param chatMessage RuneLite chat message event
	 */
	public abstract void handleChatMessage(ChatMessage chatMessage);

	/**
	 * Method to be run any time a widget is loaded.
	 * Most tasks will have an interface (combat task progress, leagues tab, etc). Data can be scraped from this interface.
	 * Hook into a widget opening by overriding this method.
	 * @param widgetLoaded RuneLite widget loaded event
	 */
    public void handleOnWidgetLoaded(WidgetLoaded widgetLoaded) {}

	/**
	 * Method to be run any time after a script has fired.
	 * Most tasks will have a script that load a list of tasks into an interface. Some lists are not accessible through widgets until this script is complete.
	 * Hook into the completion of a script by overriding this method.
	 * @param scriptPostFired
	 */
	public void handleOnScriptPostFired(ScriptPostFired scriptPostFired) {}

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
        Optional<Task> first = tasks.stream().filter(t -> t.getName().equalsIgnoreCase(taskName)).findFirst();
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
        for (Task task : tasks) {
            if (taskProgress.containsKey(task.getName()))
            {
                task.setCompleted(taskProgress.get(task.getName()));
				trackerDataStore.saveTask(task);
            }
        }

        sendTaskUpdateMessage(taskProgress);
    }

    private void sendTaskUpdateMessage(LinkedHashMap<String, Boolean> taskProgress) {
        String taskCount = String.valueOf(taskProgress.size());
        String helpMessage = " (remove filters to get full export)";
        Color messageColor = Color.decode("#940B00");
        if (maxTaskCount > 0) {
            taskCount += "/" + maxTaskCount;
            if (maxTaskCount == taskProgress.size()) {
                messageColor = Color.decode("#007517");
                helpMessage = "";
            }
        }
        plugin.sendChatMessage(taskCount + " tasks stored for export" + helpMessage, messageColor);
    }
}
