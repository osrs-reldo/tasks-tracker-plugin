package com.tylerthardy.taskstracker.tasktypes.league1;

import com.tylerthardy.taskstracker.tasktypes.RequiredSkill;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskManager;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class League1Task extends Task
{
    public RequiredSkill[] skills;
    public String other;

    public League1Task(String name, String description, String tier)
    {
        super(name, description, tier);
    }

    @Override
    public TaskPanel generatePanel(TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager) {
        return new League1TaskPanel(taskManager, clientThread, spriteManager, this);
    }
}
