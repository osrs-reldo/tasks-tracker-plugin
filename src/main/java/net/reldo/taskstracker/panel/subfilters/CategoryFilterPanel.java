package net.reldo.taskstracker.panel.subfilters;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.league4.League4TaskCategory;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

public class CategoryFilterPanel extends FilterButtonPanel
{

    public CategoryFilterPanel(TasksTrackerPlugin plugin, SpriteManager spriteManager)
    {
        super(plugin, "Category");
        this.configKey = "categoryFilter";

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        redraw();
    }

    @Override
    protected JPanel makeButtonPanel()
    {
        // Panel that holds category icons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        LinkedHashMap<String, BufferedImage> buttonImages = getIconImages();

        buttonPanel.setLayout(new GridLayout(buttonImages.size() / 3, 3));

        // For each category create a button and add it to the UI
        buttonImages.forEach((name, image) -> {
            JToggleButton button = makeButton(name, image);
            buttons.put(name, button);
            buttonPanel.add(button);
        });

        return buttonPanel;
    }

    //@todo (maybe) change this to use SpriteManager after it has apologised to me
    @Override
    protected LinkedHashMap<String, BufferedImage> getIconImages()
    {
        LinkedHashMap<String, BufferedImage> images = new LinkedHashMap<>();
        String categoryName;
        BufferedImage categoryImage;
        String directory = "panel/components/task_categories/";

        if (plugin.getConfig().taskType() == null ||
            plugin.getConfig().taskType().equals(TaskType.COMBAT) ||
            plugin.getConfig().taskType().equals(TaskType.LEAGUE_3)) return images;

        if (plugin.getConfig().taskType().equals(TaskType.LEAGUE_4))
        {
            directory += "league4/";

            for (League4TaskCategory category : League4TaskCategory.values())
            {
                categoryName = category.name().toLowerCase();
                String categoryIcon = directory + categoryName + ".png";
                categoryImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, categoryIcon);

                images.put(categoryName, categoryImage);
            }
        }

        return images;
    }

    @Override
    public void redraw()
    {
        if (plugin.getConfig().taskType() != null)
            this.setVisible(plugin.getConfig().taskType().equals(TaskType.LEAGUE_4));

        super.redraw();
    }
}
