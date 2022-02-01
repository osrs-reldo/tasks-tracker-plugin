package net.reldo.taskstracker.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.panel.components.SearchBox;
import net.reldo.taskstracker.panel.components.TriToggleButton;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class LoggedInPanel extends JPanel
{
	public TaskListPanel taskListPanel;
	private JComboBox<TaskType> taskTypeDropdown;

	private final TasksTrackerPlugin plugin;
	private final ClientThread clientThread;
	private final SpriteManager spriteManager;
	private final SkillIconManager skillIconManager;
	private final TasksTrackerConfig config;

	// Filter buttons
	private TriToggleButton completedFilterBtn = new TriToggleButton();
	private TriToggleButton trackedFilterBtn = new TriToggleButton();
	private TriToggleButton ignoredFilterBtn = new TriToggleButton();
	private JPanel titlePanel = new JPanel();

	private final String completeBtnPath = "panel/components/complete_button/style_2a/";
	private final Icon COMPLETE_INCOMPLETE_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, completeBtnPath + "complete_and_incomplete_icon.png"));
	private final Icon COMPLETE_ONLY_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, completeBtnPath + "complete_only_icon.png"));
	private final Icon INCOMPLETE_ONLY_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, completeBtnPath + "incomplete_only_icon.png"));

	private final String ignoredBtnPath = "panel/components/ignored_button/";
	private final Icon VISIBLE_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, ignoredBtnPath + "visible_icon.png"));
	private final Icon INVISIBLE_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, ignoredBtnPath + "invisible_icon.png"));
	private final BufferedImage semivisibleimg = ImageUtil.loadImageResource(TasksTrackerPlugin.class, ignoredBtnPath + "semivisible_icon.png");
	private final Icon SEMIVISIBLE_ICON = new ImageIcon(ImageUtil.alphaOffset(semivisibleimg, -180));

	private final String trackedBtnPath = "panel/components/tracked_button/";
	private final Icon TRACKED_UNTRACKED_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, trackedBtnPath + "tracked_and_untracked_icon.png"));
	private final Icon TRACKED_ONLY_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, trackedBtnPath + "tracked_icon.png"));
	private final Icon UNTRACKED_ONLY_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, trackedBtnPath + "untracked_icon.png"));

	// Task list tabs
	private final JRadioButton trackedTasksListPanelBtn = new JRadioButton();
	private final JRadioButton allTasksListPanelBtn = new JRadioButton();
	private final JRadioButton customTasksListPanelBtn = new JRadioButton();

	private final String tabBtnPath = "panel/components/list_tabs/";

	private final Icon ALL_TAB_SELECTED_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "all_tasks_selected.png"));
	private final Icon ALL_TAB_HOVER_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "all_tasks_hovered.png"));
	private final Icon ALL_TAB_UNSELECTED_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "all_tasks_unselected.png"));

	private final Icon TRACKED_TAB_SELECTED_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "tracked_tasks_selected.png"));
	private final Icon TRACKED_TAB_HOVER_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "tracked_tasks_hovered.png"));
	private final Icon TRACKED_TAB_UNSELECTED_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "tracked_tasks_unselected.png"));

	private final Icon CUSTOM_TAB_SELECTED_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "custom_tasks_selected.png"));
	private final Icon CUSTOM_TAB_HOVER_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "custom_tasks_hovered.png"));
	private final Icon CUSTOM_TAB_UNSELECTED_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "custom_tasks_unselected.png"));

	private final Icon LEFT_TAB_SPACER_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "left_spacer.png"));
	private final Icon RIGHT_TAB_SPACER_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, tabBtnPath + "right_spacer.png"));

	// sub-filter panel
	private SubFilterPanel subFilterPanel;
	private final JToggleButton collapseBtn = new JToggleButton();

	private final String expandBtnPath = "panel/components/";
	private final BufferedImage collapseImg = ImageUtil.loadImageResource(TasksTrackerPlugin.class, expandBtnPath + "filter_menu_collapsed.png");
	private final Icon MENU_COLLAPSED_ICON = new ImageIcon(ImageUtil.alphaOffset(collapseImg, -180));
	private final Icon MENU_ICON_HOVER = new ImageIcon(collapseImg);
	private final Icon MENU_EXPANDED_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, expandBtnPath + "filter_menu_expanded.png"));

	public LoggedInPanel(TasksTrackerPlugin plugin, TasksTrackerConfig config, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
	{
		super(false);
		this.plugin = plugin;
		this.clientThread = clientThread;
		this.spriteManager = spriteManager;
		this.skillIconManager = skillIconManager;
		this.config = config;

		createPanel(this);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return this.getParent().getSize();
	}

	public void redraw()
	{
		subFilterPanel.redraw();
		taskListPanel.redraw();
	}

	public void refresh(Task task)
	{
		if(task == null)
			updateCollapseButtonText();

		taskListPanel.refresh(task);
	}

	private void createPanel(JPanel parent)
	{
		parent.setLayout(new BorderLayout());
		parent.setBackground(ColorScheme.DARK_GRAY_COLOR);

		taskListPanel = new TaskListPanel(plugin, clientThread, spriteManager, skillIconManager);

		parent.add(getNorthPanel(), BorderLayout.NORTH);
		parent.add(getCenterPanel(), BorderLayout.CENTER);
		parent.add(getSouthPanel(), BorderLayout.SOUTH);

		loadAndApplyFilters(config.taskListTab());
		if(config.taskListTab().equals(ConfigValues.TaskListTabs.TRACKED))
		{
			trackedFilterBtn.setState(1);
			trackedFilterBtn.setEnabled(false);
		}
	}

	private JPanel getCenterPanel() {
		// wrapper for the task list and tab buttons
		final JPanel taskListPanel = new JPanel(new BorderLayout());
		taskListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// list tab button wrapper
		final JPanel taskListTabButtons = new JPanel();
		taskListTabButtons.setBackground(ColorScheme.DARK_GRAY_COLOR);
		taskListTabButtons.setLayout(new BoxLayout(taskListTabButtons, BoxLayout.X_AXIS));

		SwingUtil.removeButtonDecorations(trackedTasksListPanelBtn);
		trackedTasksListPanelBtn.setBorder(new EmptyBorder(0,0,0,0));
		trackedTasksListPanelBtn.setIcon(TRACKED_TAB_UNSELECTED_ICON);
		trackedTasksListPanelBtn.setSelectedIcon(TRACKED_TAB_SELECTED_ICON);
		trackedTasksListPanelBtn.setRolloverIcon(TRACKED_TAB_HOVER_ICON);
		trackedTasksListPanelBtn.addActionListener(e -> {
			changeTab(ConfigValues.TaskListTabs.TRACKED);
			trackedFilterBtn.setState(1);
			trackedFilterBtn.setEnabled(false);
			plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskListTab", ConfigValues.TaskListTabs.TRACKED);
			filterButtonAction("tracked");
		});
		trackedTasksListPanelBtn.setSelected(config.taskListTab().equals(ConfigValues.TaskListTabs.TRACKED));

		SwingUtil.removeButtonDecorations(allTasksListPanelBtn);
		allTasksListPanelBtn.setBorder(new EmptyBorder(0,0,0,0));
		allTasksListPanelBtn.setIcon(ALL_TAB_UNSELECTED_ICON);
		allTasksListPanelBtn.setSelectedIcon(ALL_TAB_SELECTED_ICON);
		allTasksListPanelBtn.setRolloverIcon(ALL_TAB_HOVER_ICON);
		allTasksListPanelBtn.addActionListener(e -> {
			changeTab(ConfigValues.TaskListTabs.ALL);
			plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskListTab", ConfigValues.TaskListTabs.ALL);
			plugin.refresh();
		});
		allTasksListPanelBtn.setSelected(config.taskListTab().equals(ConfigValues.TaskListTabs.ALL));

		SwingUtil.removeButtonDecorations(customTasksListPanelBtn);
		customTasksListPanelBtn.setBorder(new EmptyBorder(0,0,0,0));
		customTasksListPanelBtn.setIcon(CUSTOM_TAB_UNSELECTED_ICON);
		customTasksListPanelBtn.setSelectedIcon(CUSTOM_TAB_SELECTED_ICON);
		customTasksListPanelBtn.setRolloverIcon(CUSTOM_TAB_HOVER_ICON);
		customTasksListPanelBtn.addActionListener(e -> {
			changeTab(ConfigValues.TaskListTabs.CUSTOM);
			plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskListTab", ConfigValues.TaskListTabs.CUSTOM);
			plugin.refresh();
		});
		customTasksListPanelBtn.setSelected(config.taskListTab().equals(ConfigValues.TaskListTabs.CUSTOM));

		// set initial filter states to "complete and incomplete", "tracked and untracked", "not ignored"
		Map<String, Integer> filterStates = new HashMap<>();
		filterStates.put("completed",0);
		filterStates.put("tracked",0);
		filterStates.put("ignored",0);
		filterStore.put(ConfigValues.TaskListTabs.ALL, filterStates);
		filterStore.put(ConfigValues.TaskListTabs.CUSTOM, filterStates);
		filterStore.put(ConfigValues.TaskListTabs.TRACKED, filterStates);

		ButtonGroup listTabGroup = new ButtonGroup();
		listTabGroup.add(trackedTasksListPanelBtn);
		listTabGroup.add(allTasksListPanelBtn);
		listTabGroup.add(customTasksListPanelBtn);

		final JLabel leftTabSpacer = new JLabel(LEFT_TAB_SPACER_ICON);
		final JLabel rightTabSpacer = new JLabel(RIGHT_TAB_SPACER_ICON);

		taskListTabButtons.add(leftTabSpacer);
		taskListTabButtons.add(trackedTasksListPanelBtn);
		taskListTabButtons.add(allTasksListPanelBtn);
		taskListTabButtons.add(customTasksListPanelBtn);
		taskListTabButtons.add(rightTabSpacer);

		taskListPanel.add(taskListTabButtons, BorderLayout.NORTH);
		taskListPanel.add(this.taskListPanel, BorderLayout.CENTER);
		return taskListPanel;
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
		filterStates.put("completed",config.completedFilter().ordinal());
		filterStates.put("tracked",config.trackedFilter().ordinal());
		filterStates.put("ignored",config.ignoredFilter().ordinal());

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

		JButton importButton = new JButton("Import");
		importButton.setBorder(new EmptyBorder(5, 5, 5, 5));
		importButton.setLayout(new BorderLayout(0, PluginPanel.BORDER_OFFSET));
		importButton.addActionListener(e -> plugin.openImportJsonDialog());
		southPanel.add(importButton, BorderLayout.WEST);

		JButton exportButton = new JButton("Export");
		exportButton.setBorder(new EmptyBorder(5, 5, 5, 5));
		exportButton.setLayout(new BorderLayout(0, PluginPanel.BORDER_OFFSET));
		exportButton.addActionListener(e -> plugin.copyJsonToClipboard(plugin.getConfig().taskType()));
		southPanel.add(exportButton, BorderLayout.EAST);

		return southPanel;
	}

	private JPanel getNorthPanel()
	{
		JPanel northPanel = new JPanel();
		BoxLayout layout = new BoxLayout(northPanel, BoxLayout.Y_AXIS);
		northPanel.setLayout(layout);
		northPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		taskTypeDropdown = new JComboBox<>(TaskType.values());
		taskTypeDropdown.setAlignmentX(LEFT_ALIGNMENT);
		taskTypeDropdown.setSelectedItem(plugin.getConfig().taskType());
		taskTypeDropdown.addActionListener(e -> updateWithNewTaskType(taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex())));

		// Wrapper for collapsible sub-filter menu
		JPanel subFilterWrapper = new JPanel();
		subFilterWrapper.setLayout(new BorderLayout());
		subFilterWrapper.setBorder(new MatteBorder(1, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR));
		subFilterWrapper.setAlignmentX(LEFT_ALIGNMENT);
		subFilterWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// collapse button
		SwingUtil.removeButtonDecorations(collapseBtn);
		collapseBtn.setIcon(MENU_COLLAPSED_ICON);
		collapseBtn.setSelectedIcon(MENU_EXPANDED_ICON);
		collapseBtn.setRolloverIcon(MENU_ICON_HOVER);
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
		collapseBtn.setIcon(MENU_COLLAPSED_ICON);
		collapseBtn.setSelectedIcon(MENU_EXPANDED_ICON);

		// panel to hold sub-filters
		subFilterPanel = new SubFilterPanel(plugin, spriteManager);

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
		completedFilterBtn.setIcons(COMPLETE_INCOMPLETE_ICON, COMPLETE_ONLY_ICON, INCOMPLETE_ONLY_ICON);
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
		trackedFilterBtn.setIcons(TRACKED_UNTRACKED_ICON, TRACKED_ONLY_ICON, UNTRACKED_ONLY_ICON);
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
		ignoredFilterBtn.setIcons(SEMIVISIBLE_ICON, VISIBLE_ICON, INVISIBLE_ICON);
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
		plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskType", taskType);
		redraw();
		refresh(null);
	}

	private void updateCollapseButtonText()
	{
		List<String> filterCounts = new ArrayList<>();

		if(plugin.getConfig().taskType().equals(TaskType.LEAGUE_3))
		{
			int count = config.skillFilter().equals("") ? 0 : config.skillFilter().split(",").length ;
			filterCounts.add(count + " skill");
		}

		int count = config.tierFilter().equals("") ? 0 : config.tierFilter().split(",").length;
		filterCounts.add(count + " tier");

		collapseBtn.setText(String.join(", ", filterCounts) + " filters");
	}
}