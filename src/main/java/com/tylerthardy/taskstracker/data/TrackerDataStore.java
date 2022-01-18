package com.tylerthardy.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class TrackerDataStore
{
	private static final String PLUGIN_BASE_GROUP = "tasksTracker";
	private static final String SETTINGS_DATA = "settingsData";
	private static final String TASKS_PREFIX = "tasks";

	private final ConfigManager configManager;

	public TrackerData currentData;

	private int runescapeVersion;
	private String runeliteVersion;

	@Inject
	public TrackerDataStore(ConfigManager configManager)
	{
		this.configManager = configManager;
		this.currentData = new TrackerData();
	}

	public void setVersions(int runescapeVersion, String runeliteVersion)
	{
		this.runescapeVersion = runescapeVersion;
		this.runeliteVersion = runeliteVersion;
	}

	public void saveTask(Task task)
	{
		HashMap<String, TaskSave> typeTasks = currentData.tasksByType.computeIfAbsent(task.getType(), k -> new HashMap<>());
		if (task.isTracked() || task.isCompleted()) {
			TaskSave taskSave = new TaskSave();
			taskSave.setCompletedOn(task.getCompletedOn());
			taskSave.setTrackedOn(task.getTrackedOn());
			typeTasks.put(task.getName(), taskSave);
		} else {
			typeTasks.remove(task.getName());
		}

		saveCurrentToConfig();
	}

	public void loadProfile()
	{
		TrackerData trackerData = new TrackerData();

		trackerData.settings = getDataFromConfig(PLUGIN_BASE_GROUP, SETTINGS_DATA, TrackerSettings.class, new TrackerSettings());

		Type taskDeserializeType = new TypeToken<HashMap<String, TaskSave>>(){}.getType();
		for (TaskType taskType : TaskType.values())
		{
			HashMap<String, TaskSave> taskData = getDataFromConfig(PLUGIN_BASE_GROUP, TASKS_PREFIX + "." + taskType.name(), taskDeserializeType, new HashMap<>());
			trackerData.tasksByType.put(taskType, taskData);
		}

		currentData = trackerData;
	}

	public String exportToJson(TaskType taskType, HashMap<String, Object> additionalData)
	{
		// Uses different serializer
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();

		if (taskType == null)
		{
			return gson.toJson(currentData);
		} else {
			HashMap<String, Object> export = additionalData;
			export.put("runescapeVersion", runescapeVersion);
			export.put("runeliteVersion", runeliteVersion);
			export.put("timestamp", Instant.now().toEpochMilli());
			export.put("tasks", currentData.tasksByType.get(taskType));
			return gson.toJson(export);
		}
	}

	private Gson buildGson()
	{
		return new GsonBuilder()
			.registerTypeAdapter(TaskSave.class, new TaskDeserializer())
			.registerTypeAdapter(TaskSave.class, new TaskSerializer())
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();
	}

	private <T> T getDataFromConfig(String groupName, String key, Type deserializeType, T defaultValue)
	{
		String jsonString = configManager.getRSProfileConfiguration(groupName, key);
		if (jsonString == null)
		{
			return defaultValue;
		}
		try
		{
			Gson gson = buildGson();
			return gson.fromJson(jsonString, deserializeType);
		}
		catch (JsonParseException ex)
		{
			log.error("{} json invalid. All is lost", groupName + "." + key, ex);
			configManager.unsetRSProfileConfiguration(groupName, key);
			return defaultValue;
		}
	}

	private void saveCurrentToConfig()
	{
		Gson gson = buildGson();

		configManager.setRSProfileConfiguration(PLUGIN_BASE_GROUP, SETTINGS_DATA, gson.toJson(currentData.settings));

		for (TaskType taskType : TaskType.values())
		{
			if (!currentData.tasksByType.containsKey(taskType))
			{
				continue;
			}
			String configValue = gson.toJson(currentData.tasksByType.get(taskType));
			configManager.setRSProfileConfiguration(PLUGIN_BASE_GROUP, TASKS_PREFIX + "." + taskType.name(), configValue);
		}
	}
}
