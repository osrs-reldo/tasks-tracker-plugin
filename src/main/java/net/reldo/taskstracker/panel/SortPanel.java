package net.reldo.taskstracker.panel;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.panel.components.MultiToggleButton;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class SortPanel extends FixedWidthPanel
{
	private static final String ROUTE_OPTION = "Route";
	private static final String ROUTE_INFO_TOOLTIP =
		"<html>Route Mode orders tasks by sections defined in the selected route.<br>" +
		"Import routes from clipboard or create one from your current task order<br>" +
		"using the manage button (...).<br><br>" +
		"Route Designer: coming soon</html>";

	private final TasksTrackerConfig config;
	private final TaskService taskService;
	private final TaskListPanel taskListPanel;
	private final ConfigManager configManager;
	private JComboBox<String> sortDropdown;
	private MultiToggleButton directionButton;
	private JLabel routeInfoLabel;

	private Runnable sortChangeCallback;
	private boolean isSuppressingActionListener = false;

	public SortPanel(ConfigManager configManager, TasksTrackerConfig config, TaskService taskService, TaskListPanel taskListPanel)
	{
		this.config = config;
		this.configManager = configManager;
		this.taskService = taskService;
		this.taskListPanel = taskListPanel;

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setAlignmentX(LEFT_ALIGNMENT);
	}

	public void setSortChangeCallback(Runnable callback)
	{
		this.sortChangeCallback = callback;
	}

	public void redraw()
	{
		removeAll();

		List<String> criteriaList = taskService.getSortedIndexes().keySet().stream()
			.sorted()
			.map((str) -> str.substring(0, 1).toUpperCase() + str.substring(1))
			.collect(Collectors.toList());
		criteriaList.add(0, "Default");
		criteriaList.add(1, ROUTE_OPTION);

		String[] criteriaArray = criteriaList.toArray(new String[0]);
		sortDropdown = new JComboBox<>(criteriaArray);
		sortDropdown.setAlignmentX(LEFT_ALIGNMENT);

		// Restore saved sort criteria from per-tab config (fallback to global)
		String tabId = config.taskListTab().configID;
		String savedCriteria = configManager.getConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tabId + "SortCriteria");
		if (savedCriteria == null)
		{
			savedCriteria = config.sortCriteria();
		}
		int selectedIndex = 0;
		for (int i = 0; i < criteriaArray.length; i++)
		{
			if (criteriaArray[i].equalsIgnoreCase(savedCriteria))
			{
				selectedIndex = i;
				break;
			}
		}
		sortDropdown.setSelectedIndex(selectedIndex);

		sortDropdown.addActionListener(e -> {
			if (isSuppressingActionListener)
			{
				return;
			}
			updateConfig();
			updateRouteInfoVisibility();
			SwingUtilities.invokeLater(taskListPanel::redraw);
			if (sortChangeCallback != null)
			{
				sortChangeCallback.run();
			}
		});
		sortDropdown.setFocusable(false);

		directionButton = new MultiToggleButton(2);
		SwingUtil.removeButtonDecorations(directionButton);
		directionButton.setIcons(new Icon[]{Icons.ASCENDING_ICON, Icons.DESCENDING_ICON});
		directionButton.setToolTips(new String[]{"Ascending", "Descending"});
		directionButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
		directionButton.setStateChangedAction(e -> {
			updateConfig();
			SwingUtilities.invokeLater(taskListPanel::redraw);
		});

		routeInfoLabel = new JLabel("i");
		routeInfoLabel.setFont(FontManager.getDefaultBoldFont());
		routeInfoLabel.setToolTipText(ROUTE_INFO_TOOLTIP);
		routeInfoLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		routeInfoLabel.setPreferredSize(new Dimension(24, 24));
		routeInfoLabel.setHorizontalAlignment(JLabel.CENTER);
		routeInfoLabel.setVisible(false);

		add(sortDropdown);
		add(directionButton);
		add(routeInfoLabel);
		updateRouteInfoVisibility();
	}

	private void updateRouteInfoVisibility()
	{
		boolean isRoute = isRouteMode();
		directionButton.setVisible(!isRoute);
		routeInfoLabel.setVisible(isRoute);
	}

	/**
	 * Refreshes the sort dropdown and direction button from the current tab's config
	 * without rebuilding all components. Called on tab change.
	 */
	public void refreshFromConfig()
	{
		if (sortDropdown == null)
		{
			return;
		}

		String tabId = config.taskListTab().configID;
		String savedCriteria = configManager.getConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tabId + "SortCriteria");
		if (savedCriteria == null)
		{
			savedCriteria = "default";
		}

		// Suppress action listener while we programmatically update the dropdown
		isSuppressingActionListener = true;
		for (int i = 0; i < sortDropdown.getItemCount(); i++)
		{
			if (sortDropdown.getItemAt(i).equalsIgnoreCase(savedCriteria))
			{
				sortDropdown.setSelectedIndex(i);
				break;
			}
		}

		// Restore direction
		String savedDirection = configManager.getConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tabId + "SortDirection");
		if (savedDirection != null)
		{
			try
			{
				ConfigValues.SortDirections dir = ConfigValues.SortDirections.valueOf(savedDirection);
				directionButton.setState(dir.ordinal());
			}
			catch (IllegalArgumentException ignored)
			{
			}
		}
		isSuppressingActionListener = false;

		// Write the restored values to global config so TaskListPanel reads the right values
		updateConfig();
		updateRouteInfoVisibility();
	}

	public boolean isRouteMode()
	{
		if (sortDropdown == null)
		{
			return false;
		}
		String selected = (String) sortDropdown.getSelectedItem();
		return ROUTE_OPTION.equals(selected);
	}

	protected void updateConfig()
	{
		String criteria = sortDropdown.getItemAt(sortDropdown.getSelectedIndex()).toLowerCase();
		ConfigValues.SortDirections direction = ConfigValues.SortDirections.values()[directionButton.getState()];
		String tabId = config.taskListTab().configID;

		log.debug("updateConfig {}, {}, {}", TasksTrackerPlugin.CONFIG_GROUP_NAME, tabId + "SortCriteria", criteria);

		// Save per-tab sort config
		configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tabId + "SortCriteria", criteria);
		configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tabId + "SortDirection", direction);

		// Also write global keys (TaskListPanel reads these for rendering)
		configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "sortCriteria", criteria);
		configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "sortDirection", direction);
	}
}
