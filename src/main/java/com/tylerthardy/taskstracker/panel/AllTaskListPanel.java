package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class AllTaskListPanel extends TaskListPanel
{
    public AllTaskListPanel(TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager) {
        super(taskManager, clientThread, spriteManager);
    }

    @Override
    public ArrayList<Task> getTasks() {
        // TODO: Build a filter service
        return taskManager.tasks.get(taskManager.selectedTaskType)
                .stream()
                .filter(t -> taskManager.taskTextFilter == null || t.getName().toLowerCase().startsWith(taskManager.taskTextFilter))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public String getEmptyTaskListMessage() {
        return "No tasks found.";
    }
}
