package net.reldo.taskstracker.data.task.filters;

import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.TaskMetadatum;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class MetadataDropdownFilter extends Filter
{
	private final String filterConfigKey;
	private final ConfigManager configManager;
	private final String metadataKey;

	public MetadataDropdownFilter(ConfigManager configManager, String metadataKey, String filterConfigKey)
	{
		this.configManager = configManager;
		this.metadataKey = metadataKey;
		this.filterConfigKey = filterConfigKey;
	}

	@Override
	public boolean meetsCriteria(TaskFromStruct task)
	{
		String configValue = configManager.getConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, filterConfigKey);
		boolean isEmptyFilterSelection = configValue == null || configValue.isEmpty() || configValue.equals("-1");
		if (isEmptyFilterSelection)
		{
			return true;
		}
		if (task.getTaskDefinition().getMetadata() == null || !task.getTaskDefinition().getMetadata().containsKey(metadataKey))
		{
			return false;
		}

		TaskMetadatum metadatum = task.getTaskDefinition().getMetadata().get(metadataKey);
		if (metadatum.integer == null && metadatum.string == null) return false;
		if (metadatum.integer != null) {
			try
			{
				Integer parsedConfigValue = Integer.parseInt(configValue);
				return parsedConfigValue.equals(metadatum.integer);
			}
			catch (Exception ex)
			{
				log.warn("meetsCriteria error parsing config value for {}", configValue);
				return true;
			}
		}
		return metadatum.string.equals(configValue);
	}
}
