package net.reldo.taskstracker.data.task.filters;

import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class ParamDropdownFilter extends Filter
{
	private final String paramName;
	private final String filterConfigKey;
	private final ConfigManager configManager;

	public ParamDropdownFilter(ConfigManager configManager, String paramName, String filterConfigKey)
	{
		this.configManager = configManager;
		this.paramName = paramName;
		this.filterConfigKey = filterConfigKey;
	}

	@Override
	public boolean meetsCriteria(TaskFromStruct task)
	{
		String configValue = configManager.getConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, filterConfigKey);
		log.debug("{}.meetsCriteria {}={} taskValue={}", this.getClass().getSimpleName(), filterConfigKey, configValue, task.getIntParam(paramName));
		boolean isEmptyFilterSelection = configValue == null || configValue.isEmpty() || configValue.equals("-1");
		if (isEmptyFilterSelection)
		{
			return true;
		}
		if (task.getIntParam(paramName) != null)
		{
			try
			{
				Integer parsedConfigValue = Integer.parseInt(configValue);
				return parsedConfigValue.equals(task.getIntParam(paramName));
			}
			catch (Exception ex)
			{
				log.warn("meetsCriteria error parsing config value for  {}", configValue);
				return true;
			}
		}
		return true;
	}
}
