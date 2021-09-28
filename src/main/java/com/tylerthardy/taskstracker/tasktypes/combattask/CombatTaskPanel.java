package com.tylerthardy.taskstracker.tasktypes.combattask;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.Util;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import java.awt.image.BufferedImage;
import javax.swing.JPopupMenu;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class CombatTaskPanel extends TaskPanel
{
    public CombatTaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, CombatTask task) {
        super(plugin, clientThread, spriteManager, task);
    }

    @Override
    public JPopupMenu getPopupMenu() {
        return null;
    }

    @Override
    public String getTaskTooltip() {
		CombatTask task = (CombatTask) this.task;
		String text = Util.wrapWithBold(task.getName()) + Util.HTML_LINE_BREAK +
			task.getTier() + Util.HTML_LINE_BREAK +
			task.getMonster() + Util.HTML_LINE_BREAK +
			task.getDescription();

		text = Util.wrapWithWrappingParagraph(text, 200);

		return Util.wrapWithHtml(text);
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
