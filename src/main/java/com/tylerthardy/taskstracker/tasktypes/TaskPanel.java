package com.tylerthardy.taskstracker.tasktypes;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.Util;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Constants;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

@Slf4j
public abstract class TaskPanel extends JPanel
{
    private final JLabel icon = new JLabel();
    private final JPanel container = new JPanel(new BorderLayout());
    private final JPanel body = new JPanel(new BorderLayout());
    private final JShadowedLabel name = new JShadowedLabel();
    private final JLabel description = new JLabel();
    private final JPanel buttons = new JPanel();
    private final JToggleButton toggleTrack = new JToggleButton();

    private TasksTrackerPlugin plugin;
    private final ClientThread clientThread;
    public final SpriteManager spriteManager;
    public final Task task;

    public abstract JPopupMenu getPopupMenu();
    public abstract String getTaskTooltip();
    public abstract BufferedImage getIcon();
    public Color getTaskBackgroundColor(Task task, int[] playerSkills)
    {
        return task.isCompleted() ? COMPLETED_COLOR : ColorScheme.DARKER_GRAY_COLOR;
    }

    public static Color COMPLETED_COLOR = new Color(0, 50, 0);
    public static Color UNQUALIFIED_COLOR = new Color(70, 30, 0);
    public static ImageIcon PLUS_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, "plus.png"));
    public static ImageIcon MINUS_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, "minus.png"));
    public static ImageIcon EYE_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, "eye.png"));

    public TaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, Task task)
    {
        super(new BorderLayout());
        this.plugin = plugin;
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;
        this.task = task;
        createPanel(task);
        setComponentPopupMenu(getPopupMenu());
        setToolTipText(getTaskTooltip());
        refresh();
    }

    public void createPanel(Task task)
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 7, 0));

        container.setBorder(new EmptyBorder(7, 7, 6, 0));

        // Body

        name.setFont(FontManager.getRunescapeSmallFont());
        name.setForeground(Color.WHITE);
        body.add(name, BorderLayout.NORTH);

        description.setFont(FontManager.getRunescapeSmallFont());
        description.setForeground(Color.GRAY);
        body.add(description, BorderLayout.CENTER);

        // Buttons
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(new EmptyBorder(0,0,0,7));
        toggleTrack.setPreferredSize(new Dimension(8, 8));
        toggleTrack.setIcon(PLUS_ICON);
        toggleTrack.setSelectedIcon(MINUS_ICON);
        toggleTrack.setBorder(new EmptyBorder(0,0,5,0));
        toggleTrack.addActionListener(e -> {
            task.setTracked(toggleTrack.isSelected());
            plugin.pluginPanel.redraw();
        });
        SwingUtil.removeButtonDecorations(toggleTrack);
        JLabel viewDetails = new JLabel();
        viewDetails.setIcon(EYE_ICON);
        buttons.add(toggleTrack);
        buttons.add(viewDetails);

        // Full
        container.add(icon, BorderLayout.WEST);
        container.add(body, BorderLayout.CENTER);
        container.add(buttons, BorderLayout.EAST);

        clientThread.invoke(() -> {
            if (getIcon() != null)
            {
                icon.setMinimumSize(new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT));
                icon.setIcon(new ImageIcon(getIcon()));
                icon.setBorder(new EmptyBorder(0,0,0,5));
            } else {
                icon.setBorder(new EmptyBorder(0,0,0,0));
            }
        });

        add(container, BorderLayout.NORTH);
    }

    public void refresh()
    {
        name.setText(Util.wrapWithHtml(task.getName()));
        description.setText(Util.wrapWithHtml(task.getDescription()));
        setBackgroundColor(getTaskBackgroundColor(task, plugin.playerSkills));
        toggleTrack.setSelected(task.isTracked());

        setVisible(meetsFilterCriteria());

        revalidate();
    }

    private boolean meetsFilterCriteria()
    {
        String nameLowercase = task.getName().toLowerCase();
        if (plugin.taskTextFilter != null && !nameLowercase.startsWith(plugin.taskTextFilter))
        {
            return false;
        }

        return true;
    }

    private void setBackgroundColor(Color color)
    {
        container.setBackground(color);
        body.setBackground(color);
        buttons.setBackground(color);
    }

    @Override
    public Dimension getMaximumSize()
    {
        return new Dimension(PluginPanel.PANEL_WIDTH, getPreferredSize().height);
    }
}
