package net.reldo.taskstracker.data.route;

import com.google.gson.Gson;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.TrackerGlobalConfigStore;
import net.reldo.taskstracker.data.gson.GsonFactory;
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
	private TaskService taskService;
	@Inject
	private TasksTrackerConfig config;
	@Inject
	private TrackerGlobalConfigStore trackerGlobalConfigStore;

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

			if (route.getTaskType() != null && !route.getTaskType().equals(currentTaskType))
			{
				int result = JOptionPane.showConfirmDialog(
					null,
					"This route was created for " + route.getTaskType() +
						" but you're viewing " + currentTaskType + ".\n\nImport anyway?",
					"Task Type Mismatch",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
				);
				if (result != JOptionPane.YES_OPTION)
				{
					return false;
				}
			}

			route.setTaskType(currentTaskType);

			Set<CustomRouteItem> duplicates = getDuplicateCustomRouteItems(route);
			if (!duplicates.isEmpty())
			{
				int result = JOptionPane.showConfirmDialog(
					null,
					"Duplicate custom item IDs detected.\n"
						+ "The imported route may be different than expected.\n\n"
						+ "Import anyway?",
					"Duplicate IDs",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE
				);
				if (result != JOptionPane.OK_OPTION)
				{
					return false;
				}
				for (CustomRouteItem ci : duplicates)
				{
					String newId = UUID.randomUUID().toString();
					log.warn("Duplicate custom item ID '{}' found, regenerated as '{}'", ci.getId(), newId);
					ci.setId(newId);
				}
			}
			ConfigValues.TaskListTabs currentTab = config.taskListTab();

			trackerGlobalConfigStore.addRoute(currentTaskType, route);
			trackerGlobalConfigStore.saveActiveRouteName(currentTab, currentTaskType, route.getName());
			taskService.setActiveRoute(currentTab, route);

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
	 * Exports the active route to the system clipboard as JSON.
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

		Gson routeGson = GsonFactory.newBuilder(gson).setPrettyPrinting().create();

		String json = routeGson.toJson(route);

		StringSelection selection = new StringSelection(json);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

		log.debug("Exported route to clipboard: {}", route.getName());
		return true;
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
			null,
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
		trackerGlobalConfigStore.saveActiveRouteName(currentTab, taskType, name);
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
		String routeName = trackerGlobalConfigStore.loadActiveRouteName(currentTab, taskType);

		if (routeName == null)
		{
			return false;
		}

		int result = JOptionPane.showConfirmDialog(
			null,
			"Delete route \"" + routeName + "\"?",
			"Delete Route",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		);

		if (result != JOptionPane.YES_OPTION)
		{
			return false;
		}

		trackerGlobalConfigStore.removeRoute(taskType, routeName);

		// Clear active route on all tabs that reference the deleted route
		for (ConfigValues.TaskListTabs tab : ConfigValues.TaskListTabs.values())
		{
			String tabRouteName = trackerGlobalConfigStore.loadActiveRouteName(tab, taskType);
			if (routeName.equals(tabRouteName))
			{
				trackerGlobalConfigStore.saveActiveRouteName(tab, taskType, null);
				taskService.clearActiveRoute(tab);
			}
		}

		log.debug("Deleted route: {}", routeName);
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

	private void showErrorMessage(String message)
	{
		JOptionPane.showMessageDialog(
			null,
			message,
			"Error",
			JOptionPane.ERROR_MESSAGE
		);
	}
}
