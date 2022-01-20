package com.tylerthardy.taskstracker.tasktypes.league2;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.TaskPanel;
import com.tylerthardy.taskstracker.tasktypes.Task;
import java.awt.image.BufferedImage;
import javax.swing.JPopupMenu;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class League2TaskPanel extends TaskPanel
{
	public League2TaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, Task task)
	{
		super(plugin, clientThread, spriteManager, task);
	}

	@Override
	public JPopupMenu getPopupMenu()
	{
		return null;
	}

	@Override
	public String getTaskTooltip()
	{
		return task.getDescription();
	}

	@Override
	public BufferedImage getIcon()
	{
		League2TaskTier tier = League2TaskTier.getTierByName(task.getTier());
		if (tier == null)
		{
			return null;
		}

		return spriteManager.getSprite(tier.spriteId, 0);
	}
}
