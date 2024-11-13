package net.reldo.taskstracker.panel.filters;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.FilterCustomItem;
import net.reldo.taskstracker.data.task.TaskType;
import net.runelite.client.ui.ColorScheme;

public class DynamicButtonFilterPanel extends FilterButtonPanel
{
	private final FilterConfig filterConfig;
	private TaskType taskType;

	public DynamicButtonFilterPanel(TasksTrackerPlugin plugin, FilterConfig filterConfig, TaskType taskType)
	{
		super(plugin, filterConfig.getLabel());
		this.filterConfig = filterConfig;
		this.taskType = taskType;
		this.configKey = taskType.getFilterConfigPrefix() + filterConfig.getConfigKey();

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(10, 10, 10, 10));

		redraw();
	}

	@Override
	protected JPanel makeButtonPanel()
	{
		// Panel that holds tier icons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		LinkedHashMap<String, BufferedImage> buttonImages = getIconImages();
		LinkedHashMap<String, String> buttonTooltips = getTooltips();

		buttonPanel.setLayout(new GridLayout(buttonImages.size() / 3, 3));

		// For each difficulty tier create a button and add it to the UI
		buttonImages.forEach((key, image) -> {
			String tooltip = buttonTooltips.get(key);
			JToggleButton button = makeButton(tooltip, image);
			buttons.put(key, button);
			buttonPanel.add(button);
		});

		return buttonPanel;
	}

	@Override
	protected LinkedHashMap<String, BufferedImage> getIconImages()
	{
		LinkedHashMap<String, BufferedImage> images = new LinkedHashMap<>();
		for (FilterCustomItem customItem : filterConfig.getCustomItems())
		{
			String key = customItem.getValue().toString();
			images.put(key, taskType.getSpritesById().get(customItem.getSpriteId()));
		}
		return images;
	}

	private LinkedHashMap<String, String> getTooltips()
	{
		LinkedHashMap<String, String> tooltips = new LinkedHashMap<>();
		for (FilterCustomItem customItem : filterConfig.getCustomItems())
		{
			String key = customItem.getValue().toString();
			tooltips.put(key, customItem.getTooltip());
		}
		return tooltips;
	}
}
