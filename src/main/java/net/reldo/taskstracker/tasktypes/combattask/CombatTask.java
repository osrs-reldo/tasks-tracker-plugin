package net.reldo.taskstracker.tasktypes.combattask;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.TaskPanel;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import lombok.Getter;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

public class CombatTask extends Task
{
	@Getter
	public String monster;
	@Getter
	public String category;

	public CombatTask(String name, String description, String tier)
	{
		super(name, description, tier);
	}

	@Override
	public TaskType getType()
	{
		return TaskType.COMBAT;
	}

	@Override
	public TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
	{
		return new CombatTaskPanel(plugin, clientThread, spriteManager, this);
	}
}
