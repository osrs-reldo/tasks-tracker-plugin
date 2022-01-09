package com.tylerthardy.taskstracker.panel;

import com.google.gson.Gson;
import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.components.CheckBox;
import com.tylerthardy.taskstracker.panel.components.SearchBox;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class TasksTrackerPluginPanel extends PluginPanel
{
    private AllTaskListPanel allTasksPanel;
    private TrackedTaskListPanel trackedTaskListPanel;
    private TasksTrackerPlugin plugin;
    private final ClientThread clientThread;
    private final SpriteManager spriteManager;
    private SkillIconManager skillIconManager;

    public TasksTrackerPluginPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
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
        return new Dimension(PANEL_WIDTH + SCROLLBAR_WIDTH, super.getPreferredSize().height);
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
        exportButton.addActionListener(e -> copyJsonToClipboard());
        southPanel.add(exportButton, BorderLayout.SOUTH);

        return southPanel;
    }

    private JPanel getNorthPanel()
	{
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Tasks Tracker");
        title.setForeground(Color.WHITE);

        JComboBox<TaskType> taskTypeDropdown = new JComboBox<>(TaskType.values());
        taskTypeDropdown.setSelectedItem(plugin.selectedTaskType);
        taskTypeDropdown.addActionListener(e -> updateWithNewTaskType(taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex())));

        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(taskTypeDropdown, BorderLayout.CENTER);
        northPanel.add(getFiltersPanel(), BorderLayout.SOUTH);

        return northPanel;
    }

    private JPanel getFiltersPanel()
	{
		JPanel filtersPanel = new JPanel();
		filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

		SearchBox text = new SearchBox();
		text.addTextChangedListener(() -> {
			plugin.taskTextFilter = text.getText().toLowerCase();
			plugin.refresh();
		});

		CheckBox isIncomplete = new CheckBox("Show Incomplete Only");
		isIncomplete.setSelected(plugin.isIncompleteFilter);
		isIncomplete.addActionListener(e -> {
			plugin.isIncompleteFilter = isIncomplete.isSelected();
			plugin.refresh();
		});

		filtersPanel.add(text);
		filtersPanel.add(isIncomplete);

		return filtersPanel;
	}

    private void updateWithNewTaskType(TaskType taskType)
    {
        plugin.setSelectedTaskType(taskType);
        redraw();
    }

    private void copyJsonToClipboard()
    {
        if (plugin.taskManagers.get(plugin.selectedTaskType).tasks.size() == 0)
        {
            showMessageBox(
                    "Cannot Export Data",
                    "There is no task data to export. Try opening UIs for the tasks to gather data.");
            return;
        }

        Gson gson = new Gson();
        String json = gson.toJson(plugin.taskManagers.get(plugin.selectedTaskType).tasks);
        final StringSelection stringSelection = new StringSelection(json);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        showMessageBox(
                "Data Exported!",
                "Exported task data copied to clipboard!"
        );
    }

    private static void showMessageBox(final String title, final String message)
    {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        null,
                        message, title,
                        JOptionPane.INFORMATION_MESSAGE));
    }

}