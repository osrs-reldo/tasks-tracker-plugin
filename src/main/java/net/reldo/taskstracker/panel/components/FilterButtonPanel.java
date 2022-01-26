package net.reldo.taskstracker.panel.components;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public abstract class FilterButtonPanel extends FixedWidthPanel
{
    protected final TasksTrackerPlugin plugin;

    protected final Map<String, JToggleButton> buttons = new HashMap<>();
    protected String configKey;


    public FilterButtonPanel(TasksTrackerPlugin plugin)
    {
        this.plugin = plugin;

    }

    protected abstract LinkedHashMap<String, BufferedImage> getIconImages();

    protected abstract JPanel makeButtonPanel();

    protected JToggleButton makeButton(String name, BufferedImage image)
    {
        JToggleButton button = new JToggleButton();
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setBorder(new EmptyBorder(2, 0, 2, 0));

        ImageIcon selectedIcon = new ImageIcon(image);
        ImageIcon deselectedIcon = new ImageIcon(ImageUtil.alphaOffset(image, -180));

        button.setIcon(deselectedIcon);
        button.setSelectedIcon(selectedIcon);
        button.setToolTipText(name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase());

        button.addActionListener(e -> {
            updateFilterText();
            plugin.refresh();
        });

        button.setSelected(true);

        return button;
    }

    protected void updateFilterText()
    {
        String filterText = buttons.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(","));

        plugin.getConfigManager().setConfiguration("tasks-tracker", configKey, filterText);
    }

    protected void setAllSelected(boolean state)
    {
        buttons.values().forEach(button -> button.setSelected(state));
    }
}
