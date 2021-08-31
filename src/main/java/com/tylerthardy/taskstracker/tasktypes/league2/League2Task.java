package com.tylerthardy.taskstracker.tasktypes.league2;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.GenericTaskPanel;
import com.tylerthardy.taskstracker.tasktypes.RequiredSkill;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import java.awt.image.BufferedImage;

public class League2Task extends Task
{
    public String area;
    public RequiredSkill[] skills;
    public String other;

    public League2Task(String name, String description, String tier)
    {
        super(name, description, tier);
    }

    @Override
    public TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager) {
        return new League2TaskPanel(plugin, clientThread, spriteManager, this);
    }
}
