package com.tylerthardy.taskstracker.tasktypes.combattask;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.panel.TaskPanel;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import lombok.Getter;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

public class CombatTask extends Task
{
	@Getter
    public String monster;
	@Getter
    public String category;
    public CombatTask(String name, String description, String tier) {
        super(TaskType.COMBAT, name, description, tier);
    }

    @Override
    public TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager) {
        return new CombatTaskPanel(plugin, clientThread, spriteManager, this);
    }
}
