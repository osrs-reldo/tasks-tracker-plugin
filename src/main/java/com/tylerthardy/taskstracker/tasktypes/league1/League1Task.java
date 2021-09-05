package com.tylerthardy.taskstracker.tasktypes.league1;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.GenericTaskPanel;
import com.tylerthardy.taskstracker.tasktypes.RequiredSkill;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import java.awt.image.BufferedImage;

public class League1Task extends Task
{
    public RequiredSkill[] skills;
    public String other;

    public League1Task(String name, String description, String tier)
    {
        super(name, description, tier);
    }

    @Override
    public TaskPanel generatePanel(ClientThread clientThread, SpriteManager spriteManager) {
        return new League1TaskPanel(clientThread, spriteManager, this);
    }
}
