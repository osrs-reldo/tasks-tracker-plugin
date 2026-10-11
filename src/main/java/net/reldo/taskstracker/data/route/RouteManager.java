package net.reldo.taskstracker.data.route;

import com.google.gson.Gson;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.TrackerGlobalConfigStore;
import net.reldo.taskstracker.data.gson.GsonFactory;
import net.reldo.taskstracker.data.jsondatastore.PremadeRouteClient;
import net.reldo.taskstracker.data.jsondatastore.types.PremadeRouteEntry;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.data.task.TaskService;

/**
 * Manages route CRUD operations: import, export, create, delete.
 * Handles data persistence and user dialogs.
 * UI refresh is the caller's responsibility.
 * Future in-game route editing functions may also be added here.
 */
@Slf4j
@Singleton
public class RouteManager
{
	@Inject
	private Gson gson;
	@Inject
	private TasksTrackerPlugin plugin;
	@Inject
	private TaskService taskService;
	@Inject
	private TasksTrackerConfig config;
	@Inject
	private TrackerGlobalConfigStore trackerGlobalConfigStore;
	@Inject
	private PremadeRouteClient premadeRouteClient;

	/**
	 * Imports a route from the system clipboard.
	 * Shows confirmation dialog if task type mismatches.
	 * @return true if a route was imported successfully
	 */
	public boolean importRouteFromClipboard()
	{
		try
		{
			String clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
				.getData(DataFlavor.stringFlavor).toString();

			Gson routeGson = GsonFactory.newBuilder(gson).create();
			CustomRoute route = routeGson.fromJson(clipboard, CustomRoute.class);

			route = validateAndPrepareRoute(route);
			if (route == null)
			{
				return false;
			}

			saveAndActivateRoute(route);
			log.debug("Imported route: {}", route.getName());
			return true;
		}
		catch (Exception e)
		{
			log.error("Failed to import route", e);
			showErrorMessage("Failed to import route: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Fetches the premade route manifest from the repository.
	 * @return list of available premade routes, filtered to current task type
	 */
	public List<PremadeRouteEntry> fetchPremadeRouteManifest() throws Exception
	{
		return premadeRouteClient.getManifest();
	}

	/**
	 * Fetches a premade route from the repository.
	 * This performs a network call and must NOT be called on the EDT.
	 * @param filename the route filename (without .json extension)
	 * @return the fetched route
	 */
	public CustomRoute fetchPremadeRoute(String filename) throws Exception
	{
		return premadeRouteClient.getRoute(filename);
	}

	/**
	 * Imports a pre-fetched route (from premade routes or any other source).
	 * Validates, saves, and activates the route. Safe to call on the EDT.
	 * @return true if the route was imported successfully
	 */
	public boolean importRoute(CustomRoute route)
	{
		try
		{
			route = validateAndPrepareRoute(route);
			if (route == null)
			{
				return false;
			}

			saveAndActivateRoute(route);
			log.debug("Imported route: {}", route.getName());
			return true;
		}
		catch (Exception e)
		{
			log.error("Failed to import route", e);
			showErrorMessage("Failed to import route: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Validates and sanitizes a route for import.
	 * Checks required fields, generates missing UUIDs, handles task type mismatch,
	 * and detects/resolves duplicate IDs.
	 * @return the validated route, or null if the user cancelled a dialog
	 */
	private CustomRoute validateAndPrepareRoute(CustomRoute route) throws Exception
	{
		if (route == null)
		{
			throw new Exception("Invalid route JSON");
		}

		if (route.getName() == null || route.getName().isEmpty())
		{
			throw new Exception("Missing route name");
		}

		if (route.getTaskType() == null || route.getTaskType().isEmpty())
		{
			throw new Exception("Missing route task type");
		}

		if (route.getId() == null || route.getId().isEmpty())
		{
			route.setId(UUID.randomUUID().toString());
		}

		String currentTaskType = taskService.getCurrentTaskType().getTaskJsonName();

		if (!route.getTaskType().equals(currentTaskType))
		{
			int result = JOptionPane.showConfirmDialog(
				plugin.pluginPanel,
				"This route was created for " + route.getTaskType() +
					" but you're viewing " + currentTaskType + ".\n\nImport anyway?",
				"Task Type Mismatch",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
			);
			if (result != JOptionPane.YES_OPTION)
			{
				return null;
			}
		}

		List<RouteSection> sections = route.getSections();
		for (RouteSection section : sections)
		{
			if (section.getId() == null || section.getId().isEmpty())
			{
				section.setId(UUID.randomUUID().toString());
			}

			for (RouteItem item : section.getItems())
			{
				if (!item.isTask() && item.getCustomItem() != null)
				{
					CustomRouteItem customItem = item.getCustomItem();
					if (customItem.getId() == null || customItem.getId().isEmpty())
					{
						customItem.setId(UUID.randomUUID().toString());
					}
				}
			}
		}

		route.setTaskType(currentTaskType);

		Set<CustomRouteItem> duplicateCustomItems = getDuplicateCustomRouteItems(route);
		Set<Integer> duplicateTasks = getDuplicateTaskIds(route);
		if (!duplicateCustomItems.isEmpty() || !duplicateTasks.isEmpty())
		{
			List<String> duplicateMessages = new ArrayList<>();
			duplicateMessages.add(!duplicateTasks.isEmpty() ? "task IDs" : "");
			duplicateMessages.add(!duplicateCustomItems.isEmpty() ? "custom item IDs" : "");
			String duplicatesMessage = duplicateMessages.stream()
				.filter(string -> !string.isEmpty())
				.collect(Collectors.joining(" and "));

			int result = JOptionPane.showConfirmDialog(
				plugin.pluginPanel,
				"Duplicate " + duplicatesMessage + " detected.\n"
					+ "The imported route may be different than expected.\n\n"
					+ "Import anyway?",
				"Duplicate IDs",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE
			);
			if (result != JOptionPane.OK_OPTION)
			{
				return null;
			}
			for (Integer taskId : duplicateTasks)
			{
				log.warn("Duplicate task ID '{}' found, all later instances removed", taskId);
				RouteSection firstSection = route.getSectionForTask(taskId);
				sections.forEach(section ->
				{
					if (!section.equals(firstSection))
					{
						section.remove(taskId);
					}
				});
			}
			for (CustomRouteItem customRouteItem : duplicateCustomItems)
			{
				String newId = UUID.randomUUID().toString();
				log.warn("Duplicate custom item ID '{}' found, regenerated as '{}'", customRouteItem.getId(), newId);
				customRouteItem.setId(newId);
			}
		}

		return route;
	}

	/**
	 * Persists the route and sets it as the active route on the current tab.
	 */
	private void saveAndActivateRoute(CustomRoute route)
	{
		String taskType = taskService.getCurrentTaskType().getTaskJsonName();
		ConfigValues.TaskListTabs currentTab = config.taskListTab();
		trackerGlobalConfigStore.addRoute(taskType, route);
		trackerGlobalConfigStore.saveActiveRouteId(currentTab, taskType, route.getId());
		taskService.setActiveRoute(currentTab, route);
	}

	/**
	 * Removes a task from the active route.
	 * @param taskId the task ID to remove
	 * @return true if the task was found and removed
	 */
	public boolean removeTaskFromActiveRoute(int taskId)
	{
		CustomRoute activeRoute = taskService.getActiveRoute();
		if (activeRoute == null)
		{
			return false;
		}
		if (activeRoute.removeTask(taskId))
		{
			String taskType = taskService.getCurrentTaskType().getTaskJsonName();
			trackerGlobalConfigStore.addRoute(taskType, activeRoute);
			taskService.addRouteIndex(activeRoute);
			return true;
		}
		return false;
	}

	/**
	 * Exports the active route to the system clipboard as JSON.
	 * Populates per-item completedOn timestamps so planning tools can display progress.
	 * @return true if a route was exported
	 */
	public boolean exportActiveRoute()
	{
		ConfigValues.TaskListTabs currentTab = config.taskListTab();
		String taskType = taskService.getCurrentTaskType().getTaskJsonName();

		CustomRoute route = trackerGlobalConfigStore.getActiveRoute(currentTab, taskType);

		if (route == null)
		{
			showErrorMessage("No active route to export");
			return false;
		}

		populateCompletedOn(route, taskType);

		Gson routeGson = GsonFactory.newBuilder(gson).setPrettyPrinting().create();

		String json = routeGson.toJson(route);

		StringSelection selection = new StringSelection(json);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

		log.debug("Exported route to clipboard: {}", route.getName());
		return true;
	}

	/**
	 * Decorates each RouteItem with completedOn (epoch ms) if it's been marked complete.
	 * Tasks use their tracked completion timestamp; custom items use export-time since the
	 * plugin doesn't persist real completion timestamps for them.
	 */
	private void populateCompletedOn(CustomRoute route, String taskType)
	{
		Set<String> completedCustomItems = trackerGlobalConfigStore.loadCustomItemCompletion(taskType, route.getId());
		long now = System.currentTimeMillis();

		for (RouteSection section : route.getSections())
		{
			for (RouteItem item : section.getItems())
			{
				if (item.isTask())
				{
					TaskFromStruct task = taskService.getTaskByStructId(item.getTaskId());
					if (task != null && task.getCompletedOn() > 0)
					{
						item.setCompletedOn(task.getCompletedOn());
					}
				}
				else if (item.getCustomItem() != null && completedCustomItems.contains(item.getCustomItem().getId()))
				{
					item.setCompletedOn(now);
				}
			}
		}
	}

	/**
	 * Creates a new route from the given task IDs.
	 * Shows a dialog to enter the route name.
	 * @param visibleTaskIds ordered list of task IDs to include
	 * @return true if a route was created
	 */
	public boolean createRouteFromCurrentOrder(List<Integer> visibleTaskIds)
	{
		String name = JOptionPane.showInputDialog(
			plugin.pluginPanel,
			"Enter route name:",
			"Create Route",
			JOptionPane.PLAIN_MESSAGE
		);

		if (name == null || name.trim().isEmpty())
		{
			return false;
		}

		name = name.trim();

		if (visibleTaskIds.isEmpty())
		{
			showErrorMessage("No visible tasks to create route from");
			return false;
		}

		CustomRoute route = new CustomRoute(UUID.randomUUID().toString(), name, taskService.getCurrentTaskType().getTaskJsonName());
		route.setTaskType(taskService.getCurrentTaskType().getTaskJsonName());
		route.setAuthor("User");
		route.setDescription("Created from current task order");

		RouteSection section = new RouteSection(UUID.randomUUID().toString(), "All Tasks");
		section.setTaskIds(visibleTaskIds);

		route.setSections(List.of(section));

		ConfigValues.TaskListTabs currentTab = config.taskListTab();
		String taskType = route.getTaskType();

		trackerGlobalConfigStore.addRoute(taskType, route);
		trackerGlobalConfigStore.saveActiveRouteId(currentTab, taskType, route.getId());
		taskService.setActiveRoute(currentTab, route);

		log.debug("Created route from current order: {}", name);
		return true;
	}

	/**
	 * Deletes the active route after user confirmation.
	 * Clears the active route on all tabs that reference it.
	 * @return true if a route was deleted
	 */
	public boolean deleteActiveRoute()
	{
		ConfigValues.TaskListTabs currentTab = config.taskListTab();
		String taskType = taskService.getCurrentTaskType().getTaskJsonName();
		CustomRoute activeRoute = taskService.getActiveRoute(currentTab);

		if (activeRoute == null)
		{
			return false;
		}

		String routeId = activeRoute.getId();

		int result = JOptionPane.showConfirmDialog(
			plugin.pluginPanel,
			"Delete route \"" + activeRoute.getName() + "\"?",
			"Delete Route",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		);

		if (result != JOptionPane.YES_OPTION)
		{
			return false;
		}

		trackerGlobalConfigStore.removeRoute(taskType, routeId);

		// Clear active route on all tabs that reference the deleted route
		for (ConfigValues.TaskListTabs tab : ConfigValues.TaskListTabs.values())
		{
			String tabRouteId = trackerGlobalConfigStore.loadActiveRouteId(tab, taskType);
			if (routeId.equals(tabRouteId))
			{
				trackerGlobalConfigStore.saveActiveRouteId(tab, taskType, null);
				taskService.clearActiveRoute(tab);
			}
		}

		log.debug("Deleted route: {}", routeId);
		return true;
	}

	/**
	 * Returns a set of duplicate CustomRouteItems (by ID) found in the route.
	 * If empty, there are no duplicates.
	 */
	private Set<CustomRouteItem> getDuplicateCustomRouteItems(CustomRoute route)
	{
		Set<CustomRouteItem> duplicates = new HashSet<>();
		if (route.getSections() == null)
		{
			return duplicates;
		}
		Set<String> seenIds = new HashSet<>();
		for (RouteSection section : route.getSections())
		{
			for (RouteItem item : section.getItems())
			{
				if (!item.isTask() && item.getCustomItem() != null)
				{
					CustomRouteItem ci = item.getCustomItem();
					if (!seenIds.add(ci.getId()))
					{
						duplicates.add(ci);
					}
				}
			}
		}
		return duplicates;
	}

	/**
	 * Returns a set of duplicate tasks (by ID) found in the route.
	 * If empty, there are no duplicates.
	 */
	private Set<Integer> getDuplicateTaskIds(CustomRoute route)
	{
		Set<Integer> duplicates = new HashSet<>();
		if (route.getSections() == null)
		{
			return duplicates;
		}
		Set<Integer> seenIds = new HashSet<>();
		for (RouteSection section : route.getSections())
		{
			for (RouteItem item : section.getItems())
			{
				if (item.isTask() && item.getTaskId() != null)
				{
					Integer taskId = item.getTaskId();
					if (!seenIds.add(taskId))
					{
						duplicates.add(taskId);
					}
				}
			}
		}
		return duplicates;
	}

	private void showErrorMessage(String message)
	{
		JOptionPane.showMessageDialog(
			plugin.pluginPanel,
			message,
			"Error",
			JOptionPane.ERROR_MESSAGE
		);
	}
}
