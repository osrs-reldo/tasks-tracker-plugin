package net.reldo.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.reldo.ReldoImport;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.league3.League3Task;
import net.reldo.taskstracker.tasktypes.league3.League3TaskManager;
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

	public void saveTask(Task task)
	{
		HashMap<String, TaskSave> typeTasks = currentData.tasksByType.computeIfAbsent(task.getType(), k -> new HashMap<>());
		if (task.isTracked() || task.isCompleted() || task.isIgnored())
		{
			TaskSave taskSave = new TaskSave();
			if (task.getType() == TaskType.LEAGUE_3)
			{
				taskSave.setId(((League3Task)task).id);
			}
			taskSave.setCompletedOn(task.getCompletedOn());
			taskSave.setTrackedOn(task.getTrackedOn());
			taskSave.setIgnoredOn(task.getIgnoredOn());
			typeTasks.put(task.getName(), taskSave);
		}
		else
		{
			typeTasks.remove(task.getName());
		}

		saveCurrentToConfig();
	}

	public void importTasks(TaskType taskType, HashMap<String, TaskSave> tasks)
	{
		currentData.tasksByType.put(taskType, tasks);
		saveCurrentToConfig();
	}

	public void importTasksFromReldo(ReldoImport reldoImport, League3TaskManager taskManager)
	{
		// FIXME: This entire method is a hack
		// FIXME: Hardcoded for league 3 only
		TaskType taskType = TaskType.LEAGUE_3;

		// TODO: Remove this extra transform from id to name once we rely on ids only
		HashMap<String, TaskSave> taskSavesByName = new HashMap<>();
		taskManager.tasks.forEach((task) -> {
			League3Task league3Task = (League3Task) task;
			String idString = String.valueOf(league3Task.id);
			if (reldoImport.getTasks().containsKey(idString))
			{
				taskSavesByName.put(task.getName(), reldoImport.getTasks().get(idString).toTaskSave());
			}
		});
		importTasks(taskType, taskSavesByName);
		taskManager.applyTrackerSave();
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
