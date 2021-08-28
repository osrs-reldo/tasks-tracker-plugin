package com.tylerthardy.taskstracker.types;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.types.testtask.TestTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class TaskPanel extends JPanel
{
    private final Task task;
    @Getter
    private final JLabel icon = new JLabel();
    @Getter
    private final JPanel body = new JPanel(new BorderLayout());

    public TaskPanel(TasksTrackerPlugin plugin, SpriteManager spriteManager, Task task)
    {
        super(new BorderLayout());
        this.task = task;
        createPanel(task);
        setComponentPopupMenu(createPopupMenu());
    }

    public void createPanel(Task task)
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(7, 0, 0, 0));

        JPanel container = new JPanel();
        container.setBorder(new EmptyBorder(7, 7, 6, 0));
        container.setLayout(new BorderLayout());
        container.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Icon
        if (task.getIcon() != null)
        {
            icon.setMinimumSize(new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT));
            icon.setIcon(new ImageIcon(task.getIcon()));
        }

        // Body
        body.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JLabel name = new JShadowedLabel(task.getName());
        name.setFont(FontManager.getRunescapeSmallFont());
        name.setForeground(Color.WHITE);
        String descriptionText = task.getDescription();
        if (task instanceof TestTask)
        {
            descriptionText +=  ((TestTask) task).filter1;
        }
        JLabel description = new JLabel("<html>" + descriptionText + "</html>");
        description.setFont(FontManager.getRunescapeSmallFont());
        description.setForeground(Color.GRAY);
        body.add(name, BorderLayout.NORTH);
        body.add(description, BorderLayout.CENTER);

        // Full
        container.add(icon, BorderLayout.WEST);
        container.add(body, BorderLayout.CENTER);
        add(container, BorderLayout.NORTH);

        setToolTipText(task.getDescription());
        createPopupMenu();
        revalidate();
    }

    private JPopupMenu createPopupMenu()
    {
        final JMenuItem removeTask = new JMenuItem("Remove");
        removeTask.addActionListener(e -> log.info(task.getName())); // plugin.toggleTrackTask(task.name));

        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
        popupMenu.add(removeTask);

        return popupMenu;
    }
}
