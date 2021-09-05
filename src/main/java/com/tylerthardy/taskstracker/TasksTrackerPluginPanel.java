package com.tylerthardy.taskstracker;

import com.google.gson.Gson;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskManager;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TasksTrackerPluginPanel extends PluginPanel
{
    private final boolean developerMode;
    private final FixedWidthPanel mainPanel = new FixedWidthPanel();

    private final TasksTrackerPlugin plugin;
    private TaskManager taskManager;
    private ClientThread clientThread;
    private final SpriteManager spriteManager;

    public TasksTrackerPluginPanel(TasksTrackerPlugin plugin, TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager, boolean developerMode)
    {
        super(false);
        this.plugin = plugin;
        this.taskManager = taskManager;
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;
        this.developerMode = developerMode;

        drawPanel();
        refresh();
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(PANEL_WIDTH + SCROLLBAR_WIDTH, super.getPreferredSize().height);
    }

    public void refresh()
    {
        assert SwingUtilities.isEventDispatchThread();
        mainPanel.removeAll();

        log.debug("Creating panels...");
        ArrayList<Task> tasks = taskManager.tasks.get(taskManager.selectedTaskType);
        if (tasks != null) {
            for (Task task : tasks) {
                TaskPanel taskPanel = task.generatePanel(plugin, clientThread, spriteManager);
                mainPanel.add(taskPanel);
            }
        }
        log.debug("Validate and repaint...");
        validate();
        repaint();
    }

    private void drawPanel() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(getNorthPanel(), BorderLayout.NORTH);

        FixedWidthPanel centerPanel = new FixedWidthPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(mainPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        add(getSouthPanel(), BorderLayout.SOUTH);
    }

    private JPanel getSouthPanel()
    {
        JPanel southPanel = new JPanel(new BorderLayout());

        JButton exportButton = new JButton("Export");
        exportButton.setBorder(new EmptyBorder(5, 5, 5, 5));
        exportButton.setLayout(new BorderLayout(0, BORDER_OFFSET));
        exportButton.addActionListener(e -> copyJsonToClipboard());
        southPanel.add(exportButton, BorderLayout.SOUTH);

        JButton removeAllButton = new JButton("Remove All");
        removeAllButton.setBorder(new EmptyBorder(5, 5, 5, 5));
        removeAllButton.setLayout(new BorderLayout(0, BORDER_OFFSET));
        removeAllButton.addActionListener(e -> removeAllTasks());
        southPanel.add(removeAllButton, BorderLayout.NORTH);

        return southPanel;
    }

    private void removeAllTasks()
    {
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                "Are you sure you want to remove all of your tracked tasks?", "Remove All Tasks",
                JOptionPane.YES_NO_OPTION))
        {
            taskManager.trackedTasks.clear();
            refresh();
        }
    }

    private JPanel getNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Tasks Tracker");
        title.setForeground(Color.WHITE);

        JComboBox<TaskType> taskTypeDropdown = new JComboBox<>(TaskType.values());
        taskTypeDropdown.setSelectedItem(taskManager.selectedTaskType);
        taskTypeDropdown.addActionListener(e -> updateWithNewTaskType(taskTypeDropdown.getItemAt(taskTypeDropdown.getSelectedIndex())));

        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(taskTypeDropdown, BorderLayout.SOUTH);

        return northPanel;
    }

    private void updateWithNewTaskType(TaskType taskType)
    {
        taskManager.setSelectedTaskType(taskType);
        refresh();
    }

    private void copyJsonToClipboard()
    {
        if (taskManager.tasks.size() == 0)
        {
            showMessageBox(
                    "Cannot Export Data",
                    "There is no task data to export. Try opening UIs for the tasks to gather data.");
            return;
        }

        Gson gson = new Gson();
        String json = gson.toJson(taskManager.tasks);
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