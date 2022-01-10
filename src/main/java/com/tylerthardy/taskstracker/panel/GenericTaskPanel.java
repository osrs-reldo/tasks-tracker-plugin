package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.Util;
import com.tylerthardy.taskstracker.tasktypes.Task;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.SpriteID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import java.awt.image.BufferedImage;

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
    public BufferedImage getIcon() {
        return spriteManager.getSprite(SpriteID.PRAYER_PROTECT_FROM_MAGIC, 0);
    }
}
