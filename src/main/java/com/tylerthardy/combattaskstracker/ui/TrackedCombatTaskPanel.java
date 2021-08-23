package com.tylerthardy.combattaskstracker.ui;

import com.tylerthardy.combattaskstracker.CombatTask;
import com.tylerthardy.combattaskstracker.CombatTasksTrackerPlugin;
import net.runelite.api.Constants;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

public class TrackedCombatTaskPanel extends JPanel {

    private final CombatTasksTrackerPlugin plugin;
    private final SpriteManager spriteManager;
    private final CombatTask task;
    private final JLabel icon = new JLabel();
    private final JPanel details = new JPanel(new BorderLayout());

    public TrackedCombatTaskPanel(CombatTasksTrackerPlugin plugin, SpriteManager spriteManager, CombatTask task) {
        super(new BorderLayout());
        this.plugin = plugin;
        this.spriteManager = spriteManager;
        this.task = task;
        createPanel(task);
        setComponentPopupMenu(createPopupMenu());
    }

    public void createPanel(CombatTask task)
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(7, 0, 0, 0));

        JPanel container = new JPanel();
        container.setBorder(new EmptyBorder(7, 7, 6, 0));
        container.setLayout(new BorderLayout());
        container.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Icon
        icon.setMinimumSize(new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT));
        spriteManager.getSpriteAsync(task.tier.spriteId, 0, sprite -> icon.setIcon(new ImageIcon(sprite)));

        // Details
        details.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JLabel name = new JShadowedLabel(task.name);
        name.setFont(FontManager.getRunescapeSmallFont());
        name.setForeground(Color.WHITE);
        JLabel description = new JLabel("<html>" + task.description + "</html>");
        description.setFont(FontManager.getRunescapeSmallFont());
        description.setForeground(Color.GRAY);
        details.add(name, BorderLayout.NORTH);
        details.add(description, BorderLayout.CENTER);

        // Full
        container.add(icon, BorderLayout.WEST);
        container.add(details, BorderLayout.CENTER);
        add(container, BorderLayout.NORTH);

        setToolTipText(task.description);
        createPopupMenu();
        revalidate();
    }

    private JPopupMenu createPopupMenu()
    {
        final JMenuItem removeTask = new JMenuItem("Remove");
        removeTask.addActionListener(e -> plugin.toggleTrackTask(task.name));

        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
        popupMenu.add(removeTask);

        return popupMenu;
    }
}
