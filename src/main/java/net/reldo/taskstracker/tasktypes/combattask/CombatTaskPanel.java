package net.reldo.taskstracker.tasktypes.combattask;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JPopupMenu;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.Util;
import net.reldo.taskstracker.panel.TaskPanel;
import net.reldo.taskstracker.panel.filters.TierFilter;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class CombatTaskPanel extends TaskPanel
{
	private String datePattern = "MM-dd-yyyy hh:mma";
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);

	public CombatTaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, CombatTask task)
	{
		super(plugin, clientThread, spriteManager, task);
		filters.add(new TierFilter(plugin.getConfig()));
	}

	@Override
	public JPopupMenu getPopupMenu()
	{
		return null;
	}

	@Override
	public String getTaskTooltip()
	{
		CombatTask task = (CombatTask) this.task;
		String text = Util.wrapWithBold(task.getName()) + Util.HTML_LINE_BREAK +
			task.getTier() + Util.HTML_LINE_BREAK +
			task.getMonster() + Util.HTML_LINE_BREAK +
			task.getDescription();

		if (task.isCompleted())
		{
			text += Util.HTML_LINE_BREAK + Util.HTML_LINE_BREAK + "✔ " + simpleDateFormat.format(new Date(task.getCompletedOn()));
		}

		text = Util.wrapWithWrappingParagraph(text, 200);

		return Util.wrapWithHtml(text);
	}

	@Override
	public BufferedImage getIcon()
	{
		CombatTaskTier tier = CombatTaskTier.tiersByName.get(task.getTier().toLowerCase());
		if (tier == null)
		{
			return null;
		}

		return spriteManager.getSprite(tier.spriteId, 0);
	}
}
