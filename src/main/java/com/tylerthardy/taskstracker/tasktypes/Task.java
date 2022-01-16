package com.tylerthardy.taskstracker.tasktypes;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.TaskPanel;
import lombok.Data;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

@Data
public abstract class Task
{
    private final String name;
    private final String description;
    private final String tier;

    private boolean completed;
    private boolean tracked;
    private float timestamp;

	public abstract TaskType getType();
    public abstract TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager);
}
