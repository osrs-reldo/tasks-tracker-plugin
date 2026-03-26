package net.reldo.taskstracker.data.route.interop;

import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.task.TaskService;

public interface RouteConverter
{
	/** The source identifier this converter handles (matches CustomRoute.source). */
	String getSource();

	/**
	 * Pre-import hook. Called after JSON deserialization but before the route is stored.
	 * Use for prompting the user (e.g., route name), validation, or cancellation.
	 * @return the route to import, or null to cancel import
	 */
	CustomRoute beforeImport(CustomRoute route);

	/**
	 * Converts tool-specific data into plugin-native format for internal use.
	 * Called after beforeImport and after taskType is set.
	 * The source field is preserved so the route can be exported back.
	 */
	void convertToNative(CustomRoute route, TaskService taskService);

	/**
	 * Pre-export hook. Called before the route is serialized for export.
	 * Use for prompting the user or cancellation.
	 * @return the route to export, or null to cancel export
	 */
	CustomRoute beforeExport(CustomRoute route);

	/**
	 * Converts plugin-native data back into the tool-specific format for export.
	 * Called after beforeExport. Should restore tool-specific fields
	 * (e.g., source, version, comments) so the exported JSON is valid for the target tool.
	 */
	void convertToExternal(CustomRoute route, TaskService taskService);
}
