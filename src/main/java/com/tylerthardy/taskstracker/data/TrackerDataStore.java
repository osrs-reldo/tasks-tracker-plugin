package com.tylerthardy.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.tylerthardy.taskstracker.tasktypes.Task;
import java.lang.reflect.Type;
import java.util.HashMap;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;

public class TrackerDataStore
{
	private static final String PLUGIN_BASE_GROUP = "tasksTracker";
	private static final String TRACKER_SAVES_KEY = "trackerSaves";

	private final ConfigManager configManager;

	public TrackerSave loaded;
	private final HashMap<String, HashMap<TrackerWorldType, TrackerSave>> data;

	@Inject
	public TrackerDataStore(ConfigManager configManager)
	{
		this.configManager = configManager;
		this.data = loadDataFromConfig();
	}

	public void saveTask(Task task)
	{
		HashMap<String, TaskSave> typeTasks = loaded.tasks.computeIfAbsent(task.getType(), k -> new HashMap<>());
		TaskSave taskSave = new TaskSave();
		taskSave.setCompleted(task.isCompleted());
		taskSave.setTracked(task.isTracked());
		typeTasks.put(task.getName(), taskSave);
		saveDataToConfig();
	}

	public void load(String username, TrackerWorldType worldType)
	{
		HashMap<TrackerWorldType, TrackerSave> playerSave = data.computeIfAbsent(username, k -> new HashMap<>());
		this.loaded = playerSave.computeIfAbsent(worldType, k -> new TrackerSave(username, worldType));
	}

	private void saveLoadedToData()
	{
		String username = loaded.getUsername();

		// Check if there is a current tracker save for existing login and remove it
		if (data.containsKey(username))
		{
			data.get(username).remove(loaded.getWorldType());
		}
		else
		{
			data.put(username, new HashMap<>());
		}

		// Re-save
		data.get(username).put(loaded.getWorldType(), loaded);
	}

	private void saveDataToConfig()
	{
		saveLoadedToData();

		// Set configuration
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(TaskSave.class, new TaskSerializer())
			.create();
		String configValue = gson.toJson(data);
		configManager.setConfiguration(PLUGIN_BASE_GROUP, TRACKER_SAVES_KEY, configValue);
	}

	private HashMap<String, HashMap<TrackerWorldType, TrackerSave>> loadDataFromConfig()
	{
		Type deserializeType = new TypeToken<HashMap<String, HashMap<TrackerWorldType, TrackerSave>>>(){}.getType();
		String jsonString = configManager.getConfiguration(PLUGIN_BASE_GROUP, TrackerDataStore.TRACKER_SAVES_KEY);
		if (jsonString == null)
		{
			// Never set before
			return new HashMap<>();
		}

		Gson gson = new GsonBuilder()
			.registerTypeAdapter(Task.class, new TaskDeserializer())
			.create();
		try
		{
			return gson.fromJson(jsonString, deserializeType);
		}
		catch (JsonParseException ex)
		{
			// log.error("{} json invalid. All is lost", TrackerDataStore.TRACKER_SAVES_KEY, ex);
			configManager.unsetConfiguration(PLUGIN_BASE_GROUP, TrackerDataStore.TRACKER_SAVES_KEY);
			return new HashMap<>();
		}
	}
}
