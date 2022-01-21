package net.reldo.taskstracker.panel.tabs;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.TaskListPanel;
import net.reldo.taskstracker.tasktypes.Task;
import java.util.ArrayList;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

public class AllTaskListPanel extends TaskListPanel
{
	public AllTaskListPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
	{
		super(plugin, clientThread, spriteManager, skillIconManager);
	}

	@Override
	public ArrayList<Task> getTasks()
	{
		// TODO: Build a filter service
		if (plugin.selectedTaskType == null)
		{
			return null;
		}
		return plugin.taskManagers.get(plugin.selectedTaskType).tasks;
	}

	@Override
	public String getEmptyTaskListMessage()
	{
		return "No tasks found.";
	}
}
