package com.tylerthardy.taskstracker.tasktypes;

import lombok.Data;
import net.runelite.client.callback.ClientThread;
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

    public abstract TaskPanel generatePanel(TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager);
}
