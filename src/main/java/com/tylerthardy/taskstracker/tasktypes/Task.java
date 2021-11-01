package com.tylerthardy.taskstracker.tasktypes;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import lombok.Data;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

@Data
public abstract class Task
{
    private static String dataFileName;
    private static TaskType type;

    private final String name;
    private final String description;
    private final String tier;

    private boolean completed;
    private boolean tracked;

    public abstract TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager);
}
