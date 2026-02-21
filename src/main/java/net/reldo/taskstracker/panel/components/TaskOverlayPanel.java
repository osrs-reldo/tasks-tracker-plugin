package net.reldo.taskstracker.panel.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.ComponentConstants;
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

	private void buildOverlayText(Graphics2D graphics, TaskFromStruct task)
	{

		final FontMetrics fontMetrics = graphics.getFontMetrics();
		int panelWidth = Math.max(ComponentConstants.STANDARD_WIDTH, fontMetrics.stringWidth(task.getName()) +
			ComponentConstants.STANDARD_BORDER + ComponentConstants.STANDARD_BORDER);

		panelComponent.setPreferredSize(new Dimension(panelWidth, 0));
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(task.getName())
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left(task.getDescription())
			.build());

		if (task.getNote() != null && !task.getNote().isEmpty())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Note:")
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left(task.getNote())
				.leftFont(FontManager.getRunescapeFont().deriveFont(Font.ITALIC))
				.build());
		}
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

		buildOverlayText(graphics, task);

		return super.render(graphics);
	}
}
