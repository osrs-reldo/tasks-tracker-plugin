package com.tylerthardy.taskstracker.tasktypes.league3;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.TaskPanel;
import com.tylerthardy.taskstracker.tasktypes.Task;
import java.awt.image.BufferedImage;
import javax.swing.JPopupMenu;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

public class League3TaskPanel extends TaskPanel
{
	private final SkillIconManager skillIconManager;

	public League3TaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager, Task task) {
		super(plugin, clientThread, spriteManager, task);
		this.skillIconManager = skillIconManager;
    }

    @Override
    public JPopupMenu getPopupMenu() {
        return null;
    }

    @Override
    public String getTaskTooltip() {
        return task.getDescription();
    }

    @Override
    public BufferedImage getIcon() {
        League3TaskTier tier = League3TaskTier.getTierByName(task.getTier());
        if (tier == null)
        {
            return null;
        }

        return spriteManager.getSprite(tier.spriteId, 0);
    }
}
