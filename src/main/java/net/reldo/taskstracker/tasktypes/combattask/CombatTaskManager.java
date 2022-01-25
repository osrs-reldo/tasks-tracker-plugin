package net.reldo.taskstracker.tasktypes.combattask;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.TaskDataClient;
import net.reldo.taskstracker.data.TrackerDataStore;
import net.reldo.taskstracker.tasktypes.AbstractTaskManager;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

public class CombatTaskManager extends AbstractTaskManager
{
	public CombatTaskManager(Client client, ClientThread clientThread, TasksTrackerPlugin plugin, TrackerDataStore trackerDataStore, TaskDataClient taskDataClient)
	{
		super(TaskType.COMBAT, plugin, trackerDataStore, taskDataClient);
	}
}
