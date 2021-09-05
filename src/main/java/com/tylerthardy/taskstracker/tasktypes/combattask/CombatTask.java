package com.tylerthardy.taskstracker.tasktypes.combattask;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import java.awt.image.BufferedImage;

public class CombatTask extends Task
{
    public String area;

    public CombatTask(String name, String description, String tier) {
        super(name, description, tier);
    }

    @Override
    public TaskPanel generatePanel(ClientThread clientThread, SpriteManager spriteManager) {
        return new CombatTaskPanel(clientThread, spriteManager, this);
    }
}
