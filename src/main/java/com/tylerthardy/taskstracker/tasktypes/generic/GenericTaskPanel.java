package com.tylerthardy.taskstracker.tasktypes.generic;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.Util;
import com.tylerthardy.taskstracker.panel.TaskPanel;
import com.tylerthardy.taskstracker.tasktypes.Task;
import java.awt.image.BufferedImage;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.SpriteID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

@Slf4j
public class GenericTaskPanel extends TaskPanel
{
	public GenericTaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, Task task)
	{
		super(plugin, clientThread, spriteManager, task);
	}

	public JPopupMenu getPopupMenu()
	{
		final JMenuItem removeTask = new JMenuItem("Remove");
		removeTask.addActionListener(e -> log.info(task.getName())); // plugin.toggleTrackTask(task.name));

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		popupMenu.add(removeTask);

		return popupMenu;
	}

	@Override
	public String getTaskTooltip()
	{
		return Util.wrapWithHtml(task.getName() +
			"</br>" +
			task.getTier() +
			"</br>" +
			task.getDescription());
	}

	@Override
	public BufferedImage getIcon()
	{
		return spriteManager.getSprite(SpriteID.QUESTS_PAGE_ICON_BLUE_QUESTS, 0);
	}
}
