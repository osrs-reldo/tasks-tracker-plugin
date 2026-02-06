package net.reldo.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.data.task.ConfigTaskSave;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.data.task.TaskType;
import net.runelite.client.config.ConfigManager;

@Singleton
@Slf4j
public class TrackerConfigStore
{
	public static final String CONFIG_TASKS_PREFIX = "tasks";
	public static final String CONFIG_GROUP_PREFIX_SEPARATOR = "-";
	public static final String CONFIG_GROUP_NAME = TasksTrackerPlugin.CONFIG_GROUP_NAME;

	private final Gson customGson;
	@Inject
	private TaskService taskService;
	@Inject
	private ConfigManager configManager;

	@Inject
	public TrackerConfigStore(Gson gson)
	{
		this.customGson = gson.newBuilder().excludeFieldsWithoutExposeAnnotation()
			.registerTypeAdapter(float.class, new LongSerializer()).create();
	}

	public void loadCurrentTaskTypeFromConfig()
	{
		TaskType currentTaskType = taskService.getCurrentTaskType();
		if (currentTaskType == null) {
			log.debug("loadTaskTypeFromConfig type is null, skipping");
			return;
		}
		log.debug("loadTaskTypeFromConfig {}", currentTaskType.getName());
		String configKey = getCurrentTaskTypeConfigKey();
		String configJson = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, configKey);
		if (configJson == null) {
			log.debug("No save information for task type {}, not applying save", currentTaskType.getName());
			return;
		}

		Type deserializeType = TypeToken.getParameterized(HashMap.class, Integer.class, ConfigTaskSave.class).getType();
		try {
			HashMap<Integer, ConfigTaskSave> saveData = customGson.fromJson(configJson, deserializeType);
			taskService.applySave(currentTaskType, saveData);
		}
		catch (JsonParseException ex) {
			log.error("{} {} json invalid. wiping saved data", CONFIG_GROUP_NAME, configKey, ex);
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP_NAME, configKey);
		}
	}

	public void saveCurrentTaskTypeData()
	{
		log.debug("saveTaskTypeToConfig");
		Map<Integer, ConfigTaskSave> saveDataByStructId = taskService.getTasks().stream()
			.filter(task -> task.getCompletedOn() != 0 || task.getIgnoredOn() != 0 || task.getTrackedOn() != 0)
			.collect(Collectors.toMap(TaskFromStruct::getStructId, TaskFromStruct::getSaveData,
				(existing, replacement) -> existing, HashMap::new));

		String configValue = this.customGson.toJson(saveDataByStructId);
		String configKey = CONFIG_TASKS_PREFIX + CONFIG_GROUP_PREFIX_SEPARATOR
			+ taskService.getCurrentTaskType().getTaskJsonName();
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, configKey, configValue);
	}

	private String getCurrentTaskTypeConfigKey()
	{
		return CONFIG_TASKS_PREFIX + CONFIG_GROUP_PREFIX_SEPARATOR + taskService.getCurrentTaskType().getTaskJsonName();
	}
}
