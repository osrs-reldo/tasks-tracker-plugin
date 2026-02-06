package net.reldo.taskstracker.data.task.filters;

import java.util.HashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.FilterDataClient;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;

@Singleton
@Slf4j
public class FilterService
{
	@Inject
	private FilterDataClient filterDataClient;

	// Filter config cache
	private HashMap<String, FilterConfig> filterConfigs = new HashMap<>();

	public FilterConfig getGlobalFilterByKey(String filterKey)
	{
		// Instantiate filterConfigs if not already
		if (filterConfigs == null || filterConfigs.isEmpty()) {
			try {
				filterConfigs = filterDataClient.getFilterConfigs();
				return filterConfigs.get(filterKey);
			}
			catch (Exception ex) {
				log.error("Unable to get filter configs", ex);
			}
		}
		else {
			return filterConfigs.get(filterKey);
		}

		return null;
	}

	public void clearFilterConfigs()
	{
		this.filterConfigs.clear();
	}
}
