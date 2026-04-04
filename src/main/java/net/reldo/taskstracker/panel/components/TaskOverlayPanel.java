package net.reldo.taskstracker.panel.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.swing.JComponent;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.CustomItemPanel;
import net.reldo.taskstracker.panel.TaskPanel;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;

public class TaskOverlayPanel extends OverlayPanel
{
	private final TasksTrackerPlugin plugin;

	@Inject
	private TaskOverlayPanel(TasksTrackerPlugin plugin)
	{
		this.plugin = plugin;
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPriority(PRIORITY_HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.getConfig().showOverlay())
		{
			return super.render(graphics);
		}

		JComponent priorityPanel = plugin.getPriorityPanel();

		if (priorityPanel == null)
		{
			return null;
		}

		if (priorityPanel instanceof TaskPanel)
		{
			((TaskPanel) priorityPanel).buildOverlayText(graphics, panelComponent);
		}
		else if (priorityPanel instanceof CustomItemPanel)
		{
			((CustomItemPanel) priorityPanel).buildOverlayText(graphics, panelComponent);
		}

		return super.render(graphics);
	}
}
