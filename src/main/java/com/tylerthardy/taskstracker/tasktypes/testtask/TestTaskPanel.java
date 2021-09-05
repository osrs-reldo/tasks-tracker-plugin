package com.tylerthardy.taskstracker.tasktypes.testtask;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.GenericTaskPanel;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class TestTaskPanel extends GenericTaskPanel
{
    public TestTaskPanel(ClientThread clientThread, SpriteManager spriteManager, Task task)
    {
        super(clientThread, spriteManager, task);
    }
}
