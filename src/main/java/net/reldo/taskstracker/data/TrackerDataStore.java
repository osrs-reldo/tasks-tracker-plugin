package net.reldo.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
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
	public static final String CONFIG_TASKS_PREFIX = "tasks";
	public static final String CONFIG_GROUP_PREFIX_SEPARATOR = "-";
	public static final String CONFIG_GROUP_NAME = TasksTrackerPlugin.CONFIG_GROUP_NAME;

	private Gson gson;
	private final ConfigManager configManager;


	@Inject
	public TrackerDataStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson.newBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();
	}

	public HashMap<Integer, Task> loadTaskTypeFromConfig(TaskType taskType)
	{
		log.debug("loadTaskTypeFromConfig {}", taskType.getDisplayString());
		String key = CONFIG_TASKS_PREFIX + CONFIG_GROUP_PREFIX_SEPARATOR + taskType.name();
		String jsonString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, key);
		if (jsonString == null)
		{
			log.debug("No save information for task type {}, returning empty", taskType.getDisplayString());
			return new HashMap<>();
		}

		Type taskClassType = taskType.getClassType();
		Type deserializeType = TypeToken.getParameterized(HashMap.class, Integer.class, taskClassType).getType();
		try
		{
			return this.gson.fromJson(jsonString, deserializeType);
		}
		catch (JsonParseException ex)
		{
			log.error("{} {} json invalid. All is lost", CONFIG_GROUP_NAME, key, ex);
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP_NAME, key);
			return new HashMap<>();
		}
	}

	public void saveTaskTypeToConfig(TaskType taskType, Collection<Task> tasks)
	{
		log.debug("saveTaskTypeToConfig");
		Map<Integer, Task> tasksWithData = tasks.stream()
			.filter(task -> task.getCompletedOn() != 0 || task.getIgnoredOn() != 0 || task.getTrackedOn() != 0)
			.collect(Collectors.<Task, Integer, Task>toMap(Task::getId, task -> task));

		String configValue = this.gson.toJson(tasksWithData);
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_TASKS_PREFIX + CONFIG_GROUP_PREFIX_SEPARATOR + taskType.name(), configValue);
	}
}
