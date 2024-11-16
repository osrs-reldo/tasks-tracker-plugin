package net.reldo.taskstracker.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.FilterType;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.data.task.TaskType;
import net.reldo.taskstracker.panel.components.SearchBox;
import net.reldo.taskstracker.panel.components.TriToggleButton;
import net.reldo.taskstracker.panel.filters.ComboItem;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class LoggedInPanel extends JPanel
{
	public TaskListPanel taskListPanel;
	private JComboBox<ComboItem<TaskType>> taskTypeDropdown;

	private TaskService taskService;
	private final TaskPanelFactory taskPanelFactory;
	private final TasksTrackerPlugin plugin;
	private final TasksTrackerConfig config;

	// Filter buttons
	private final TriToggleButton completedFilterBtn = new TriToggleButton();
	private final TriToggleButton trackedFilterBtn = new TriToggleButton();
	private final TriToggleButton ignoredFilterBtn = new TriToggleButton();
	private final JPanel titlePanel = new JPanel();

	// Task list tabs
	private final JPanel tabPane = new JPanel();

	// sub-filter panel
	private SubFilterPanel subFilterPanel;
	private SortPanel sortPanel;
	private final JToggleButton collapseBtn = new JToggleButton();

	public LoggedInPanel(TasksTrackerPlugin plugin, TasksTrackerConfig config, TaskService taskService, TaskPanelFactory taskPanelFactory)
	{
		super(false);
		this.plugin = plugin;
		this.taskService = taskService;
		this.taskPanelFactory = taskPanelFactory;
		this.config = config;

		createPanel();
	}

	@Override
	public Dimension getPreferredSize()
	{
		return getParent().getSize();
	}

	public void redraw()
	{
		subFilterPanel.redraw();
		sortPanel.redraw();
		updateCollapseButtonText();

		taskListPanel.redraw();
	}

	public void refresh(TaskFromStruct task)
	{
		if(task == null)
		{
			updateCollapseButtonText();
		}

		taskListPanel.refresh(task);
	}

	private void createPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		taskListPanel = new TaskListPanel(plugin, taskPanelFactory, taskService);

		add(getNorthPanel(), BorderLayout.NORTH);
		add(getCenterPanel(), BorderLayout.CENTER);
		add(getSouthPanel(), BorderLayout.SOUTH);

		loadAndApplyFilters(config.taskListTab());
		if(config.taskListTab().equals(ConfigValues.TaskListTabs.TRACKED))
		{
			trackedFilterBtn.setState(1);
			trackedFilterBtn.setEnabled(false);
			plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "trackedFilter", ConfigValues.TrackedFilterValues.TRACKED);
		}
	}

	private JPanel getCenterPanel() {
		// wrapper for the task list and tab buttons
		final JPanel taskListPanel = new JPanel(new BorderLayout());
		taskListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		taskListPanel.setBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR));
		taskListPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);

		tabPane.setLayout(new BoxLayout(tabPane, BoxLayout.X_AXIS));
		tabPane.setBorder(new EmptyBorder(0,0,0,0));
		tabPane.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH,24));

		JToggleButton trackedTab = tabButton("Tracked Tasks", ConfigValues.TaskListTabs.TRACKED);
		JToggleButton allTab = tabButton("All Tasks", ConfigValues.TaskListTabs.ALL);
		JToggleButton customTab = tabButton("Custom", ConfigValues.TaskListTabs.CUSTOM);

		ButtonGroup tabGroup = new ButtonGroup();

		tabGroup.add(trackedTab);
		tabGroup.add(allTab);
		tabGroup.add(customTab);

		tabPane.add(Box.createHorizontalGlue());
		tabPane.add(trackedTab);
		tabPane.add(Box.createHorizontalGlue());
		tabPane.add(allTab);
		tabPane.add(Box.createHorizontalGlue());
		tabPane.add(customTab);
		tabPane.add(Box.createHorizontalGlue());

		taskListPanel.add(tabPane, BorderLayout.NORTH);
		taskListPanel.add(this.taskListPanel, BorderLayout.CENTER);

		// set initial filter states to "complete and incomplete", "tracked and untracked", "not ignored"
		Map<String, Integer> filterStates = new HashMap<>();
		filterStates.put("completed",0);
		filterStates.put("tracked",0);
		filterStates.put("ignored",0);
		for(ConfigValues.TaskListTabs tab : ConfigValues.TaskListTabs.values())
		{
			filterStore.put(tab, filterStates);
		}

		switch (config.taskListTab())
		{
			case TRACKED:
				trackedTab.setSelected(true);
				break;
			case ALL:
				allTab.setSelected(true);
				break;
			case CUSTOM:
				customTab.setSelected(true);
				break;
		}
		tabChanged(config.taskListTab());

		return taskListPanel;
	}

	public void tabChanged(ConfigValues.TaskListTabs newTab)
	{
		if(newTab != null) {
			changeTab(newTab);

            switch (newTab) {
                case TRACKED:
                    trackedFilterBtn.setState(1);
                    trackedFilterBtn.setEnabled(false);
                    plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskListTab", newTab);
                    filterButtonAction("tracked");
                    break;
                case ALL:
                    trackedFilterBtn.setState(0);
                    trackedFilterBtn.setEnabled(false);
                    completedFilterBtn.setState(0);
                    completedFilterBtn.setEnabled(false);
                    ignoredFilterBtn.setState(1);
                    ignoredFilterBtn.setEnabled(false);
                    plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskListTab", newTab);
                    actionAllFilterButtons();
                    break;
				case CUSTOM:
					plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskListTab", newTab);
					plugin.refresh();
					break;
                default:
                    plugin.refresh();
                    break;
            }
		}
	}

	private JToggleButton tabButton(String label, ConfigValues.TaskListTabs tab)
	{
		JToggleButton button = new JToggleButton(label);

		button.setBorder(new EmptyBorder(2,5,2,5));
		button.setBackground(ColorScheme.DARK_GRAY_COLOR);
		button.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		button.addActionListener(e -> tabChanged(tab));

		return button;
	}

	private void changeTab(ConfigValues.TaskListTabs newTab)
	{
		saveFilters();
		resetFilters();
		loadAndApplyFilters(newTab);
	}

	private final Map<ConfigValues.TaskListTabs, Map<String, Integer>> filterStore = new HashMap<>();

	private void saveFilters()
	{
		ConfigValues.TaskListTabs tab = config.taskListTab();

		Map<String, Integer> filterStates = new HashMap<>();
		filterStates.put("completed", config.completedFilter().ordinal());
		filterStates.put("tracked", config.trackedFilter().ordinal());
		filterStates.put("ignored", config.ignoredFilter().ordinal());

		filterStore.put(tab, filterStates);
	}

	private void resetFilters()
	{
		completedFilterBtn.setEnabled(true);
		trackedFilterBtn.setEnabled(true);
		ignoredFilterBtn.setEnabled(true);
	}

	private void loadAndApplyFilters(ConfigValues.TaskListTabs tab)
	{
		Map<String,Integer> filterStates = filterStore.get(tab);

		if(filterStates == null) return;

		Enum configValue;

		completedFilterBtn.setState(filterStates.get("completed"));
		trackedFilterBtn.setState(filterStates.get("tracked"));
		ignoredFilterBtn.setState(filterStates.get("ignored"));

		configValue = ConfigValues.CompletedFilterValues.values()[completedFilterBtn.getState()];
		plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "completedFilter", configValue);

		configValue = ConfigValues.TrackedFilterValues.values()[trackedFilterBtn.getState()];
		plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "trackedFilter", configValue);

		configValue = ConfigValues.IgnoredFilterValues.values()[ignoredFilterBtn.getState()];
		plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "ignoredFilter", configValue);
	}

	private JPanel getSouthPanel()
	{
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(new EmptyBorder(5,0,2,0));

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
		exportButton.addActionListener(e -> plugin.copyJsonToClipboard());
		southPanel.add(exportButton, BorderLayout.EAST);

		return southPanel;
	}

	private JPanel getNorthPanel()
	{
		JPanel northPanel = new JPanel();
		BoxLayout layout = new BoxLayout(northPanel, BoxLayout.Y_AXIS);
		northPanel.setLayout(layout);
		northPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		ArrayList<ComboItem<TaskType>> taskTypeItems = new ArrayList<>();
		taskService.getTaskTypesByJsonName().forEach((taskTypeJsonName, taskType) -> taskTypeItems.add(new ComboItem(taskType, taskType.getName())));
		ComboItem<TaskType>[] comboItemsArray = taskTypeItems.toArray(new ComboItem[0]);
		taskTypeDropdown = new JComboBox<>(comboItemsArray);
		taskTypeDropdown.setAlignmentX(LEFT_ALIGNMENT);

		TaskType currentTaskType = taskService.getCurrentTaskType();
		ComboItem<TaskType> currentTaskTypeComboItem = Arrays.stream(comboItemsArray)
			.filter(item -> item.getValue().equals(currentTaskType))
			.findFirst().orElseGet(() -> comboItemsArray[0]);
		taskTypeDropdown.setSelectedItem(currentTaskTypeComboItem);
		taskTypeDropdown.addActionListener(e -> {
			TaskType taskType = taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex()).getValue();
			boolean wasTaskTypeChanged = taskService.setTaskType(taskType);
			if (wasTaskTypeChanged)
			{
				redraw();
				refresh(null);
			}
		});
		taskTypeDropdown.setFocusable(false);

		// Wrapper for collapsible sub-filter menu
		JPanel subFilterWrapper = new JPanel();
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

		sortPanel = new SortPanel(plugin, taskService, taskListPanel);

		northPanel.add(getTitleAndButtonPanel());
		northPanel.add(Box.createVerticalStrut(10));
		northPanel.add(taskTypeDropdown);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(getSearchPanel());
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(sortPanel);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(subFilterWrapper);

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
		reloadPluginMenuItem.addActionListener(e -> plugin.reload());
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
		ignoredFilterBtn.setIcons(Icons.SEMIVISIBLE_ICON, Icons.VISIBLE_ICON, Icons.INVISIBLE_ICON);
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

	private void filterButtonActionNoRefresh(String filter)
	{
		int state;
		Enum configValue;

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

		plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, filter + "Filter", configValue);
	}

	private void filterButtonAction(String filter)
	{
		filterButtonActionNoRefresh(filter);
		plugin.refresh();
	}

	private void actionAllFilterButtons()
	{
		filterButtonActionNoRefresh("tracked");
		filterButtonActionNoRefresh("ignored");
		filterButtonActionNoRefresh("completed");
		plugin.refresh();
	}

	private JPanel getSearchPanel()
	{
		JPanel filtersPanel = new JPanel();
		filtersPanel.setAlignmentX(LEFT_ALIGNMENT);
		filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

		SearchBox textSearch = new SearchBox();
		textSearch.addTextChangedListener(() -> {
			plugin.taskTextFilter = textSearch.getText().toLowerCase();
			plugin.refresh();
		});

		filtersPanel.add(textSearch);

		return filtersPanel;
	}

	private void updateCollapseButtonText()
	{
		if (taskService.getCurrentTaskType() == null) return;
		
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

			if (filterConfig.getFilterType().equals(FilterType.BUTTON_FILTER)) countInclusive += count;
			if (filterConfig.getFilterType().equals(FilterType.DROPDOWN_FILTER)) countExclusive += count;
		}

		collapseBtn.setText(countInclusive + " inclusive, "  + countExclusive + " exclusive filters");
	}
}