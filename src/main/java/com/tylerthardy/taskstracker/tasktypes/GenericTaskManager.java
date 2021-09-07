package com.tylerthardy.taskstracker.tasktypes;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import net.runelite.api.events.ChatMessage;

import java.util.HashMap;

public class GenericTaskManager extends AbstractTaskManager
{
    public GenericTaskManager(TaskType taskType, TasksTrackerPlugin plugin) {
        super(taskType, plugin);
    }

    @Override
    public void handleChatMessage(ChatMessage chatMessage) {
    }

    @Override
    public HashMap<String, Boolean> scrapeTaskCompletedData() {
        return null;
    }

    @Override
    public int scrapeTotalCount() {
        return 0;
    }
}
