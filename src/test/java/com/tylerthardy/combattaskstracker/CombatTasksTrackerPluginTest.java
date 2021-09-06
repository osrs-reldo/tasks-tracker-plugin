package com.tylerthardy.combattaskstracker;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CombatTasksTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TasksTrackerPlugin.class);
		RuneLite.main(args);
	}
}