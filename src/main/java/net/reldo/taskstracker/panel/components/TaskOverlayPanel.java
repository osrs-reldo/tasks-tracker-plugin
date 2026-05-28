package net.reldo.taskstracker.panel.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.swing.JComponent;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.CustomItemPanel;
import net.reldo.taskstracker.panel.TaskPanel;
import net.reldo.taskstracker.panel.flyweight.FlyweightTaskPanelAdapter;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.PanelComponent;

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

		boolean supportedPanel = priorityPanel instanceof TaskPanel || priorityPanel instanceof CustomItemPanel;
		if (!supportedPanel)
		{
			panelComponent.getChildren().clear();
			return super.render(graphics);
		}

		PanelComponent renderer = FlyweightTaskPanelAdapter.acquireRenderer();
		try
		{
			panelComponent.getChildren().clear();

			if (priorityPanel instanceof TaskPanel)
			{
				((TaskPanel) priorityPanel).buildOverlayText(renderer);
			}
			else
			{
				((CustomItemPanel) priorityPanel).buildOverlayText(renderer);
			}

			panelComponent.setBackgroundColor(renderer.getBackgroundColor());
			panelComponent.setPreferredSize(renderer.getPreferredSize());
			panelComponent.getChildren().clear();
			panelComponent.getChildren().addAll(renderer.getChildren());
		}
		finally
		{
			FlyweightTaskPanelAdapter.releaseRenderer(renderer);
		}

		return super.render(graphics);
	}
}
