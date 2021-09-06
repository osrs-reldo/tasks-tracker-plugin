package com.tylerthardy.taskstracker.tasktypes.combattask;

import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskManager;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class CombatTask extends Task
{
    public String area;
    public String monster;
    public String category;
    public CombatTask(String name, String description, String tier) {
        super(name, description, tier);
    }

    @Override
    public TaskPanel generatePanel(TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager) {
        return new CombatTaskPanel(taskManager, clientThread, spriteManager, this);
    }
}
