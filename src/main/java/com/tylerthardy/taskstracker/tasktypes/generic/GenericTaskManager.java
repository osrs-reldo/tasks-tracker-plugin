package com.tylerthardy.taskstracker.tasktypes.generic;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.AbstractTaskManager;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import net.runelite.api.events.ChatMessage;

public class GenericTaskManager extends AbstractTaskManager
{
    public GenericTaskManager(TaskType taskType, TasksTrackerPlugin plugin) {
        super(taskType, plugin);
    }

    @Override
    public void handleChatMessage(ChatMessage chatMessage) {
    }
}
