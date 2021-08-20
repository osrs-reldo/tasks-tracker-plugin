package com.tylerthardy.combattaskstracker;

import com.google.gson.Gson;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class CombatTasksTrackerPanel extends PluginPanel
{
    private final JLabel title = new JLabel();

    private final CombatTasksTrackerPlugin plugin;

    public CombatTasksTrackerPanel(CombatTasksTrackerPlugin plugin)
    {
        this.plugin = plugin;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

        title.setText("Combat Tasks Tracker");
        title.setForeground(Color.WHITE);

        northPanel.add(title, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> copyJsonToClipboard());
        centerPanel.setBackground( ColorScheme.DARK_GRAY_COLOR);
        centerPanel.add(exportButton);

        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
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