package com.tylerthardy.taskstracker.tasktypes.testtask;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.panel.TaskPanel;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

public class TestTask extends Task
{
    public String filter1;
    public String filter2;
    public String filter3;
    public TestTask(String name, String description, String tier) {
        super(TaskType.TEST, name, description, tier);
    }

    @Override
    public TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager) {
        return new TestTaskPanel(plugin, clientThread, spriteManager, this);
    }
}
