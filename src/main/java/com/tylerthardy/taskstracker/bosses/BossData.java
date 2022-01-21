package com.tylerthardy.taskstracker.bosses;

import java.util.HashMap;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginManager;

public class BossData extends HashMap<String, Integer>
{
	private static final String CHAT_COMMANDS_GROUP_NAME = "killcount";

	public BossData(PluginManager pluginManager, ConfigManager configManager)
	{
		if (pluginManager.getPlugins().stream().noneMatch(p -> p.getName().equals("Chat Commands")))
		{
			return;
		}

		for (Boss boss : Boss.values())
		{
			Integer killCount = configManager.getRSProfileConfiguration(CHAT_COMMANDS_GROUP_NAME, boss.getDisplayName().toLowerCase(), int.class);
			this.put(boss.getDisplayName(), killCount);
		}
	}
}
