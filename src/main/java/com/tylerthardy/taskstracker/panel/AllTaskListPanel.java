package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.Task;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import java.util.ArrayList;

public class AllTaskListPanel extends TaskListPanel
{
    public AllTaskListPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager) {
        super(plugin, clientThread, spriteManager);
    }

    @Override
    public ArrayList<Task> getTasks() {
        // TODO: Build a filter service
        return plugin.taskManagers.get(plugin.selectedTaskType).tasks;
    }

    @Override
    public String getEmptyTaskListMessage() {
        return "No tasks found.";
    }
}
