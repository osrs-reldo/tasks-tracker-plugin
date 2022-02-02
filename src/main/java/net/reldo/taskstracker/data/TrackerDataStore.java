package net.reldo.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.combattask.CombatTask;
import net.reldo.taskstracker.tasktypes.league3.League3Task;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class TrackerDataStore
{
	public static final String TASKS_PREFIX = "tasks";

	private final ConfigManager configManager;


	@Inject
	public TrackerDataStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	private Gson buildGson()
	{
		return new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();
	}

	public <T> T getDataFromConfig(String key, Type deserializeType, T defaultValue)
	{
		String jsonString = configManager.getRSProfileConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, key);
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
			log.error("{} json invalid. All is lost", TasksTrackerPlugin.CONFIG_GROUP_NAME + "." + key, ex);
			configManager.unsetRSProfileConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, key);
			return defaultValue;
		}
	}

	public void saveTaskTypeToConfig(TaskType taskType, ArrayList<Task> tasks)
	{
		Gson gson = buildGson();
		Map<Integer, Task> tasksWithData = tasks.stream()
			.filter(task -> task.getCompletedOn() != 0 || task.getIgnoredOn() != 0 || task.getTrackedOn() != 0)
			.collect(Collectors.<Task, Integer, Task>toMap(Task::getId, task -> task));

		String configValue = gson.toJson(tasksWithData);
		configManager.setRSProfileConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, TASKS_PREFIX + "." + taskType.name(), configValue);
	}

	public boolean hasStringKeyTaskData(TaskType taskType)
	{
		String data = configManager.getRSProfileConfiguration("tasksTracker", TrackerDataStore.TASKS_PREFIX + "." + taskType.name());
		return data != null && !data.isEmpty();
	}

	public HashMap<Integer, Task> convertStringKeyDataToIdKeyData(TaskType taskType)
	{
		String key = TrackerDataStore.TASKS_PREFIX + "." + taskType.name();
		String jsonString = configManager.getRSProfileConfiguration("tasksTracker", key);
		if (jsonString == null)
		{
			return new HashMap<>();
		}

		HashMap<String, String> stringKeyTaskData;
		HashMap<Integer, Task> idKeyTaskData = new HashMap<>();
		// Note: value type of "String" used to be TaskSave, with a deserializer to parse the string. This now does it manually because that class was deprecated.
		// There is no need to restore the class because this method will be removed when Leagues III finishes.
		Type taskDeserializeType = new TypeToken<HashMap<String, String>>(){}.getType();

		try
		{
			Gson gson = buildGson();
			stringKeyTaskData = gson.fromJson(jsonString, taskDeserializeType);
		}
		catch (JsonParseException ex)
		{
			log.error("{} json invalid when when converting string key data to id key data. Abandoning data, but dumping here to preserve.\n{}", "tasksTracker." + key, jsonString, ex);
			configManager.unsetRSProfileConfiguration("tasksTracker", key);
			return new HashMap<>();
		}

		try
		{
			for (String taskData : stringKeyTaskData.values())
			{
				String[] attributes = taskData.split("\\|");
				long completedOn = Long.parseLong(attributes[0]);
				long trackedOn = Long.parseLong(attributes[1]);
				long ignoredOn = Long.parseLong(attributes[2]);
				int id = Integer.parseInt(attributes[3]);

				Task task;
				if (taskType == TaskType.LEAGUE_3)
				{
					task = new League3Task(id, null, null, null);
				}
				else
				{
					task = new CombatTask(id, null, null, null);
				}
				task.setCompletedOn(completedOn);
				task.setTrackedOn(trackedOn);
				task.setIgnoredOn(ignoredOn);

				idKeyTaskData.put(id, task);
			}
		}
		catch (NumberFormatException ex)
		{
			log.error("{} numbers invalid when when converting string key data to id key data. Abandoning data, but dumping here to preserve.\n{}", "tasksTracker." + key, jsonString, ex);
			configManager.unsetRSProfileConfiguration("tasksTracker", key);
			return new HashMap<>();
		}

		return idKeyTaskData;
	}
}
