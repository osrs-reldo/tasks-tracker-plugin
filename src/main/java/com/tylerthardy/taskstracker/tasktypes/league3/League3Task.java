package com.tylerthardy.taskstracker.tasktypes.league3;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.TaskPanel;
import com.tylerthardy.taskstracker.tasktypes.RequiredSkill;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

public class League3Task extends Task
{
    public RequiredSkill[] skills;
    public String other;

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
