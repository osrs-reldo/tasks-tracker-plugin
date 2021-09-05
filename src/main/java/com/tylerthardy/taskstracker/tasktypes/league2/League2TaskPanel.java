package com.tylerthardy.taskstracker.tasktypes.league2;

import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskManager;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import javax.swing.JPopupMenu;
import java.awt.image.BufferedImage;

public class League2TaskPanel extends TaskPanel
{
    public League2TaskPanel(TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager, Task task) {
        super(taskManager, clientThread, spriteManager, task);
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
        League2TaskTier tier = League2TaskTier.getTierByName(task.getTier());
        if (tier == null)
        {
            return null;
        }

        return spriteManager.getSprite(tier.spriteId, 0);
    }
}
