package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.components.SearchBox;
import com.tylerthardy.taskstracker.panel.components.TriToggleButton;
import com.tylerthardy.taskstracker.panel.tabs.AllTaskListPanel;
import com.tylerthardy.taskstracker.panel.tabs.TrackedTaskListPanel;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class LoggedInPanel extends PluginPanel
{
	public AllTaskListPanel allTasksPanel;
	public TrackedTaskListPanel trackedTaskListPanel;
	private JComboBox<TaskType> taskTypeDropdown;

	private final TasksTrackerPlugin plugin;
	private final ClientThread clientThread;
	private final SpriteManager spriteManager;
	private final SkillIconManager skillIconManager;

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

	public LoggedInPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
	{
		super(false);
		this.plugin = plugin;
		this.clientThread = clientThread;
		this.spriteManager = spriteManager;
		this.skillIconManager = skillIconManager;

		createPanel(this);
		redraw();
	}

	@Override
	public Dimension getPreferredSize()
	{
		return this.getParent().getSize();
	}

	public void redraw()
	{
		if (plugin.selectedTaskType != null)
		{
			taskTypeDropdown.setSelectedItem(plugin.selectedTaskType);
		}
		allTasksPanel.redraw();
		trackedTaskListPanel.redraw();
	}

	public void refresh(Task task)
	{
		allTasksPanel.refresh(task);
		trackedTaskListPanel.refresh(task);
	}

	private void createPanel(JPanel parent) {
		parent.setLayout(new BorderLayout());
		parent.setBackground(ColorScheme.DARK_GRAY_COLOR);

		trackedTaskListPanel = new TrackedTaskListPanel(plugin, clientThread, spriteManager, skillIconManager);
		allTasksPanel = new AllTaskListPanel(plugin, clientThread, spriteManager, skillIconManager);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Tracked Tasks", trackedTaskListPanel);
		tabbedPane.addTab("All Tasks", allTasksPanel);

		parent.add(getNorthPanel(), BorderLayout.NORTH);
		parent.add(tabbedPane, BorderLayout.CENTER);
		parent.add(getSouthPanel(), BorderLayout.SOUTH);
	}

	private JPanel getSouthPanel()
	{
		JPanel southPanel = new JPanel(new BorderLayout());

		JButton exportButton = new JButton("Export");
		exportButton.setBorder(new EmptyBorder(5, 5, 5, 5));
		exportButton.setLayout(new BorderLayout(0, BORDER_OFFSET));
		exportButton.addActionListener(e -> plugin.copyJsonToClipboard(plugin.selectedTaskType));
		southPanel.add(exportButton, BorderLayout.SOUTH);

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
		taskTypeDropdown.setSelectedItem(plugin.selectedTaskType);
		taskTypeDropdown.addActionListener(e -> updateWithNewTaskType(taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex())));

		northPanel.add(getTitlePanel());
		northPanel.add(Box.createVerticalStrut(10));
		northPanel.add(taskTypeDropdown);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(getFiltersPanel());

		return northPanel;
	}

	private JPanel getTitlePanel()
	{
		titlePanel.setLayout(new BorderLayout());
		titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		titlePanel.setPreferredSize(new Dimension(0, 30));
		titlePanel.setBorder(new EmptyBorder(5, 5, 5, 10));

		JLabel title = new JLabel("Tasks Tracker");
		title.setHorizontalAlignment(SwingConstants.LEFT);
		title.setForeground(Color.WHITE);

		final JPanel viewControls = new JPanel();
		viewControls.setLayout(new BoxLayout(viewControls, BoxLayout.X_AXIS));
		viewControls.setBackground(ColorScheme.DARK_GRAY_COLOR);

		SwingUtil.removeButtonDecorations(completedFilterBtn);
		completedFilterBtn.setIcons(COMPLETE_INCOMPLETE_ICON, COMPLETE_ONLY_ICON, INCOMPLETE_ONLY_ICON);
		completedFilterBtn.setToolTips("All tasks", "Completed tasks only", "Incomplete tasks only");
		completedFilterBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		completedFilterBtn.addActionListener(e -> {
			completedFilterBtn.changeState();
			plugin.isCompleteFilter = completedFilterBtn.getState() != 2;
			plugin.isIncompleteFilter = completedFilterBtn.getState() != 1;
			plugin.refresh();
		});
		viewControls.add(completedFilterBtn);

		SwingUtil.removeButtonDecorations(trackedFilterBtn);
		trackedFilterBtn.setIcons(TRACKED_UNTRACKED_ICON, TRACKED_ONLY_ICON, UNTRACKED_ONLY_ICON);
		trackedFilterBtn.setToolTips("All tasks", "Tracked tasks only", "Untracked tasks only");
		trackedFilterBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		trackedFilterBtn.addActionListener(e -> {
			trackedFilterBtn.changeState();
			plugin.isTrackedFilter = trackedFilterBtn.getState() != 2;
			plugin.isUntrackedFilter = trackedFilterBtn.getState() != 1;
			plugin.refresh();
		});
		viewControls.add(trackedFilterBtn);

		SwingUtil.removeButtonDecorations(ignoredFilterBtn);
		ignoredFilterBtn.setIcons(SEMIVISIBLE_ICON, VISIBLE_ICON, INVISIBLE_ICON);
		ignoredFilterBtn.setToolTips("Hide ignored tasks", "All tasks", "Ignored tasks only");
		ignoredFilterBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
		ignoredFilterBtn.addActionListener(e -> {
			ignoredFilterBtn.changeState();
			plugin.isIgnoredFilter = ignoredFilterBtn.getState() != 0;
			plugin.isNotIgnoredFilter = ignoredFilterBtn.getState() != 2;
			plugin.refresh();
		});
		viewControls.add(ignoredFilterBtn);

		titlePanel.add(viewControls, BorderLayout.EAST);
		titlePanel.add(title, BorderLayout.WEST);
		titlePanel.setAlignmentX(LEFT_ALIGNMENT);

		return titlePanel;
	}


	private JPanel getFiltersPanel()
	{
		JPanel filtersPanel = new JPanel();
		filtersPanel.setAlignmentX(LEFT_ALIGNMENT);
		filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

		SearchBox textSearch = new SearchBox();
		textSearch.addTextChangedListener(() -> {
			plugin.taskTextFilter = textSearch.getText().toLowerCase();
			plugin.refresh();
		});

//		CheckBox isIncompleteCheckbox = new CheckBox("Show Incomplete Only");
//		isIncompleteCheckbox.setSelected(plugin.isIncompleteFilter);
//		isIncompleteCheckbox.addActionListener(e -> {
//			plugin.isIncompleteFilter = isIncompleteCheckbox.isSelected();
//			plugin.refresh();
//		});

		filtersPanel.add(textSearch);
//		filtersPanel.add(isIncompleteCheckbox);

		return filtersPanel;
	}

	private void updateWithNewTaskType(TaskType taskType)
	{
		plugin.setSelectedTaskType(taskType);
		redraw();
	}
}