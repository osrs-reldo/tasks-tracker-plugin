package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TrackedTaskListPanel extends TaskListPanel
{
    public TrackedTaskListPanel(TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager) {
        super(taskManager, clientThread, spriteManager);
    }

    @Override
    public ArrayList<Task> getTasks() {
        return taskManager.tasks.get(taskManager.selectedTaskType)
                .stream()
                .filter(Task::isTracked)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public String getEmptyTaskListMessage() {
        return "No tracked tasks found. Remove filters or add tracked tasks from the 'all tasks' tab.";
    }
}
