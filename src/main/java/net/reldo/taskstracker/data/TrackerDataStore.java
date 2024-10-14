package net.reldo.taskstracker.data;

import com.google.gson.Gson;
import java.util.Collection;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
import net.reldo.taskstracker.data.task.TaskFromStruct;
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

	// TODO: reimplement
	public HashMap<Integer, TaskFromStruct> loadTaskTypeFromConfig(TaskTypeDefinition taskType)
	{
//		log.debug("loadTaskTypeFromConfig {}", taskType.getName());
//		String key = CONFIG_TASKS_PREFIX + CONFIG_GROUP_PREFIX_SEPARATOR + taskType.getTaskJsonName();
//		String jsonString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, key);
//		if (jsonString == null)
//		{
//			log.debug("No save information for task type {}, returning empty", taskType.getName());
//			return new HashMap<>();
//		}
//
//		Type taskClassType = TaskFromStruct
//		Type deserializeType = TypeToken.getParameterized(HashMap.class, Integer.class, taskClassType).getType();
//		try
//		{
//			return this.gson.fromJson(jsonString, deserializeType);
//		}
//		catch (JsonParseException ex)
//		{
//			log.error("{} {} json invalid. All is lost", CONFIG_GROUP_NAME, key, ex);
//			configManager.unsetRSProfileConfiguration(CONFIG_GROUP_NAME, key);
//			return new HashMap<>();
//		}
		return null;
	}

	// TODO: reimplement
	public void saveTaskTypeToConfig(TaskTypeDefinition taskType, Collection<TaskFromStruct> tasks)
	{
//		log.debug("saveTaskTypeToConfig");
//		Map<Integer, TaskFromStruct> tasksWithData = tasks.stream()
//			.filter(task -> task.getCompletedOn() != 0 || task.getIgnoredOn() != 0 || task.getTrackedOn() != 0)
//			.collect(Collectors.<TaskFromStruct, Integer, TaskFromStruct>toMap(TaskFromStruct::getId, task -> task));
//
//		String configValue = this.gson.toJson(tasksWithData);
//		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_TASKS_PREFIX + CONFIG_GROUP_PREFIX_SEPARATOR + taskType.name(), configValue);
	}
}
