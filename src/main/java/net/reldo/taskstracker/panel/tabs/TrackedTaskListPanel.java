package net.reldo.taskstracker.panel.tabs;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.TaskListPanel;
import net.reldo.taskstracker.tasktypes.Task;
import java.util.ArrayList;
import java.util.stream.Collectors;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

public class TrackedTaskListPanel extends TaskListPanel
{
	public TrackedTaskListPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
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
		return plugin.taskManagers.get(plugin.selectedTaskType).tasks
			.stream()
			.filter(Task::isTracked)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public String getEmptyTaskListMessage()
	{
		return "No tracked tasks found. Remove filters or add tracked tasks from the 'all tasks' tab.";
	}
}
