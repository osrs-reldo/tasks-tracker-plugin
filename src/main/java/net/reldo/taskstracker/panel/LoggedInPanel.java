package net.reldo.taskstracker.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
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
public class LoggedInPanel extends JPanel  implements ChangeListener
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
	private final JTabbedPane tabbedPane = new JTabbedPane();

	// sub-filter panel
	private SubFilterPanel subFilterPanel;
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

		tabbedPane.setBorder(new EmptyBorder(0,0,0,0));
		tabbedPane.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH,24));

		tabbedPane.addTab("Tracked Tasks", emptyPanel());
		tabbedPane.addTab("All Tasks", emptyPanel());
		tabbedPane.addTab("Custom", emptyPanel());

		taskListPanel.add(tabbedPane, BorderLayout.NORTH);
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

		tabbedPane.setSelectedIndex(config.taskListTab().ordinal());
		tabbedPane.addChangeListener(this);

		return taskListPanel;
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		ConfigValues.TaskListTabs newTab = ConfigValues.TaskListTabs.values()[tabbedPane.getSelectedIndex()];
		changeTab(newTab);

		if(newTab.equals(ConfigValues.TaskListTabs.TRACKED))
		{
			trackedFilterBtn.setState(1);
			trackedFilterBtn.setEnabled(false);
			plugin.getConfigManager().setConfiguration("tasks-tracker", "taskListTab", ConfigValues.TaskListTabs.TRACKED);
			filterButtonAction("tracked");
		}
		else
		{
			plugin.getConfigManager().setConfiguration("tasks-tracker", "taskListTab", newTab);
			plugin.refresh();
		}
	}

	private JPanel emptyPanel()
	{
		JPanel emptyPanel = new JPanel();
		emptyPanel.setBorder(new EmptyBorder(0,0,0,0));
		emptyPanel.setPreferredSize(new Dimension(0,0));
		emptyPanel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR.darker());
		emptyPanel.setVisible(false);
		return emptyPanel;
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
		exportButton.addActionListener(e -> plugin.copyJsonToClipboard(taskTypeDropdown.getItemAt(0).getValue())); // TODO: reimplement config
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
		taskService.getTaskTypes().forEach((taskTypeJsonName, taskType) -> taskTypeItems.add(new ComboItem(taskType, taskType.getName())));
		ComboItem<TaskType>[] comboItemsArray = taskTypeItems.toArray(new ComboItem[0]);
		taskTypeDropdown = new JComboBox<>(comboItemsArray);
		taskTypeDropdown.setAlignmentX(LEFT_ALIGNMENT);
		taskTypeDropdown.setSelectedItem(comboItemsArray[0]); // TODO: reimplement config
		taskTypeDropdown.addActionListener(e -> updateWithNewTaskType(taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex()).getValue()));

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

		// filter button
		SwingUtil.removeButtonDecorations(collapseBtn);
		collapseBtn.setIcon(Icons.MENU_COLLAPSED_ICON);
		collapseBtn.setSelectedIcon(Icons.MENU_EXPANDED_ICON);

		// panel to hold sub-filters
		subFilterPanel = new SubFilterPanel(plugin, taskService);

		subFilterWrapper.add(collapseBtn, BorderLayout.NORTH);
		subFilterWrapper.add(subFilterPanel, BorderLayout.CENTER);

		northPanel.add(getTitleAndButtonPanel());
		northPanel.add(Box.createVerticalStrut(10));
		northPanel.add(taskTypeDropdown);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(getSearchPanel());
		northPanel.add(Box.createVerticalStrut(5));
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

		// Filter button bar
		final JPanel viewControls = new JPanel();
		viewControls.setLayout(new BoxLayout(viewControls, BoxLayout.X_AXIS));
		viewControls.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Completed tasks filter button
		SwingUtil.removeButtonDecorations(completedFilterBtn);
		completedFilterBtn.setIcons(Icons.COMPLETE_INCOMPLETE_ICON, Icons.COMPLETE_ONLY_ICON, Icons.INCOMPLETE_ONLY_ICON);
		completedFilterBtn.setToolTips("All tasks", "Completed tasks only", "Incomplete tasks only");
		completedFilterBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		completedFilterBtn.addActionListener(e -> {
			completedFilterBtn.changeState();
			filterButtonAction("completed");
		});
		completedFilterBtn.setState(config.completedFilter().ordinal());

		// Create popup menu for manually setting the button state
		final JPopupMenu completedFilterBtnPopupMenu = new JPopupMenu();
		completedFilterBtnPopupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		completedFilterBtn.setComponentPopupMenu(completedFilterBtnPopupMenu);

		final JMenuItem allTasksC = new JMenuItem("All tasks");
		allTasksC.addActionListener(e -> {
			completedFilterBtn.setState(0);
			filterButtonAction("completed");
		});
		completedFilterBtnPopupMenu.add(allTasksC);

		final JMenuItem completedTasks = new JMenuItem("Completed tasks only");
		completedTasks.addActionListener(e -> {
			completedFilterBtn.setState(1);
			filterButtonAction("completed");
		});
		completedFilterBtnPopupMenu.add(completedTasks);

		final JMenuItem incompleteTasks = new JMenuItem("Incomplete tasks only");
		incompleteTasks.addActionListener(e -> {
			completedFilterBtn.setState(2);
			filterButtonAction("completed");
		});
		completedFilterBtnPopupMenu.add(incompleteTasks);

		viewControls.add(completedFilterBtn);

		// Tracked tasks filter button
		SwingUtil.removeButtonDecorations(trackedFilterBtn);
		trackedFilterBtn.setIcons(Icons.TRACKED_UNTRACKED_ICON, Icons.TRACKED_ONLY_ICON, Icons.UNTRACKED_ONLY_ICON);
		trackedFilterBtn.setToolTips("All tasks", "Tracked tasks only", "Untracked tasks only");
		trackedFilterBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		trackedFilterBtn.addActionListener(e -> {
			trackedFilterBtn.changeState();
			filterButtonAction("tracked");
		});
		trackedFilterBtn.setState(config.trackedFilter().ordinal());

		// Create popup menu for manually setting the button state
		final JPopupMenu trackedFilterBtnPopupMenu = new JPopupMenu();
		trackedFilterBtnPopupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		trackedFilterBtn.setComponentPopupMenu(trackedFilterBtnPopupMenu);

		final JMenuItem allTasksT = new JMenuItem("All tasks");
		allTasksT.addActionListener(e -> {
			trackedFilterBtn.setState(0);
			filterButtonAction("tracked");
		});
		trackedFilterBtnPopupMenu.add(allTasksT);

		final JMenuItem trackedTasks = new JMenuItem("Tracked tasks only");
		trackedTasks.addActionListener(e -> {
			trackedFilterBtn.setState(1);
			filterButtonAction("tracked");
		});
		trackedFilterBtnPopupMenu.add(trackedTasks);

		final JMenuItem untrackedTasks = new JMenuItem("Untracked tasks only");
		untrackedTasks.addActionListener(e -> {
			trackedFilterBtn.setState(2);
			filterButtonAction("tracked");
		});
		trackedFilterBtnPopupMenu.add(untrackedTasks);

		viewControls.add(trackedFilterBtn);

		// Ignored tasks filter button
		SwingUtil.removeButtonDecorations(ignoredFilterBtn);
		ignoredFilterBtn.setIcons(Icons.SEMIVISIBLE_ICON, Icons.VISIBLE_ICON, Icons.INVISIBLE_ICON);
		ignoredFilterBtn.setToolTips("Hide ignored tasks", "All tasks", "Ignored tasks only");
		ignoredFilterBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		ignoredFilterBtn.addActionListener(e -> {
			ignoredFilterBtn.changeState();
			filterButtonAction("ignored");
		});
		ignoredFilterBtn.setState(config.ignoredFilter().ordinal());

		// Create popup menu for manually setting the button state
		final JPopupMenu ignoredFilterBtnPopupMenu = new JPopupMenu();
		ignoredFilterBtnPopupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		ignoredFilterBtn.setComponentPopupMenu(ignoredFilterBtnPopupMenu);

		final JMenuItem allTasksI = new JMenuItem("All tasks");
		allTasksI.addActionListener(e -> {
			ignoredFilterBtn.setState(1);
			filterButtonAction("ignored");
		});
		ignoredFilterBtnPopupMenu.add(allTasksI);

		final JMenuItem unignoredTasks = new JMenuItem("Hide ignored tasks");
		unignoredTasks.addActionListener(e -> {
			ignoredFilterBtn.setState(0);
			filterButtonAction("ignored");
		});
		ignoredFilterBtnPopupMenu.add(unignoredTasks);

		final JMenuItem ignoredTasks = new JMenuItem("Ignored tasks only");
		ignoredTasks.addActionListener(e -> {
			ignoredFilterBtn.setState(2);
			filterButtonAction("ignored");
		});
		ignoredFilterBtnPopupMenu.add(ignoredTasks);

		viewControls.add(ignoredFilterBtn);

		titlePanel.add(viewControls, BorderLayout.EAST);
		titlePanel.add(title, BorderLayout.WEST);
		titlePanel.setAlignmentX(LEFT_ALIGNMENT);

		return titlePanel;
	}

	private void filterButtonAction(String filter)
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

	private void updateWithNewTaskType(TaskType taskType)
	{
		plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskTypeName", taskType.getTaskJsonName());
		taskService.setTaskType(taskType.getTaskJsonName());
		redraw();
		refresh(null);
	}

	private void updateCollapseButtonText()
	{
		// TODO: needs to be updated to support dynamic filters, can just be a total, "X filters"
		if(getSelectedTaskType() == null) return;

		List<String> filterCounts = new ArrayList<>();
		int count = config.tierFilter().equals("") ? 0 : config.tierFilter().split(",").length;
		filterCounts.add(count + " tier");

		collapseBtn.setText(String.join(", ", filterCounts) + " filters");
	}

	private TaskType getSelectedTaskType()
	{
		return taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex()).getValue();
	}
}