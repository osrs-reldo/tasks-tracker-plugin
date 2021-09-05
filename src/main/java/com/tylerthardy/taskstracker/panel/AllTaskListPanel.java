package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import java.util.ArrayList;

public class AllTaskListPanel extends TaskListPanel
{
    public AllTaskListPanel(TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager) {
        super(taskManager, clientThread, spriteManager);
    }

    @Override
    public ArrayList<Task> getTasks() {
        return taskManager.tasks.get(taskManager.selectedTaskType);
    }

    @Override
    public String getEmptyTaskListMessage() {
        return "No tasks found.";
    }
}
