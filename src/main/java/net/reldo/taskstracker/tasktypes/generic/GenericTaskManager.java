package net.reldo.taskstracker.tasktypes.generic;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.TrackerDataStore;
import net.reldo.taskstracker.tasktypes.AbstractTaskManager;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.api.events.ChatMessage;

public class GenericTaskManager extends AbstractTaskManager
{
	public GenericTaskManager(TaskType taskType, TasksTrackerPlugin plugin, TrackerDataStore trackerDataStore)
	{
		super(taskType, plugin, trackerDataStore);
	}

	@Override
	public void handleChatMessage(ChatMessage chatMessage)
	{
	}
}
