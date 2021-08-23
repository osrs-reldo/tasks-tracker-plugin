package com.tylerthardy.combattaskstracker.ui;

import com.tylerthardy.combattaskstracker.CombatTask;
import net.runelite.api.Constants;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

public class TrackedCombatTaskPanel extends JPanel {

    private final SpriteManager spriteManager;

    private final JLabel icon = new JLabel();
    private final JPanel details = new JPanel(new BorderLayout());
    private final int PADDING_TOP = 7;
    private final int CONTAINER_PADDING_TOP = 7;
    private final int CONTAINER_PADDING_BOTTOM = 6;

    public TrackedCombatTaskPanel(SpriteManager spriteManager, CombatTask task) {
        super(new BorderLayout());
        this.spriteManager = spriteManager;
        createPanel(task);
    }

    public void createPanel(CombatTask task)
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(7, 0, 0, 0));

        JPanel container = new JPanel();
        container.setBorder(new EmptyBorder(CONTAINER_PADDING_TOP, CONTAINER_PADDING_TOP, CONTAINER_PADDING_BOTTOM, 0));
        container.setLayout(new BorderLayout());
        container.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Icon
        icon.setMinimumSize(new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT));
        spriteManager.getSpriteAsync(task.tier.spriteId, 0, sprite -> {
            icon.setIcon(new ImageIcon(sprite));
        });

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
        revalidate();
    }
}
