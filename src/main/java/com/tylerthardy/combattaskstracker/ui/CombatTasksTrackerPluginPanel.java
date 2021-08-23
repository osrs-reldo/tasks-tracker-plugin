package com.tylerthardy.combattaskstracker.ui;

import com.google.gson.Gson;
import com.tylerthardy.combattaskstracker.CombatTask;
import com.tylerthardy.combattaskstracker.CombatTasksTrackerPlugin;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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

import static com.tylerthardy.combattaskstracker.CombatTask.COLLATERAL_DAMAGE;
import static com.tylerthardy.combattaskstracker.CombatTask.DONT_LOOK_AT_ME;
import static com.tylerthardy.combattaskstracker.CombatTask.DONT_LOOK_AT_THE_ECLIPSE;
import static com.tylerthardy.combattaskstracker.CombatTask.HARDER_MODE_I;
import static com.tylerthardy.combattaskstracker.CombatTask.HARDER_MODE_II;
import static com.tylerthardy.combattaskstracker.CombatTask.ID_RATHER_NOT_LEARN;
import static com.tylerthardy.combattaskstracker.CombatTask.JUST_LIKE_THAT;
import static com.tylerthardy.combattaskstracker.CombatTask.LEAVING_NO_ONE_BEHIND;
import static com.tylerthardy.combattaskstracker.CombatTask.LIGHTNING_LURE;
import static com.tylerthardy.combattaskstracker.CombatTask.PERSONAL_SPACE;
import static com.tylerthardy.combattaskstracker.CombatTask.REMINISCE;
import static com.tylerthardy.combattaskstracker.CombatTask.ROYAL_AFFAIRS;
import static com.tylerthardy.combattaskstracker.CombatTask.WHACKAMOLE;

public class CombatTasksTrackerPluginPanel extends PluginPanel
{
    private final boolean developerMode;
    private FixedWidthPanel mainPanel;

    private final CombatTasksTrackerPlugin plugin;
    private final SpriteManager spriteManager;

    public CombatTasksTrackerPluginPanel(CombatTasksTrackerPlugin plugin, SpriteManager spriteManager, boolean developerMode)
    {
        super(false);
        this.plugin = plugin;
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
        populatePanelWithTasks(mainPanel);
        validate();
        repaint();
    }

    private void drawPanel() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(getNorthPanel(), BorderLayout.NORTH);

        FixedWidthPanel centerPanel = new FixedWidthPanel();
        mainPanel = new FixedWidthPanel();
        mainPanel.setBorder(new EmptyBorder(8, 10, 10, 10));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(mainPanel, BorderLayout.NORTH);
        centerPanel.setBackground(Color.RED);
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

        JButton clearButton = new JButton("Clear");
        clearButton.setBorder(new EmptyBorder(5, 5, 5, 5));
        clearButton.setLayout(new BorderLayout(0, BORDER_OFFSET));
        clearButton.addActionListener(e -> clear());
        southPanel.add(clearButton, BorderLayout.NORTH);

        if (developerMode)
        {
            JButton testButton = new JButton("Test");
            testButton.setBorder(new EmptyBorder(5, 5, 5, 5));
            testButton.setLayout(new BorderLayout(0, BORDER_OFFSET));
            testButton.addActionListener(e -> test());
            southPanel.add(testButton, BorderLayout.CENTER);
        }

        return southPanel;
    }

    private void clear()
    {
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                "Are you sure you clear your tracked tasks?", "Clear Tasks",
                JOptionPane.YES_NO_OPTION))
        {
            plugin.trackedTasks.clear();
            refresh();
        }
    }

    private void test() {
        int displayCount = (int)(Math.random() * 10);
        CombatTask[] testTasks = {
                PERSONAL_SPACE,
                ROYAL_AFFAIRS,
                HARDER_MODE_I,
                HARDER_MODE_II,
                REMINISCE,
                WHACKAMOLE,
                COLLATERAL_DAMAGE,
                LIGHTNING_LURE,
                LEAVING_NO_ONE_BEHIND,
                DONT_LOOK_AT_THE_ECLIPSE,
                DONT_LOOK_AT_ME,
                ID_RATHER_NOT_LEARN,
                JUST_LIKE_THAT
        };
        for (int i = 0; i < displayCount; i++) {
            plugin.trackedTasks.add(testTasks[i]);
        }
        refresh();
    }

    private JPanel getNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Combat Tasks Tracker");
        title.setForeground(Color.WHITE);

        northPanel.add(title, BorderLayout.NORTH);

        return northPanel;
    }

    private void populatePanelWithTasks(JPanel target) {
        for (CombatTask trackedTask : plugin.trackedTasks) {
            TrackedCombatTaskPanel task = new TrackedCombatTaskPanel(spriteManager, trackedTask);
            target.add(task);
        }
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