package com.tylerthardy.taskstracker.tasktypes.combattask;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import javax.swing.JPopupMenu;
import java.awt.image.BufferedImage;

public class CombatTaskPanel extends TaskPanel
{
    public CombatTaskPanel(ClientThread clientThread, SpriteManager spriteManager, Task task) {
        super(clientThread, spriteManager, task);
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
        CombatTaskTier tier = CombatTaskTier.getTierByName(task.getTier());
        if (tier == null)
        {
            return null;
        }

        return spriteManager.getSprite(tier.spriteId, 0);
    }
}
