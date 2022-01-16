package com.tylerthardy.taskstracker.panel.tabs;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.TaskListPanel;
import com.tylerthardy.taskstracker.tasktypes.Task;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TrackedTaskListPanel extends TaskListPanel
{
    public TrackedTaskListPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager) {
        super(plugin, clientThread, spriteManager, skillIconManager);
    }

    @Override
    public ArrayList<Task> getTasks() {
        // TODO: Build a filter service
		if (plugin.selectedTaskType == null)
			return null;
        return plugin.taskManagers.get(plugin.selectedTaskType).tasks
                .stream()
                .filter(Task::isTracked)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public String getEmptyTaskListMessage() {
        return "No tracked tasks found. Remove filters or add tracked tasks from the 'all tasks' tab.";
    }
}
