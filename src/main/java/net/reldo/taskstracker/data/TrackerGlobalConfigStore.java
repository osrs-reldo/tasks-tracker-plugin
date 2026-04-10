package net.reldo.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.gson.GsonFactory;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.task.TaskService;
import net.runelite.client.config.ConfigManager;

@Singleton
@Slf4j
public class TrackerGlobalConfigStore
{
	public static final String CONFIG_TASKS_PREFIX = ConfigValues.CONFIG_TASKS_PREFIX;
	public static final String CONFIG_GROUP_PREFIX_SEPARATOR = ConfigValues.CONFIG_GROUP_PREFIX_SEPARATOR;
	public static final String CONFIG_GROUP_NAME = TasksTrackerPlugin.CONFIG_GROUP_NAME;

	private final Gson customGson;
	@Inject
	private TaskService taskService;
	@Inject
	private ConfigManager configManager;

	@Inject
	public TrackerGlobalConfigStore(Gson gson)
	{
		this.customGson = GsonFactory.newBuilder(gson).create();
	}

	// ========================================================================
	// Route Persistence
	// Routes are stored per-taskType.
	// Config key pattern: "routes-{taskType}"
	// ========================================================================

	/** Saves all routes for a given task type, replacing any existing routes. */
	public void saveRoutes(String taskType, List<CustomRoute> routes)
	{
		String key = "routes" + CONFIG_GROUP_PREFIX_SEPARATOR + taskType;
		String json = customGson.toJson(routes);
		configManager.setConfiguration(CONFIG_GROUP_NAME, key, json);
	}

	/** Loads all routes for a given task type. Returns empty list if none exist or on parse error. */
	public List<CustomRoute> loadRoutes(String taskType)
	{
		String key = "routes" + CONFIG_GROUP_PREFIX_SEPARATOR + taskType;
		String configJson = configManager.getConfiguration(CONFIG_GROUP_NAME, key);
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
			// Remove null routes and assign UUID to old route data
			routes.removeIf(route -> route == null);
			routes.forEach(route -> {
				if (route.getId() == null || route.getId().isEmpty())
				{
					route.setId(UUID.randomUUID().toString());
				}

				route.getSections().forEach(section -> {
					if (section.getId() == null || section.getId().isEmpty())
					{
						section.setId(UUID.randomUUID().toString());
					}
				});
			});
			saveRoutes(taskType, routes);
			return routes;
		}
		catch (JsonParseException ex)
		{
			log.error("Failed to parse routes for {}: {}", taskType, ex.getMessage());
			return new ArrayList<>();
		}
	}

	/** Adds or updates a route. If a route with the same id exists, it is replaced. */
	public void addRoute(String taskType, CustomRoute route)
	{
		if (route == null)
		{
			log.warn("Cannot add a null route");
			return;
		}
		List<CustomRoute> routes = loadRoutes(taskType);
		String routeId = route.getId();
		routes.removeIf(r -> r.getId().equals(routeId));
		routes.add(route);
		saveRoutes(taskType, routes);
	}

	/** Removes a route by id. */
	public void removeRoute(String taskType, String routeId)
	{
		if (routeId == null)
		{
			return;
		}
		List<CustomRoute> routes = loadRoutes(taskType);
		routes.removeIf(r -> routeId.equals(r.getId()));
		saveRoutes(taskType, routes);
	}

	// ========================================================================
	// Active Route Selection
	// Each tab can have a different route selected (per tab AND per taskType).
	// Stored in global config (shared across all profiles).
	// Config key pattern: "activeRoute-{tabId}-{taskType}"
	// ========================================================================

	/** Saves which route is active for a specific tab and task type. Pass null to clear. */
	public void saveActiveRouteId(ConfigValues.TaskListTabs tab, String taskType, String routeId)
	{
		String key = "activeRoute" + CONFIG_GROUP_PREFIX_SEPARATOR + tab.configID + CONFIG_GROUP_PREFIX_SEPARATOR + taskType;
		if (routeId == null || routeId.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP_NAME, key);
		}
		else
		{
			configManager.setConfiguration(CONFIG_GROUP_NAME, key, routeId);
		}
	}

	/** Loads the id of the active route for a specific tab and task type. Returns null if none. */
	public String loadActiveRouteId(ConfigValues.TaskListTabs tab, String taskType)
	{
		String key = "activeRoute" + CONFIG_GROUP_PREFIX_SEPARATOR + tab.configID + CONFIG_GROUP_PREFIX_SEPARATOR + taskType;
		String id = configManager.getConfiguration(CONFIG_GROUP_NAME, key);
		if (id == null || id.isEmpty())
		{
			return null;
		}
		return id;
	}

	/** Loads and returns the full active route object for a tab, or null if none selected. */
	public CustomRoute getActiveRoute(ConfigValues.TaskListTabs tab, String taskType)
	{
		String id = loadActiveRouteId(tab, taskType);
		if (id == null || id.isEmpty())
		{
			return null;
		}
		return loadRoutes(taskType).stream()
			.filter(r -> id.equals(r.getId()))
			.findFirst()
			.orElse(null);
	}

	// ========================================================================
	// Custom Item Completion Tracking
	// Tracks which custom items (bank stops, teleports, etc.) have been marked complete.
	// Stored in global config per taskType + routeId (shared across tabs using the same route).
	// Config key pattern: "customCompletion-{taskType}-{routeId}"
	// ========================================================================

	/** Saves the set of completed custom item IDs for a route. Pass empty/null to clear. */
	public void saveCustomItemCompletion(String taskType, String routeId, Set<String> completedIds)
	{
		String key = "customCompletion" + CONFIG_GROUP_PREFIX_SEPARATOR + taskType
			+ CONFIG_GROUP_PREFIX_SEPARATOR + routeId;
		if (completedIds == null || completedIds.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP_NAME, key);
		}
		else
		{
			String value = String.join(",", completedIds);
			configManager.setConfiguration(CONFIG_GROUP_NAME, key, value);
		}
	}

	/** Loads the set of completed custom item IDs for a route. Returns empty set if none. */
	public Set<String> loadCustomItemCompletion(String taskType, String routeId)
	{
		String key = "customCompletion" + CONFIG_GROUP_PREFIX_SEPARATOR + taskType
			+ CONFIG_GROUP_PREFIX_SEPARATOR + routeId;
		String value = configManager.getConfiguration(CONFIG_GROUP_NAME, key);
		if (value == null || value.isEmpty())
		{
			return new HashSet<>();
		}
		return new HashSet<>(Arrays.asList(value.split(",")));
	}

	/** Clears all custom item completion data for a route. */
	public void resetCustomItemCompletion(String taskType, String routeId)
	{
		saveCustomItemCompletion(taskType, routeId, null);
	}
}
