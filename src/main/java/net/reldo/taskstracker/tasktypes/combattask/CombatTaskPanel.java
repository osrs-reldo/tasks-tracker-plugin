package net.reldo.taskstracker.tasktypes.combattask;

import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.Util;
import net.reldo.taskstracker.panel.TaskPanel;
import java.awt.image.BufferedImage;
import javax.swing.JPopupMenu;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class CombatTaskPanel extends TaskPanel
{
	public CombatTaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, CombatTask task)
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
		CombatTask task = (CombatTask) this.task;
		String text = Util.wrapWithBold(task.getName()) + Util.HTML_LINE_BREAK +
			task.getTier() + Util.HTML_LINE_BREAK +
			task.getMonster() + Util.HTML_LINE_BREAK +
			task.getDescription();

		text = Util.wrapWithWrappingParagraph(text, 200);

		return Util.wrapWithHtml(text);
	}

	@Override
	public BufferedImage getIcon()
	{
		CombatTaskTier tier = CombatTaskTier.getTierByName(task.getTier());
		if (tier == null)
		{
			return null;
		}

		return spriteManager.getSprite(tier.spriteId, 0);
	}

	//@todo decouple this from League 3. This should be a general filter that can be added to any task type with skill requirements.
	@Override
	protected boolean meetsFilterCriteria()
	{
		TasksTrackerConfig config = plugin.getConfig();

		CombatTask task = (CombatTask) this.task;

		String tierFilter = config.tierFilter();

		if (!tierFilter.contains("f-" + task.getTier().toLowerCase())) // prefix included to cover case where one key name is contained in another (e.g. "Master" -> "Grandmaster")
		{
			return false;
		}

		return super.meetsFilterCriteria();
	}
}
