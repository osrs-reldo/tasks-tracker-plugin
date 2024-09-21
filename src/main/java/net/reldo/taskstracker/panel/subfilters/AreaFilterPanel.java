package net.reldo.taskstracker.panel.subfilters;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.league4.League4TaskArea;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

public class AreaFilterPanel extends FilterButtonPanel
{
    public AreaFilterPanel(TasksTrackerPlugin plugin, SpriteManager spriteManager)
    {
        super(plugin, "Area");
        this.configKey = "areaFilter";

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        redraw();
    }

    @Override
    protected JPanel makeButtonPanel()
    {
        // Panel that holds area icons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        LinkedHashMap<String, BufferedImage> buttonImages = getIconImages();

        buttonPanel.setLayout(new GridLayout(buttonImages.size() / 3, 3));

        // For each area create a button and add it to the UI
        buttonImages.forEach((name, image) -> {
            JToggleButton button = makeButton(name, image);
            buttons.put(name, button);
            buttonPanel.add(button);
        });

        return buttonPanel;
    }

    //@TODO: (maybe) change this to use SpriteManager after it has apologised to me
    @Override
    protected LinkedHashMap<String, BufferedImage> getIconImages()
    {
        LinkedHashMap<String, BufferedImage> images = new LinkedHashMap<>();
        String areaName;
        BufferedImage areaImage;
        String directory = "panel/components/task_areas/";

        if (plugin.getConfig().taskType() == null) return images;

        if (plugin.getConfig().taskType().equals(TaskType.LEAGUE_4))
        {
            directory += "league4/";

            for (League4TaskArea area : League4TaskArea.values())
            {
                areaName = area.name().toLowerCase();
                String areaIcon = directory + areaName + ".png";
                areaImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, areaIcon);

                images.put(areaName, areaImage);
            }
        }

        return images;
    }

    @Override
    public void redraw()
    {
        if (plugin.getConfig().taskType() != null)
            setVisible(plugin.getConfig().taskType().equals(TaskType.LEAGUE_4));

        //@todo remove duplication by generalising save/load to all subfilters
        assert SwingUtilities.isEventDispatchThread();

        buttons.clear();
        removeAll();

        collapseBtn = makeCollapseButton();
        buttonPanel = makeButtonPanel();

        add(collapseBtn, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(allOrNoneButtons(), BorderLayout.SOUTH);

        if(plugin.getConfig().saveAreaFilterState())
        {
            String filterText = plugin.getConfig().areaFilter();

            buttons.forEach((key, value) -> value.setSelected(filterText.contains("f-" + key)));
        }
        else
        {
            updateFilterText();
        }

        updateCollapseButtonText();

        collapseBtn.setVisible(plugin.getConfig().filterPanelCollapsible());

        validate();
        repaint();
    }

}
