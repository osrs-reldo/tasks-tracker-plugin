package com.tylerthardy.combattaskstracker.ui;

import com.google.gson.Gson;
import com.tylerthardy.combattaskstracker.CombatTasksTrackerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.HashSet;

public class CombatTasksTrackerPluginPanel extends PluginPanel
{
    private final JLabel title = new JLabel();
    private JPanel centerPanel;

    private final CombatTasksTrackerPlugin plugin;

    public CombatTasksTrackerPluginPanel(CombatTasksTrackerPlugin plugin)
    {
        this.plugin = plugin;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel northPanel = getNorthPanel();
        this.centerPanel = getCenterPanel();
        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void redrawTracker()
    {
        assert SwingUtilities.isEventDispatchThread();
        remove(centerPanel);
        centerPanel = getCenterPanel();
        add(centerPanel);
        revalidate();
        repaint();
    }

    private JPanel getNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

        title.setText("Combat Tasks Tracker");
        title.setForeground(Color.WHITE);

        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> copyJsonToClipboard());

        JButton resetButton = new JButton("Test");
        resetButton.addActionListener(e -> {
            plugin.trackedTasks = new HashSet<>();
            redrawTracker();
        });

        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(exportButton, BorderLayout.CENTER);
        northPanel.add(resetButton, BorderLayout.SOUTH);

        return northPanel;
    }

    private JPanel getCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        for (String trackedTask : plugin.trackedTasks) {
            TrackedCombatTaskPanel task = new TrackedCombatTaskPanel(trackedTask);
            centerPanel.add(task, constraints);
            constraints.gridy++;
            centerPanel.add(Box.createRigidArea(new Dimension(0, 10)), constraints);
            constraints.gridy++;
        }

        return centerPanel;
    }

    private void copyJsonToClipboard()
    {
        if (this.plugin.taskTitleColors.size() == 0)
        {
            showMessageBox(
                    "Cannot Export Data",
                    "You must open the tasks UI, areas UI, and relics UI before exporting.");
            return;
        }

        Gson gson = new Gson();
        String json = gson.toJson(this.plugin.taskTitleColors);
        final StringSelection stringSelection = new StringSelection(json);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        showMessageBox(
                "OS League Tools Data Exported!",
                "Exported data copied to clipboard! Go to osleague.tools, click Manage Data > Import from Runelite, and paste into the box."
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