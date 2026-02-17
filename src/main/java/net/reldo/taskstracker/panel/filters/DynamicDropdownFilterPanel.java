package net.reldo.taskstracker.panel.filters;

import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.task.TaskType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

@Slf4j
public class DynamicDropdownFilterPanel extends FilterPanel
{
	private final String configKey;
	private final FilterConfig filterConfig;
	private final ConfigManager configManager;
	private final TasksTrackerPlugin plugin;
	private final ComboItem[] items;
	private JComboBox<ComboItem> dropdown;

	public DynamicDropdownFilterPanel(TasksTrackerPlugin plugin, FilterConfig filterConfig, TaskType taskType, ComboItem[] items)
	{
		this.configManager = plugin.getConfigManager();
		this.plugin = plugin;
		this.filterConfig = filterConfig;
		this.items = items;
		this.configKey = taskType.getFilterConfigPrefix() + filterConfig.getConfigKey();

		setLayout(new GridLayout(1, 2));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(5, 10, 5, 10));
	}

	private JComboBox<ComboItem> makeDropdownPanel()
	{
		JComboBox<ComboItem> dropdown = new JComboBox<>(items);
		dropdown.setFont(FontManager.getRunescapeSmallFont());
		dropdown.setAlignmentX(LEFT_ALIGNMENT);
		dropdown.setSelectedItem(items[0]);
		dropdown.setFocusable(false);
		dropdown.setBackground(ColorScheme.DARK_GRAY_COLOR.brighter());
		dropdown.addActionListener(e -> {
			ComboItem selection = dropdown.getItemAt(dropdown.getSelectedIndex());
			updateFilterConfig();
			plugin.refreshAllTasks();
			log.debug("selected: {} {}", selection.getLabel(), selection.getValue());
		});

		return dropdown;
	}

	private JLabel makeLabel()
	{
		JLabel label = new JLabel(filterConfig.getLabel() + ":");
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		return label;
	}

	protected void updateFilterConfig()
	{
		log.debug("updateFilterConfig {}, {}, {}", TasksTrackerPlugin.CONFIG_GROUP_NAME, configKey, dropdown.getItemAt(dropdown.getSelectedIndex()).getValue());
		configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, configKey, dropdown.getItemAt(dropdown.getSelectedIndex()).getValue());
	}

	public void redraw()
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			removeAll();

			dropdown = makeDropdownPanel();

			add(makeLabel());
			add(dropdown);

			updateFilterConfig();

			validate();
			repaint();
		}
		else
		{
			log.error("Dropdown filter panel redraw failed - not event dispatch thread.");
		}
	}
}
