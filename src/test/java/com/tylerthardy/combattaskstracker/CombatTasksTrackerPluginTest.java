package com.tylerthardy.combattaskstracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CombatTasksTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CombatTasksTrackerPlugin.class);
		RuneLite.main(args);
	}
}