package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.components.CheckBox;
import com.tylerthardy.taskstracker.panel.components.SearchBox;
import com.tylerthardy.taskstracker.panel.tabs.AllTaskListPanel;
import com.tylerthardy.taskstracker.panel.tabs.TrackedTaskListPanel;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

@Slf4j
public class LoggedInPanel extends PluginPanel
{
	public AllTaskListPanel allTasksPanel;
	public TrackedTaskListPanel trackedTaskListPanel;
	private TasksTrackerPlugin plugin;
	private final ClientThread clientThread;
	private final SpriteManager spriteManager;
	private SkillIconManager skillIconManager;

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

		JLabel title = new JLabel("Tasks Tracker");
		title.setHorizontalAlignment(SwingConstants.LEFT);
		title.setForeground(Color.WHITE);

		JComboBox<TaskType> taskTypeDropdown = new JComboBox<>(TaskType.values());
		taskTypeDropdown.setAlignmentX(LEFT_ALIGNMENT);
		taskTypeDropdown.setSelectedItem(plugin.selectedTaskType);
		taskTypeDropdown.addActionListener(e -> updateWithNewTaskType(taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex())));

		northPanel.add(title);
		northPanel.add(Box.createVerticalStrut(10));
		northPanel.add(taskTypeDropdown);
		northPanel.add(Box.createVerticalStrut(2));
		northPanel.add(getFiltersPanel());

		return northPanel;
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

		CheckBox isIncompleteCheckbox = new CheckBox("Show Incomplete Only");
		isIncompleteCheckbox.setSelected(plugin.isIncompleteFilter);
		isIncompleteCheckbox.addActionListener(e -> {
			plugin.isIncompleteFilter = isIncompleteCheckbox.isSelected();
			plugin.refresh();
		});

		filtersPanel.add(textSearch);
		filtersPanel.add(isIncompleteCheckbox);

		return filtersPanel;
	}

	private void updateWithNewTaskType(TaskType taskType)
	{
		plugin.setSelectedTaskType(taskType);
		redraw();
	}
}