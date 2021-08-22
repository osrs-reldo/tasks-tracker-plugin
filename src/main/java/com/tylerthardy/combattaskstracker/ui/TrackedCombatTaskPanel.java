package com.tylerthardy.combattaskstracker.ui;

import com.tylerthardy.combattaskstracker.CombatTask;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TrackedCombatTaskPanel extends JPanel {

    public TrackedCombatTaskPanel(CombatTask task) {
        super(new BorderLayout());
        createPanel(task);
    }

    public void createPanel(CombatTask task) {
        setBorder(new EmptyBorder(10,10,10,10));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel title = new JLabel(task.name);
        title.setFont(FontManager.getRunescapeSmallFont());
        add(title, BorderLayout.NORTH);

        JLabel description = new JLabel(task.description);
        description.setFont(FontManager.getRunescapeSmallFont());
        add(description, BorderLayout.SOUTH);
    }
}
