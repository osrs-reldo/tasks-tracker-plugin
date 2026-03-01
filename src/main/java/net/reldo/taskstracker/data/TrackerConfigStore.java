package net.reldo.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.route.CustomRoute;
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
		this.customGson = gson.newBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();
	}

	public void loadCurrentTaskTypeFromConfig()
	{
		TaskType currentTaskType = taskService.getCurrentTaskType();
		if (currentTaskType == null)
		{
			log.debug("loadTaskTypeFromConfig type is null, skipping");
			return;
		}
		log.debug("loadTaskTypeFromConfig {}", currentTaskType.getName());
		String configKey = getCurrentTaskTypeConfigKey();
		String configJson = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, configKey);
		if (configJson == null)
		{
			log.debug("No save information for task type {}, not applying save", currentTaskType.getName());
			return;
		}

		Type deserializeType = TypeToken.getParameterized(HashMap.class, Integer.class, ConfigTaskSave.class).getType();
		try
		{
			HashMap<Integer, ConfigTaskSave> saveData = customGson.fromJson(configJson, deserializeType);
			taskService.applySave(currentTaskType, saveData);
		}
		catch (JsonParseException ex)
		{
			log.error("{} {} json invalid. wiping saved data", CONFIG_GROUP_NAME, configKey, ex);
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP_NAME, configKey);
		}
	}

	public void saveCurrentTaskTypeData()
	{
		log.debug("saveTaskTypeToConfig");
		Map<Integer, ConfigTaskSave> saveDataByStructId = taskService.getTasks().stream()
			.filter(task -> task.getCompletedOn() != 0 || task.getIgnoredOn() != 0 || task.getTrackedOn() != 0)
			.collect(Collectors.toMap(
				TaskFromStruct::getStructId,
				TaskFromStruct::getSaveData,
				(existing, replacement) -> existing,
				HashMap::new
			));

		String configValue = this.customGson.toJson(saveDataByStructId);
		String configKey = CONFIG_TASKS_PREFIX + CONFIG_GROUP_PREFIX_SEPARATOR + taskService.getCurrentTaskType().getTaskJsonName();
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, configKey, configValue);
	}

	private String getCurrentTaskTypeConfigKey()
	{
		return CONFIG_TASKS_PREFIX + CONFIG_GROUP_PREFIX_SEPARATOR + taskService.getCurrentTaskType().getTaskJsonName();
	}

	// ========================================================================
	// Route Persistence
	// Routes are stored per-taskType (shared across all tabs).
	// Config key pattern: "routes-{taskType}"
	// ========================================================================

	/** Saves all routes for a given task type, replacing any existing routes. */
	public void saveRoutes(String taskType, List<CustomRoute> routes)
	{
		String key = "routes" + CONFIG_GROUP_PREFIX_SEPARATOR + taskType;
		String json = customGson.toJson(routes);
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, key, json);
	}

	/** Loads all routes for a given task type. Returns empty list if none exist or on parse error. */
	public List<CustomRoute> loadRoutes(String taskType)
	{
		String key = "routes" + CONFIG_GROUP_PREFIX_SEPARATOR + taskType;
		String configJson = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, key);
		if (configJson == null || configJson.isEmpty())
		{
			return new ArrayList<>();
		}

		try
		{
			Type listType = TypeToken.getParameterized(ArrayList.class, CustomRoute.class).getType();
			List<CustomRoute> routes = customGson.fromJson(configJson, listType);
			if (routes == null)
			{
				return new ArrayList<>();
			}
			// Remove null routes and routes with null names (malformed data)
			routes.removeIf(r -> r == null || r.getName() == null);
			return routes;
		}
		catch (JsonParseException ex)
		{
			log.error("Failed to parse routes for {}: {}", taskType, ex.getMessage());
			return new ArrayList<>();
		}
	}

	/** Adds or updates a route. If a route with the same name exists, it is replaced. */
	public void addRoute(String taskType, CustomRoute route)
	{
		if (route == null || route.getName() == null)
		{
			log.warn("Cannot add route with null name");
			return;
		}
		List<CustomRoute> routes = loadRoutes(taskType);
		String routeName = route.getName();
		routes.removeIf(r -> r.getName().equals(routeName));
		routes.add(route);
		saveRoutes(taskType, routes);
	}

	/** Removes a route by name. */
	public void removeRoute(String taskType, String routeName)
	{
		if (routeName == null)
		{
			return;
		}
		List<CustomRoute> routes = loadRoutes(taskType);
		routes.removeIf(r -> routeName.equals(r.getName()));
		saveRoutes(taskType, routes);
	}

	// ========================================================================
	// Active Route Selection
	// Each tab can have a different route selected (per tab AND per taskType).
	// Config key pattern: "activeRoute-{tabId}-{taskType}"
	// ========================================================================

	/** Saves which route is active for a specific tab and task type. Pass null to clear. */
	public void saveActiveRouteName(ConfigValues.TaskListTabs tab, String taskType, String routeName)
	{
		String key = "activeRoute" + CONFIG_GROUP_PREFIX_SEPARATOR + tab.configID + CONFIG_GROUP_PREFIX_SEPARATOR + taskType;
		if (routeName == null || routeName.isEmpty())
		{
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP_NAME, key);
		}
		else
		{
			configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, key, routeName);
		}
	}

	/** Loads the name of the active route for a specific tab and task type. Returns null if none. */
	public String loadActiveRouteName(ConfigValues.TaskListTabs tab, String taskType)
	{
		String key = "activeRoute" + CONFIG_GROUP_PREFIX_SEPARATOR + tab.configID + CONFIG_GROUP_PREFIX_SEPARATOR + taskType;
		String name = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, key);
		if (name == null || name.isEmpty())
		{
			return null;
		}
		return name;
	}

	/** Loads and returns the full active route object for a tab, or null if none selected. */
	public CustomRoute getActiveRoute(ConfigValues.TaskListTabs tab, String taskType)
	{
		String name = loadActiveRouteName(tab, taskType);
		if (name == null || name.isEmpty())
		{
			return null;
		}
		return loadRoutes(taskType).stream()
			.filter(r -> name.equals(r.getName()))
			.findFirst()
			.orElse(null);
	}

	// ========================================================================
	// Custom Item Completion Tracking
	// Tracks which custom items (bank stops, teleports, etc.) have been marked complete.
	// Stored per taskType + routeName (shared across tabs using the same route).
	// Config key pattern: "customCompletion-{taskType}-{routeName}"
	// ========================================================================

	/** Saves the set of completed custom item IDs for a route. Pass empty/null to clear. */
	public void saveCustomItemCompletion(String taskType, String routeName, Set<String> completedIds)
	{
		String key = "customCompletion" + CONFIG_GROUP_PREFIX_SEPARATOR + taskType
			+ CONFIG_GROUP_PREFIX_SEPARATOR + routeName;
		if (completedIds == null || completedIds.isEmpty())
		{
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP_NAME, key);
		}
		else
		{
			String value = String.join(",", completedIds);
			configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, key, value);
		}
	}

	/** Loads the set of completed custom item IDs for a route. Returns empty set if none. */
	public Set<String> loadCustomItemCompletion(String taskType, String routeName)
	{
		String key = "customCompletion" + CONFIG_GROUP_PREFIX_SEPARATOR + taskType
			+ CONFIG_GROUP_PREFIX_SEPARATOR + routeName;
		String value = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, key);
		if (value == null || value.isEmpty())
		{
			return new HashSet<>();
		}
		return new HashSet<>(Arrays.asList(value.split(",")));
	}
}
