package net.reldo.taskstracker.tasktypes.league4;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.TaskPanel;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

public class League4Task extends Task
{

	public League4Task(int id, String name, String description, String tier, int clientSortId)
	{
		super(id, name, description, tier, clientSortId);
	}

	@Override
	public TaskType getType()
	{
		return TaskType.LEAGUE_4;
	}

	@Override
	public TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
	{
		return new League4TaskPanel(plugin, clientThread, spriteManager, this);
	}
}
