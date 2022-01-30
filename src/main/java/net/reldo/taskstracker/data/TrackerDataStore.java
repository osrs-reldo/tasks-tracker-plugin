package net.reldo.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
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
}
