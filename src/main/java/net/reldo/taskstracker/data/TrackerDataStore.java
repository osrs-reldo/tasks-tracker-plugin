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
import net.reldo.taskstracker.data.reldo.ReldoImport;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskManager;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class TrackerDataStore
{
	private static final String PLUGIN_BASE_GROUP = "tasksTracker";
	private static final String SETTINGS_DATA = "settingsData";
	private static final String TASKS_PREFIX = "tasks";

	private final ConfigManager configManager;

	public TrackerData currentData;

	@Inject
	public TrackerDataStore(ConfigManager configManager)
	{
		this.configManager = configManager;
		this.currentData = new TrackerData();
	}

	public void importTasksFromReldo(ReldoImport reldoImport, TaskManager taskManager)
	{
		reldoImport.getTasks().forEach((id, reldoTaskSave) -> {
			Task task = taskManager.tasks.get(id);
			task.loadReldoSave(reldoTaskSave);
		});
	}

	public void loadProfile()
	{
		TrackerData trackerData = new TrackerData();

		for (TaskType taskType : TaskType.values())
		{
			Type classType = taskType.getClassType();
			Type taskDeserializeType = TypeToken.getParameterized(HashMap.class, Integer.class, classType).getType();
			HashMap<Integer, Task> taskData = getDataFromConfig(TASKS_PREFIX + "." + taskType.name(), taskDeserializeType, new HashMap<>());
			trackerData.tasksByType.put(taskType, taskData);
		}

		currentData = trackerData;
	}

	private Gson buildGson()
	{
		return new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();
	}

	private <T> T getDataFromConfig(String key, Type deserializeType, T defaultValue)
	{
		String jsonString = configManager.getRSProfileConfiguration(TrackerDataStore.PLUGIN_BASE_GROUP, key);
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
			log.error("{} json invalid. All is lost", TrackerDataStore.PLUGIN_BASE_GROUP + "." + key, ex);
			configManager.unsetRSProfileConfiguration(TrackerDataStore.PLUGIN_BASE_GROUP, key);
			return defaultValue;
		}
	}

	public void saveCurrentToConfig(ArrayList<Task> tasks)
	{
		Gson gson = buildGson();

		for (TaskType taskType : TaskType.values())
		{
			if (!currentData.tasksByType.containsKey(taskType))
			{
				continue;
			}

			Map<Integer, Task> tasksWithData = tasks.stream()
				.filter(task -> task.getCompletedOn() != 0 || task.getIgnoredOn() != 0 || task.getTrackedOn() != 0)
				.collect(Collectors.<Task, Integer, Task>toMap(Task::getId, task -> task));

			String configValue = gson.toJson(tasksWithData);
			configManager.setRSProfileConfiguration(PLUGIN_BASE_GROUP, TASKS_PREFIX + "." + taskType.name(), configValue);
		}
	}
}
