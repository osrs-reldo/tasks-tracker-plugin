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
	private ConfigManager configManager;
	private TasksTrackerPlugin plugin;
	private final ComboItem[] items;
	private JComboBox<ComboItem> dropdown;

	public DynamicDropdownFilterPanel(TasksTrackerPlugin plugin, FilterConfig filterConfig, TaskType taskType, ComboItem[] items)
	{
		this.configManager = plugin.getConfigManager();
		this.plugin = plugin;
		this.filterConfig = filterConfig;
		this.items = items;
		this.configKey = taskType.getConfigPrefix() + filterConfig.getConfigKey();

		setLayout(new GridLayout(1,2));
		setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
		setBorder(new EmptyBorder(5, 10, 5, 10));
	}

	private JComboBox<ComboItem> makeDropdownPanel()
	{
		JComboBox<ComboItem> dropdown = new JComboBox<>(items);
		dropdown.setFont(FontManager.getRunescapeSmallFont());
		dropdown.setAlignmentX(LEFT_ALIGNMENT);
		dropdown.setSelectedItem(items[0]);
		dropdown.addActionListener(e -> {
			ComboItem selection = dropdown.getItemAt(dropdown.getSelectedIndex());
			updateFilterConfig();
			plugin.refresh();
			log.debug("selected: {} {}", selection.getLabel(), selection.getValue());
		});

		return dropdown;
	}

	private JLabel makeLabel()
	{
		JLabel label = new JLabel(filterConfig.getLabel() + ":");
		label.setFont(FontManager.getRunescapeSmallFont());
		return label;
	}

	protected void updateFilterConfig()
	{
		log.debug("updateFilterConfig {}, {}, {}", TasksTrackerPlugin.CONFIG_GROUP_NAME, configKey, dropdown.getItemAt(dropdown.getSelectedIndex()).getValue());
		configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, configKey, dropdown.getItemAt(dropdown.getSelectedIndex()).getValue());
	}

	public void redraw()
	{
		assert SwingUtilities.isEventDispatchThread();

		removeAll();

		dropdown = makeDropdownPanel();

		add(makeLabel());
		add(dropdown);

		updateFilterConfig();

		validate();
		repaint();
	}
}
