package net.reldo.taskstracker.tasktypes.league3;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.TaskPanel;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

public class League3Task extends Task
{
	public int id;

	public League3Task(String name, String description, String tier)
	{
		super(name, description, tier);
	}

	@Override
	public TaskType getType()
	{
		return TaskType.LEAGUE_3;
	}

	@Override
	public TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
	{
		return new League3TaskPanel(plugin, clientThread, spriteManager, this);
	}
}
