package net.reldo.taskstracker.panel.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

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

	private void buildOverlayText(TaskFromStruct task)
	{

		panelComponent.getChildren().add(TitleComponent.builder()
			.text(task.getName())
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left(task.getDescription())
			.build());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.getConfig().showOverlay())
		{
			return super.render(graphics);
		}

		TaskFromStruct task = plugin.getPriorityTask();

		if (task == null)
		{
			return null;
		}

		buildOverlayText(task);

		return super.render(graphics);
	}
}
