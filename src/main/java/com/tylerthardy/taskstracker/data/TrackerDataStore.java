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
	private static final String PROFILE_DATA = "profileData";
	private static final String TASKS_PREFIX = "tasks";

	private final ConfigManager configManager;

	public TrackerProfile loaded;

	@Inject
	public TrackerDataStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public void saveTask(Task task)
	{
		HashMap<String, TaskSave> typeTasks = loaded.tasksByType.computeIfAbsent(task.getType(), k -> new HashMap<>());
		if (task.isTracked() || task.isCompleted()) {
			TaskSave taskSave = new TaskSave();
			taskSave.setCompleted(task.isCompleted());
			taskSave.setTracked(task.isTracked());
			typeTasks.put(task.getName(), taskSave);
		} else {
			typeTasks.remove(task.getName());
		}

		saveLoadedToConfig();
	}

	public void loadProfile()
	{
		TrackerProfile trackerProfile = new TrackerProfile();

		Type deserializeType = new TypeToken<HashMap<String, TaskSave>>(){}.getType();
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(TaskSave.class, new TaskDeserializer())
			.create();

		for (TaskType taskType : TaskType.values())
		{
			String jsonString = configManager.getRSProfileConfiguration(PLUGIN_BASE_GROUP, TASKS_PREFIX + "." + taskType.name());
			if (jsonString == null)
			{
				continue;
			}
			try
			{
				trackerProfile.tasksByType.put(taskType, gson.fromJson(jsonString, deserializeType));
			}
			catch (JsonParseException ex)
			{
				log.error("{} json invalid. All is lost", TASKS_PREFIX + "." + taskType.name(), ex);
				configManager.unsetRSProfileConfiguration(PLUGIN_BASE_GROUP, TASKS_PREFIX + "." + taskType.name());
			}
		}

		loaded = trackerProfile;
	}

	public String exportToJson(TaskType taskType)
	{
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();

		if (taskType == null)
		{
			return gson.toJson(loaded);
		} else {
			HashMap<String, Object> export = new HashMap<>();
			export.put("tasks", loaded.tasksByType.get(taskType));
			export.put("timestamp", Instant.now().toEpochMilli());
			return gson.toJson(export);
		}
	}

	private void saveLoadedToConfig()
	{
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(TaskSave.class, new TaskSerializer())
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();

		for (TaskType taskType : TaskType.values())
		{
			if (!loaded.tasksByType.containsKey(taskType))
			{
				continue;
			}
			String configValue = gson.toJson(loaded.tasksByType.get(taskType));
			configManager.setRSProfileConfiguration(PLUGIN_BASE_GROUP, TASKS_PREFIX + "." + taskType.name(), configValue);
		}
	}
}
