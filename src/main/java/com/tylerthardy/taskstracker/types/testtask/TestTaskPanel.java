package com.tylerthardy.taskstracker.types.testtask;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.types.Task;
import com.tylerthardy.taskstracker.types.TaskPanel;
import net.runelite.client.game.SpriteManager;

public class TestTaskPanel extends TaskPanel
{
    public TestTaskPanel(TasksTrackerPlugin plugin, SpriteManager spriteManager, Task task)
    {
        super(plugin, spriteManager, task);
    }
}
