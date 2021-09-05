package com.tylerthardy.taskstracker.tasktypes.testtask;

import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.GenericTaskPanel;
import com.tylerthardy.taskstracker.tasktypes.TaskManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class TestTaskPanel extends GenericTaskPanel
{
    public TestTaskPanel(TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager, Task task) {
        super(taskManager, clientThread, spriteManager, task);
    }
}
