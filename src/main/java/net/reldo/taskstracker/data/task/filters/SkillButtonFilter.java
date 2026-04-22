package net.reldo.taskstracker.data.task.filters;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinitionSkill;
import net.reldo.taskstracker.data.task.ITask;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class SkillButtonFilter extends Filter
{
	private final String filterConfigKey;
	private final ConfigManager configManager;

	public SkillButtonFilter(ConfigManager configManager, String filterConfigKey)
	{
		this.configManager = configManager;
		this.filterConfigKey = filterConfigKey;
	}

	@Override
	public boolean meetsCriteria(ITask task)
	{
		String configValue = configManager.getConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, filterConfigKey);
		boolean isEmptyFilterSelection = configValue == null || configValue.isEmpty() || configValue.equals("-1");
		if (isEmptyFilterSelection)
		{
			return false;
		}

		List<TaskDefinitionSkill> skills = task.getTaskDefinition().getSkills();
		boolean hasNoSkills = (skills == null || skills.isEmpty());

		if (hasNoSkills)
		{
			return configValue.contains("f-NONE-f");
		}

		for (TaskDefinitionSkill skill : skills)
		{
			if (configValue.contains("f-" + skill.getSkill() + "-f"))
			{
				return true;
			}
		}
		return false;
	}
}
