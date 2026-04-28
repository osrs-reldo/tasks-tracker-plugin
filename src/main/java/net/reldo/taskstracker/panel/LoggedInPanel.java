package net.reldo.taskstracker.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.FilterType;
import net.reldo.taskstracker.data.jsondatastore.types.PremadeRouteEntry;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.route.RouteManager;
import net.reldo.taskstracker.data.task.ITaskType;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.data.task.filters.RegexTextMatcher;
import net.reldo.taskstracker.data.task.filters.TextMatcher;
import net.reldo.taskstracker.data.task.filters.TextMatcherFactory;
import net.reldo.taskstracker.panel.components.FilterLockTabMenuItem;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.panel.components.RouteSelector;
import net.reldo.taskstracker.panel.components.SearchBox;
import net.reldo.taskstracker.panel.components.TriToggleButton;
import net.reldo.taskstracker.panel.filters.ComboItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class LoggedInPanel extends JPanel
{

	public TaskListPanel taskListPanel;
	private JComboBox<ComboItem<ITaskType>> taskTypeDropdown;

	private final TaskService taskService;
	private final TasksTrackerPlugin plugin;
	private final TasksTrackerConfig config;
	private final RouteManager routeManager;

	// Filter buttons
	private final TriToggleButton completedFilterBtn = new TriToggleButton();
	private final TriToggleButton trackedFilterBtn = new TriToggleButton();
	private final TriToggleButton ignoredFilterBtn = new TriToggleButton();
	private final JPanel titlePanel = new JPanel();

	// Task list tabs
	private final JPanel tabPane = new JPanel();

	// Route selector
	private RouteSelector routeSelector;

	// sub-filter panel
	private SubFilterPanel subFilterPanel;
	private JPanel subFilterWrapper;
	private SortPanel sortPanel;
	private JPanel searchPanel;
	private JPanel randomButtonPanel;
	private final JToggleButton collapseBtn = new JToggleButton();
	private JToggleButton tabOne;
	private JToggleButton tabTwo;
	private JToggleButton tabThree;
	private JButton routeButton;

	public LoggedInPanel(TasksTrackerPlugin plugin, TasksTrackerConfig config, TaskService taskService, RouteManager routeManager)
	{
		super(false);
		this.plugin = plugin;
		this.taskService = taskService;
		this.config = config;
		this.routeManager = routeManager;

		createPanel();
	}

	@Override
	public Dimension getPreferredSize()
	{
		return getParent().getSize();
	}

	public void drawNewTaskType()
	{
		// taskTypeDropdown may become de-synced after profile change
		String selectedTaskTypeJsonName = taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex()).getValue().getTaskJsonName();
		if (!selectedTaskTypeJsonName.equals(config.taskTypeJsonName()))
		{
			log.debug("Task type dropdown de-synced, attempting to find current task type");
			for (int i = 0; i < taskTypeDropdown.getItemCount(); i++)
			{
				ComboItem<ITaskType> item = taskTypeDropdown.getItemAt(i);
				if (item.getValue().getTaskJsonName().equals(config.taskTypeJsonName()))
				{
					log.debug("Current task type found, setting selected task type");
					taskTypeDropdown.setSelectedIndex(i);
					break;
				}
			}
		}

		subFilterPanel.redraw();
		sortPanel.redraw();
		updateCollapseButtonText();
		randomButtonPanel.setVisible(!plugin.isRouteMode() && config.showRandomTaskButton());

		refreshRouteSelector();
		taskListPanel.drawNewTaskType();
		refreshFilterButtonsFromConfig(config.taskListTab());
	}

	public void redraw()
	{
		subFilterPanel.redraw();
		sortPanel.redraw();
		updateCollapseButtonText();
		randomButtonPanel.setVisible(!plugin.isRouteMode() && config.showRandomTaskButton());

		taskListPanel.redraw();
	}

	public void redrawTaskList()
	{
		taskListPanel.redraw();
	}

	public void refreshAllTasks()
	{
		updateCollapseButtonText();
		refreshTabNames();
		taskListPanel.refreshAllTasks();
	}

	private void createPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		taskListPanel = new TaskListPanel(plugin, taskService);

		add(getNorthPanel(), BorderLayout.NORTH);
		add(getCenterPanel(), BorderLayout.CENTER);
		add(getSouthPanel(), BorderLayout.SOUTH);
	}

	private JPanel getCenterPanel()
	{
		// wrapper for the task list and tab buttons
		final JPanel taskListPanel = new JPanel(new BorderLayout());
		taskListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		taskListPanel.setBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR));
		taskListPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);

		tabPane.setLayout(new BoxLayout(tabPane, BoxLayout.X_AXIS));
		tabPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		tabPane.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 24));

		tabOne = tabButton(config.tab1Name(), ConfigValues.TaskListTabs.TAB_ONE);
		tabTwo = tabButton(config.tab2Name(), ConfigValues.TaskListTabs.TAB_TWO);
		tabThree = tabButton(config.tab3Name(), ConfigValues.TaskListTabs.TAB_THREE);

		ButtonGroup tabGroup = new ButtonGroup();

		tabGroup.add(tabOne);
		tabGroup.add(tabTwo);
		tabGroup.add(tabThree);

		tabPane.add(Box.createHorizontalGlue());
		tabPane.add(tabOne);
		tabPane.add(Box.createHorizontalGlue());
		tabPane.add(tabTwo);
		tabPane.add(Box.createHorizontalGlue());
		tabPane.add(tabThree);
		tabPane.add(Box.createHorizontalGlue());

		taskListPanel.add(tabPane, BorderLayout.NORTH);
		taskListPanel.add(this.taskListPanel, BorderLayout.CENTER);

		return taskListPanel;
	}

	public void tabChanged(ConfigValues.TaskListTabs newTab)
	{
		if (newTab != null)
		{
			plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskListTab", newTab);
			refreshFilterButtonsFromConfig(newTab);
			sortPanel.refreshFromConfig();
			onSortChanged(); // handles refreshRouteSelector() when in route mode, clears route when not
			plugin.redrawTaskList();
		}
	}

	// TODO reduce duplication
	public void refreshFilterButtonsFromConfig(ConfigValues.TaskListTabs tab)
	{
		JToggleButton tabButton = null;
		switch (tab)
		{
			case TAB_ONE:
				tabButton = tabOne;
				break;
			case TAB_TWO:
				tabButton = tabTwo;
				break;
			case TAB_THREE:
				tabButton = tabThree;
				break;
		}
		if (tabButton != null && !tabButton.isSelected())
		{
			tabButton.setSelected(true);
		}

		if (plugin.isRouteMode())
		{
			trackedFilterBtn.setState(ConfigValues.TrackedFilterValues.TRACKED_AND_UNTRACKED.ordinal());
			trackedFilterBtn.setEnabled(false);
			completedFilterBtn.setState(ConfigValues.CompletedFilterValues.COMPLETE_AND_INCOMPLETE.ordinal());
			completedFilterBtn.setEnabled(false);
			ignoredFilterBtn.setState(ConfigValues.IgnoredFilterValues.IGNORED_AND_NOT_IGNORED.ordinal());
			ignoredFilterBtn.setEnabled(false);
			return;
		}

		switch (tab)
		{
			case TAB_ONE:
				trackedFilterBtn.setState(config.tab1TrackedValue().ordinal());
				trackedFilterBtn.setEnabled(!config.tab1TrackedLock());
				completedFilterBtn.setState(config.tab1CompletedValue().ordinal());
				completedFilterBtn.setEnabled(!config.tab1CompletedLock());
				ignoredFilterBtn.setState(config.tab1IgnoredValue().ordinal());
				ignoredFilterBtn.setEnabled(!config.tab1IgnoredLock());
				actionAllFilterButtonsNoRefresh();
				break;
			case TAB_TWO:
				trackedFilterBtn.setState(config.tab2TrackedValue().ordinal());
				trackedFilterBtn.setEnabled(!config.tab2TrackedLock());
				completedFilterBtn.setState(config.tab2CompletedValue().ordinal());
				completedFilterBtn.setEnabled(!config.tab2CompletedLock());
				ignoredFilterBtn.setState(config.tab2IgnoredValue().ordinal());
				ignoredFilterBtn.setEnabled(!config.tab2IgnoredLock());
				actionAllFilterButtonsNoRefresh();
				break;
			case TAB_THREE:
				trackedFilterBtn.setState(config.tab3TrackedValue().ordinal());
				trackedFilterBtn.setEnabled(!config.tab3TrackedLock());
				completedFilterBtn.setState(config.tab3CompletedValue().ordinal());
				completedFilterBtn.setEnabled(!config.tab3CompletedLock());
				ignoredFilterBtn.setState(config.tab3IgnoredValue().ordinal());
				ignoredFilterBtn.setEnabled(!config.tab3IgnoredLock());
				actionAllFilterButtonsNoRefresh();
				break;
			default:
				break;
		}
	}

	private JToggleButton tabButton(String label, ConfigValues.TaskListTabs tab)
	{
		JToggleButton button = new JToggleButton(label);

		button.setBorder(new EmptyBorder(2, 5, 2, 5));
		button.setBackground(ColorScheme.DARK_GRAY_COLOR);
		button.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		button.addActionListener(e -> tabChanged(tab));

		JPopupMenu popupMenu = new JPopupMenu("Filter Lock Toggles");

		JMenuItem completedLockItem = new FilterLockTabMenuItem("Completed", completedFilterBtn, e -> {
			plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tab.configID + "CompletedLock", completedFilterBtn.isEnabled());
		});
		JMenuItem trackedLockItem = new FilterLockTabMenuItem("Tracked", trackedFilterBtn, e -> {
			plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tab.configID + "TrackedLock", trackedFilterBtn.isEnabled());
		});
		JMenuItem ignoredLockItem = new FilterLockTabMenuItem("Ignored", ignoredFilterBtn, e -> {
			plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tab.configID + "IgnoredLock", ignoredFilterBtn.isEnabled());
		});

		JMenuItem taskOverlayItem = new JMenuItem("Toggle canvas overlay");
		taskOverlayItem.addActionListener(e -> plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "showOverlay", !config.showOverlay()));

		popupMenu.add(completedLockItem);
		popupMenu.add(trackedLockItem);
		popupMenu.add(ignoredLockItem);
		popupMenu.addSeparator();
		popupMenu.add(taskOverlayItem);

		button.addChangeListener(e -> {
			if (completedLockItem.isEnabled() != button.isSelected())
			{
				completedLockItem.setEnabled(button.isSelected());
			}
			if (trackedLockItem.isEnabled() != button.isSelected())
			{
				trackedLockItem.setEnabled(button.isSelected());
			}
			if (ignoredLockItem.isEnabled() != button.isSelected())
			{
				ignoredLockItem.setEnabled(button.isSelected());
			}
		});

		button.setComponentPopupMenu(popupMenu);

		return button;
	}

	private void refreshTabNames()
	{
		String tabName;
		if (!tabOne.getText().equals(config.tab1Name()))
		{
			tabName = config.tab1Name().isBlank() ? "Tab 1" : config.tab1Name();
			tabOne.setText(tabName);
		}
		if (!tabTwo.getText().equals(config.tab2Name()))
		{
			tabName = config.tab2Name().isBlank() ? "Tab 2" : config.tab2Name();
			tabTwo.setText(tabName);
		}
		if (!tabThree.getText().equals(config.tab3Name()))
		{
			tabName = config.tab3Name().isBlank() ? "Tab 3" : config.tab3Name();
			tabThree.setText(tabName);
		}
	}

	private JPanel getSouthPanel()
	{
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(new EmptyBorder(5, 0, 2, 0));

		routeButton = new JButton("Route Mode");
		routeButton.setBorder(new EmptyBorder(5, 5, 5, 5));
		routeButton.setLayout(new BorderLayout(0, PluginPanel.BORDER_OFFSET));
		routeButton.addActionListener(e -> plugin.showRouteTutorial());
		hideRouteModeButton(config.hideRouteModeButton());
		plugin.getSpriteManager().getSpriteAsync(Icons.INFO_ICON, 0, img -> {
			SwingUtilities.invokeLater(() -> {
				routeButton.setIcon(new ImageIcon(img));
			});
		});
		southPanel.add(routeButton, BorderLayout.NORTH);

		JButton importButton = new JButton("Import");
		importButton.setBorder(new EmptyBorder(5, 5, 5, 5));
		importButton.setLayout(new BorderLayout(0, PluginPanel.BORDER_OFFSET));
		importButton.addActionListener(e -> plugin.openImportJsonDialog());
		southPanel.add(importButton, BorderLayout.WEST);

		JButton totalsButton = new JButton("Totals");
		totalsButton.setBorder(new EmptyBorder(5, 5, 5, 5));
		totalsButton.setLayout(new BorderLayout(0, PluginPanel.BORDER_OFFSET));
		totalsButton.addActionListener(e -> plugin.sendTotalsToChat());
		southPanel.add(totalsButton, BorderLayout.CENTER);

		JButton exportButton = new JButton("Export");
		exportButton.setBorder(new EmptyBorder(5, 5, 5, 5));
		exportButton.setLayout(new BorderLayout(0, PluginPanel.BORDER_OFFSET));
		exportButton.addActionListener(e -> plugin.openExportJsonDialog());
		southPanel.add(exportButton, BorderLayout.EAST);

		return southPanel;
	}

	private JPanel getNorthPanel()
	{
		JPanel northPanel = new JPanel();
		BoxLayout layout = new BoxLayout(northPanel, BoxLayout.Y_AXIS);
		northPanel.setLayout(layout);
		northPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		taskTypeDropdown = new JComboBox<>();
		taskTypeDropdown.setAlignmentX(LEFT_ALIGNMENT);
		taskTypeDropdown.setFocusable(false);
		initTaskTypeDropdownAsync();

		// Wrapper for collapsible sub-filter menu
		subFilterWrapper = new JPanel();
		subFilterWrapper.setLayout(new BorderLayout());
		subFilterWrapper.setBorder(new MatteBorder(1, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR));
		subFilterWrapper.setAlignmentX(LEFT_ALIGNMENT);
		subFilterWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// collapse button
		SwingUtil.removeButtonDecorations(collapseBtn);
		collapseBtn.setIcon(Icons.MENU_COLLAPSED_ICON);
		collapseBtn.setSelectedIcon(Icons.MENU_EXPANDED_ICON);
		collapseBtn.setRolloverIcon(Icons.MENU_ICON_HOVER);
		SwingUtil.addModalTooltip(collapseBtn, "Collapse filters", "Expand filters");
		collapseBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		collapseBtn.setAlignmentX(LEFT_ALIGNMENT);
		collapseBtn.setUI(new BasicButtonUI()); // substance breaks the layout
		collapseBtn.addActionListener(ev -> subFilterPanel.setVisible(!subFilterPanel.isVisible()));
		collapseBtn.setHorizontalTextPosition(JButton.CENTER);
		collapseBtn.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		collapseBtn.setFont(FontManager.getRunescapeSmallFont());
		collapseBtn.setBorder(new EmptyBorder(2, 0, 2, 0));
		collapseBtn.setFocusable(false);

		// panel to hold sub-filters
		subFilterPanel = new SubFilterPanel(plugin, taskService);

		subFilterWrapper.add(collapseBtn, BorderLayout.NORTH);
		subFilterWrapper.add(subFilterPanel, BorderLayout.CENTER);

		sortPanel = new SortPanel(plugin.getConfigManager(), config, taskService, taskListPanel);
		sortPanel.setSortChangeCallback(this::onSortChanged);

		// Route selector (visible only when sort is "Route")
		routeSelector = new RouteSelector();
		routeSelector.setAlignmentX(LEFT_ALIGNMENT);

		routeSelector.addRouteChangeListener(e -> {
			String selectedId = routeSelector.getSelectedRouteId();
			ConfigValues.TaskListTabs currentTab = config.taskListTab();
			String taskType = taskService.getCurrentTaskType().getTaskJsonName();

			// Save selection
			plugin.getTrackerGlobalConfigStore().saveActiveRouteId(currentTab, taskType, selectedId);

			// Update TaskService
			CustomRoute route = selectedId != null
				? plugin.getTrackerGlobalConfigStore().getActiveRoute(currentTab, taskType)
				: null;
			taskService.setActiveRoute(currentTab, route);

			// Redraw
			taskListPanel.redraw();
		});

		routeSelector.addManageListener(e -> showRouteManagementMenu());

		searchPanel = getSearchPanel();

		randomButtonPanel = new FixedWidthPanel();
		randomButtonPanel.setLayout(new BorderLayout());
		randomButtonPanel.setAlignmentX(LEFT_ALIGNMENT);
		JButton randomButton = new JButton("Pin Random Task");
		randomButton.setToolTipText("Task will be picked from the visible list. Use filters to restrict options.");
		randomButton.setForeground(ColorScheme.TEXT_COLOR);
		randomButton.setFocusable(false);
		randomButton.setAlignmentX(LEFT_ALIGNMENT);
		randomButton.addActionListener(e -> taskListPanel.pinRandomTask());
		randomButtonPanel.add(randomButton, BorderLayout.CENTER);

		northPanel.add(getTitleAndButtonPanel());
		northPanel.add(Box.createVerticalStrut(10));
		northPanel.add(taskTypeDropdown);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(searchPanel);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(sortPanel);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(routeSelector);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(subFilterWrapper);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(randomButtonPanel);

		// Route selector and sub-filter visibility based on sort mode
		boolean isRouteMode = plugin.isRouteMode();
		routeSelector.setVisible(isRouteMode);
		subFilterWrapper.setVisible(!isRouteMode);
		searchPanel.setVisible(!isRouteMode);
		randomButtonPanel.setVisible(!isRouteMode && config.showRandomTaskButton());

		return northPanel;
	}

	private JPanel getTitleAndButtonPanel()
	{
		titlePanel.setLayout(new BorderLayout());
		titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		titlePanel.setPreferredSize(new Dimension(0, 30));
		titlePanel.setBorder(new EmptyBorder(5, 5, 5, 10));

		JLabel title = new JLabel("Tasks Tracker");
		title.setHorizontalAlignment(SwingConstants.LEFT);
		title.setForeground(Color.WHITE);

		JPopupMenu reloadPluginPopup = new JPopupMenu();
		reloadPluginPopup.setBorder(new EmptyBorder(5, 5, 5, 5));
		JMenuItem reloadPluginMenuItem = new JMenuItem("Reload plugin");
		reloadPluginMenuItem.addActionListener(e -> plugin.reloadTaskType());
		reloadPluginPopup.add(reloadPluginMenuItem);
		title.setComponentPopupMenu(reloadPluginPopup);

		// Filter button bar
		final JPanel viewControls = new JPanel();
		viewControls.setLayout(new BoxLayout(viewControls, BoxLayout.X_AXIS));
		viewControls.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Completed tasks filter button
		SwingUtil.removeButtonDecorations(completedFilterBtn);
		completedFilterBtn.setIcons(Icons.COMPLETE_INCOMPLETE_ICON, Icons.COMPLETE_ONLY_ICON, Icons.INCOMPLETE_ONLY_ICON);
		completedFilterBtn.setToolTips("All tasks", "Completed tasks only", "Incomplete tasks only");
		completedFilterBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		completedFilterBtn.setStateChangedAction(e -> filterButtonAction("completed"));
		completedFilterBtn.popupMenuEnabled(true);
		completedFilterBtn.setState(config.completedFilter().ordinal());

		viewControls.add(completedFilterBtn);

		// Tracked tasks filter button
		SwingUtil.removeButtonDecorations(trackedFilterBtn);
		trackedFilterBtn.setIcons(Icons.TRACKED_UNTRACKED_ICON, Icons.TRACKED_ONLY_ICON, Icons.UNTRACKED_ONLY_ICON);
		trackedFilterBtn.setToolTips("All tasks", "Tracked tasks only", "Untracked tasks only");
		trackedFilterBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		trackedFilterBtn.setStateChangedAction(e -> filterButtonAction("tracked"));
		trackedFilterBtn.popupMenuEnabled(true);
		trackedFilterBtn.setState(config.trackedFilter().ordinal());

		viewControls.add(trackedFilterBtn);

		// Ignored tasks filter button
		SwingUtil.removeButtonDecorations(ignoredFilterBtn);
		ignoredFilterBtn.setIcons(Icons.UNIGNORED_ONLY_ICON, Icons.IGNORED_UNIGNORED_ICON, Icons.IGNORED_ONLY_ICON);
		ignoredFilterBtn.setToolTips("Hide ignored tasks", "All tasks", "Ignored tasks only");
		ignoredFilterBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		ignoredFilterBtn.setStateChangedAction(e -> filterButtonAction("ignored"));
		ignoredFilterBtn.popupMenuEnabled(true);
		ignoredFilterBtn.setState(config.ignoredFilter().ordinal());

		viewControls.add(ignoredFilterBtn);

		titlePanel.add(viewControls, BorderLayout.EAST);
		titlePanel.add(title, BorderLayout.WEST);
		titlePanel.setAlignmentX(LEFT_ALIGNMENT);

		return titlePanel;
	}

	public void refreshRouteSelector()
	{
		if (taskService.getCurrentTaskType() == null)
		{
			return;
		}

		ConfigValues.TaskListTabs currentTab = config.taskListTab();
		String taskType = taskService.getCurrentTaskType().getTaskJsonName();

		List<CustomRoute> routes = plugin.getTrackerGlobalConfigStore().loadRoutes(taskType);
		String activeId = plugin.getTrackerGlobalConfigStore().loadActiveRouteId(currentTab, taskType);

		routeSelector.setRoutes(routes, activeId);

		// Only set active route in TaskService when in route sort mode
		if (sortPanel.isRouteMode())
		{
			CustomRoute activeRoute = activeId != null
				? plugin.getTrackerGlobalConfigStore().getActiveRoute(currentTab, taskType)
				: null;
			taskService.setActiveRoute(currentTab, activeRoute);
		}
	}

	public void forceRouteMode()
	{
		String tabId = config.taskListTab().configID;
		plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tabId + "SortCriteria", SortPanel.ROUTE_OPTION);
		sortPanel.refreshFromConfig(); // Set config and refresh to avoid redundant task list redraws
		onSortChanged(); // refreshFromConfig() suppresses callback
		taskListPanel.redraw();
	}

	private void refreshAfterRouteChange()
	{
		refreshRouteSelector();
		taskListPanel.redraw();
	}

	private void onSortChanged()
	{
		boolean isRouteMode = sortPanel.isRouteMode();
		routeSelector.setVisible(isRouteMode);
		subFilterWrapper.setVisible(!isRouteMode);
		searchPanel.setVisible(!isRouteMode);
		randomButtonPanel.setVisible(!isRouteMode && config.showRandomTaskButton());

		if (isRouteMode)
		{
			refreshRouteSelector();
		}
		else
		{
			// Clear active route so tasks aren't filtered
			ConfigValues.TaskListTabs currentTab = config.taskListTab();
			taskService.clearActiveRoute(currentTab);
			plugin.getShortestPathService().clearGps();
		}
	}

	private void showRouteManagementMenu()
	{
		JPopupMenu menu = new JPopupMenu();

		JMenuItem importItem = new JMenuItem("Import Route from Clipboard");
		importItem.addActionListener(e -> {
			if (routeManager.importRouteFromClipboard())
			{
				SwingUtilities.invokeLater(this::refreshAfterRouteChange);
			}
		});

		JMenuItem exportItem = new JMenuItem("Export Active Route to Clipboard");
		exportItem.addActionListener(e -> routeManager.exportActiveRoute());
		exportItem.setEnabled(routeSelector.getSelectedRouteId() != null);

		JMenuItem createItem = new JMenuItem("Create New Route (Coming soon)");
		createItem.addActionListener(e -> {
			if (routeManager.createRouteFromCurrentOrder(taskListPanel.getVisibleTaskIds()))
			{
				// force plugin into edit mode
				plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "routeInEditMode", taskService.getActiveRoute().getId());
				SwingUtilities.invokeLater(this::refreshAfterRouteChange);
			}
		});
		createItem.setEnabled(false);

		JMenuItem deleteItem = new JMenuItem("Delete Active Route");
		deleteItem.addActionListener(e -> {
			if (routeManager.deleteActiveRoute())
			{
				SwingUtilities.invokeLater(this::refreshAfterRouteChange);
			}
		});
		deleteItem.setEnabled(routeSelector.getSelectedRouteId() != null);

		JMenuItem collapseSectionsItem = new JMenuItem("Collapse All Sections");
		collapseSectionsItem.addActionListener(e -> {
			taskListPanel.collapseAllSections();
		});
		collapseSectionsItem.setEnabled(routeSelector.getSelectedRouteId() != null);

		JMenuItem expandSectionsItem = new JMenuItem("Expand All Sections");
		expandSectionsItem.addActionListener(e -> {
			taskListPanel.expandAllSections();
		});
		expandSectionsItem.setEnabled(routeSelector.getSelectedRouteId() != null);

		// Route management menu items disabled while route editor in development
		JMenuItem editorItem = new JMenuItem("Route Editor (Coming soon)");
		editorItem.setEnabled(false);

		JMenuItem browseItem = new JMenuItem("Browse Premade Routes");
		browseItem.addActionListener(e -> {
			String currentTaskType = taskService.getCurrentTaskType().getTaskJsonName();
			new Thread(() -> {
				try
				{
					List<PremadeRouteEntry> manifest = routeManager.fetchPremadeRouteManifest();
					List<PremadeRouteEntry> filtered = manifest.stream()
						.filter(entry -> currentTaskType.equals(entry.getTaskType()))
						.collect(java.util.stream.Collectors.toList());

					SwingUtilities.invokeLater(() -> {
						if (filtered.isEmpty())
						{
							JOptionPane.showMessageDialog(
								this,
								"No premade routes available for " + currentTaskType + ".",
								"Premade Routes",
								JOptionPane.INFORMATION_MESSAGE
							);
							return;
						}

						PremadeRouteEntry selected = PremadeRouteBrowserDialog.show(this, filtered);
						if (selected != null)
						{
							// Fetch route on background thread, then import on EDT
							new Thread(() -> {
								try
								{
									CustomRoute route = routeManager.fetchPremadeRoute(selected.getFilename());
									SwingUtilities.invokeLater(() -> {
										if (routeManager.importRoute(route))
										{
											refreshAfterRouteChange();
										}
									});
								}
								catch (Exception ex2)
								{
									log.error("Failed to download premade route", ex2);
									SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
										this,
										"Failed to download route: " + ex2.getMessage(),
										"Error",
										JOptionPane.ERROR_MESSAGE
									));
								}
							}, "PremadeRouteDownload").start();
						}
					});
				}
				catch (Exception ex)
				{
					log.error("Failed to fetch premade routes", ex);
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
						this,
						"Could not fetch premade routes: " + ex.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE
					));
				}
			}, "PremadeRouteManifestFetch").start();
		});

		menu.add(importItem);
		menu.add(browseItem);
		menu.add(exportItem);
		menu.addSeparator();
		menu.add(createItem);
		menu.add(deleteItem);
		menu.addSeparator();
		menu.add(editorItem);
		menu.addSeparator();
		menu.add(collapseSectionsItem);
		menu.add(expandSectionsItem);

		// Show below the manage button
		menu.show(routeSelector, routeSelector.getWidth() - menu.getPreferredSize().width,
			routeSelector.getHeight());
	}

	private void filterButtonActionNoRefresh(String filter)
	{
		ConfigManager configManager = plugin.getConfigManager();
		int state;
		Enum configValue;
		String tab = "tab" + (config.taskListTab().ordinal() + 1); // tabs are 1-indexed
		String tabFormatFilter = filter.substring(0, 1).toUpperCase() + filter.substring(1).toLowerCase();

		switch (filter)
		{
			case "completed":
				state = completedFilterBtn.getState();
				configValue = ConfigValues.CompletedFilterValues.values()[state];
				break;
			case "tracked":
				state = trackedFilterBtn.getState();
				configValue = ConfigValues.TrackedFilterValues.values()[state];
				break;
			case "ignored":
				state = ignoredFilterBtn.getState();
				configValue = ConfigValues.IgnoredFilterValues.values()[state];
				break;
			default:
				log.debug("Filter button action failed due to unrecognised filter.");
				return;
		}

		// Set task list filter config
		configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, filter + "Filter", configValue);

		// Save button state to config
		if (configManager.getConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tab + tabFormatFilter + "Lock").equals("false"))
		{
			configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, tab + tabFormatFilter + "Value", configValue);
		}
	}

	private void filterButtonAction(String filter)
	{
		filterButtonActionNoRefresh(filter);
		plugin.refreshAllTasks();
	}

	private void actionAllFilterButtonsNoRefresh()
	{
		filterButtonActionNoRefresh("tracked");
		filterButtonActionNoRefresh("ignored");
		filterButtonActionNoRefresh("completed");
	}

	// TODO: unused — remove if no callers are added
	private void actionAllFilterButtons()
	{
		actionAllFilterButtonsNoRefresh();
		plugin.refreshAllTasks();
	}

	private JPanel getSearchPanel()
	{
		JPanel filtersPanel = new JPanel();
		filtersPanel.setAlignmentX(LEFT_ALIGNMENT);
		filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

		SearchBox textSearch = new SearchBox();
		textSearch.addTextChangedListener(() -> {
			String searchText = textSearch.getText();

			// Create the appropriate matcher based on config
			boolean regexEnabled = config.enableRegexSearch();
			TextMatcher matcher = TextMatcherFactory.create(searchText, regexEnabled);

			plugin.taskTextMatcher = matcher;

			// Show tooltip feedback
			String errorMessage = matcher.getErrorMessage();
			if (errorMessage != null)
			{
				textSearch.setToolTipText(errorMessage);
			}
			else if (matcher instanceof RegexTextMatcher)
			{
				textSearch.setToolTipText("Regex mode active");
			}
			else if (regexEnabled)
			{
				textSearch.setToolTipText("Regex mode inactive");
			}
			else
			{
				textSearch.setToolTipText(null);
			}

			plugin.refreshAllTasks();
		});

		filtersPanel.add(textSearch);

		return filtersPanel;
	}

	private void updateCollapseButtonText()
	{
		if (taskService.getCurrentTaskType() == null)
		{
			return;
		}

		ArrayList<FilterConfig> filters = taskService.getCurrentTaskType().getFilters();

		int countInclusive = 0;
		int countExclusive = 0;

		for (FilterConfig filterConfig : filters)
		{
			String filterText = Optional.ofNullable(plugin.getConfigManager()
					.getConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME,
						taskService.getCurrentTaskType().getFilterConfigPrefix() + filterConfig.getConfigKey()))
				.orElse("");

			int count = (filterText.isEmpty() || filterText.equals("-1")) ? 0 : filterText.split(",").length;

			if (filterConfig.getFilterType() == null)
			{
				log.warn("Skipping filter count for {} - filterType is null", filterConfig.getConfigKey());
				continue;
			}
			if (filterConfig.getFilterType().equals(FilterType.BUTTON_FILTER))
			{
				countInclusive += count;
			}
			if (filterConfig.getFilterType().equals(FilterType.DROPDOWN_FILTER))
			{
				countExclusive += count;
			}
		}

		collapseBtn.setText(countInclusive + " inclusive, " + countExclusive + " exclusive filters");
	}


	public void setTaskTypeDropdownEnabled(boolean enabled)
	{
		if (taskTypeDropdown != null)
		{
			taskTypeDropdown.setEnabled(enabled);
		}
	}

	public void hideRouteModeButton(boolean hide)
	{
		routeButton.setVisible(!hide);
	}

	private void initTaskTypeDropdownAsync()
	{
		ITaskType currentTaskType = taskService.getCurrentTaskType();
		taskService.getTaskTypesByJsonName().thenAccept(taskTypes -> {
			ArrayList<ComboItem<ITaskType>> taskTypeItems = new ArrayList<>();
			taskTypes.forEach((taskTypeJsonName, taskType) -> {
				ComboItem<ITaskType> item = new ComboItem<>(taskType, taskType.getName());
				taskTypeItems.add(item);
				taskTypeDropdown.addItem(item);
			});

			ComboItem<ITaskType> currentTaskTypeComboItem = taskTypeItems.stream()
				.filter(item -> item.getValue().equals(currentTaskType))
				.findFirst().orElseGet(() -> taskTypeItems.get(0));
			taskTypeDropdown.addActionListener(e -> {
				ITaskType taskType = taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex()).getValue();
				taskService.setTaskType(taskType.getTaskJsonName());
			});
			taskTypeDropdown.setSelectedItem(currentTaskTypeComboItem);
		});
	}
}
