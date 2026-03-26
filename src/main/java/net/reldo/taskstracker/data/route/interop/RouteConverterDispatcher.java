package net.reldo.taskstracker.data.route.interop;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.task.TaskService;

@Slf4j
@Singleton
public class RouteConverterDispatcher
{
	private final Map<String, RouteConverter> converters = new HashMap<>();
	private final TaskService taskService;

	@Inject
	public RouteConverterDispatcher(TaskService taskService)
	{
		this.taskService = taskService;

		register(new GrootsLeagueMapConverter());
	}

	private void register(RouteConverter converter)
	{
		converters.put(converter.getSource(), converter);
	}

	public RouteConverter getConverter(String source)
	{
		if (source == null)
		{
			return null;
		}
		return converters.get(source);
	}

	/**
	 * Runs the beforeImport hook if a converter matches the route's source.
	 * @return false if the converter cancelled the import, true otherwise
	 */
	public boolean runBeforeImport(CustomRoute route)
	{
		RouteConverter converter = getConverter(route.getSource());
		if (converter == null)
		{
			return true;
		}

		log.debug("Running beforeImport for source: {}", route.getSource());
		CustomRoute result = converter.beforeImport(route);
		return result != null;
	}

	/**
	 * Runs the convertToNative hook if a converter matches the route's source.
	 */
	public void runImportConvert(CustomRoute route)
	{
		RouteConverter converter = getConverter(route.getSource());
		if (converter == null)
		{
			return;
		}

		log.debug("Running convertToNative for source: {}", route.getSource());
		converter.convertToNative(route, taskService);
	}

	/**
	 * Runs the beforeExport hook if a converter matches the route's source.
	 * @return false if the converter cancelled the export, true otherwise
	 */
	public boolean runBeforeExport(CustomRoute route)
	{
		RouteConverter converter = getConverter(route.getSource());
		if (converter == null)
		{
			return true;
		}

		log.debug("Running beforeExport for source: {}", route.getSource());
		CustomRoute result = converter.beforeExport(route);
		return result != null;
	}

	/**
	 * Runs the convertToExternal hook if a converter matches the route's source.
	 */
	public void runExportConvert(CustomRoute route)
	{
		RouteConverter converter = getConverter(route.getSource());
		if (converter == null)
		{
			return;
		}

		log.debug("Running convertToExternal for source: {}", route.getSource());
		converter.convertToExternal(route, taskService);
	}
}
