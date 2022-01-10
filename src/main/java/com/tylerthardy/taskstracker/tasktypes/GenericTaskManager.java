package com.tylerthardy.taskstracker.tasktypes;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
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
