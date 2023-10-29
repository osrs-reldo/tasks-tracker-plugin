package net.reldo.taskstracker.panel.subfilters;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.SubFilterPanel;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

public abstract class FilterButtonPanel extends FixedWidthPanel
{
    protected final TasksTrackerPlugin plugin;
    private final String name;

    protected final Map<String, JToggleButton> buttons = new HashMap<>();
    protected String configKey;
    private JPanel buttonPanel;

    private JToggleButton collapseBtn;
    private final String expandBtnPath = "panel/components/";
    private final BufferedImage collapseImg = ImageUtil.loadImageResource(TasksTrackerPlugin.class, expandBtnPath + "filter_buttons_collapsed.png");
    private final Icon MENU_COLLAPSED_ICON = new ImageIcon(ImageUtil.alphaOffset(collapseImg, -180));
    private final Icon MENU_ICON_HOVER = new ImageIcon(collapseImg);
    private final BufferedImage expandedImg = ImageUtil.loadImageResource(TasksTrackerPlugin.class, expandBtnPath + "filter_buttons_expanded.png");
    private final Icon MENU_EXPANDED_ICON = new ImageIcon(ImageUtil.alphaOffset(expandedImg, -180));
    private final Icon MENU_ICON_HOVER_SELECTED = new ImageIcon(expandedImg);


    public FilterButtonPanel(TasksTrackerPlugin plugin, String name)
    {
        this.plugin = plugin;

        this.name = name;
    }

    protected abstract LinkedHashMap<String, BufferedImage> getIconImages();

    protected abstract JPanel makeButtonPanel();

    protected JToggleButton makeButton(String name, BufferedImage image)
    {
        JToggleButton button = new JToggleButton();
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setBorder(new EmptyBorder(2, 0, 2, 0));
        button.setFocusable(false);

        if(image != null) {
            ImageIcon selectedIcon = new ImageIcon(image);
            ImageIcon deselectedIcon = new ImageIcon(ImageUtil.alphaOffset(image, -180));

            button.setIcon(deselectedIcon);
            button.setSelectedIcon(selectedIcon);
        }

        button.setToolTipText(name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase());

        button.addActionListener(e -> {
            updateFilterText();
            updateCollapseButtonText();
            plugin.refresh();
        });

        button.setSelected(true);

        return button;
    }

    protected JPanel allOrNoneButtons()
    {
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.X_AXIS));
        buttonWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        buttonWrapper.setAlignmentX(JPanel.CENTER_ALIGNMENT);

        JButton all = new JButton("all");
        SwingUtil.removeButtonDecorations(all);
        all.setFocusable(false);
        all.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        all.setFont(FontManager.getRunescapeSmallFont());
        all.addActionListener(e -> {
            setAllSelected(true);
            updateFilterText();
            updateCollapseButtonText();
            plugin.refresh();
        });

        JButton none = new JButton("none");
        SwingUtil.removeButtonDecorations(none);
        none.setFocusable(false);
        none.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        none.setFont(FontManager.getRunescapeSmallFont());
        none.addActionListener(e -> {
            setAllSelected(false);
            updateFilterText();
            updateCollapseButtonText();
            plugin.refresh();
        });

        JLabel separator = new JLabel("|");
        separator.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);

        buttonWrapper.add(all);
        buttonWrapper.add(separator);
        buttonWrapper.add(none);

        return buttonWrapper;
    }

    public JToggleButton makeCollapseButton()
    {
        JToggleButton collapseBtn = new JToggleButton();

        // collapse button
        SwingUtil.removeButtonDecorations(collapseBtn);
        collapseBtn.setIcon(MENU_COLLAPSED_ICON);
        collapseBtn.setSelectedIcon(MENU_EXPANDED_ICON);
        collapseBtn.setRolloverIcon(MENU_ICON_HOVER);
        collapseBtn.setRolloverSelectedIcon(MENU_ICON_HOVER_SELECTED);
        SwingUtil.addModalTooltip(collapseBtn, "Collapse filters", "Expand filters");
        collapseBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        collapseBtn.setAlignmentX(LEFT_ALIGNMENT);
        collapseBtn.setUI(new BasicButtonUI()); // substance breaks the layout
        collapseBtn.addActionListener(ev -> buttonPanel.setVisible(!buttonPanel.isVisible()));
        collapseBtn.setHorizontalTextPosition(JButton.CENTER);
        collapseBtn.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        collapseBtn.setFont(FontManager.getRunescapeSmallFont());
        collapseBtn.setSelected(true);

        return collapseBtn;
    }

    protected void updateFilterText()
    {
        String filterText = buttons.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(e -> "f-" + e.getKey()) // prefix included to cover cases where one key name is contained in another (e.g. "Master" -> "Grandmaster")
                .collect(Collectors.joining(","));

        plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, configKey, filterText);
    }

    protected void setAllSelected(boolean state)
    {
        buttons.values().forEach(button -> button.setSelected(state));
    }

    private void updateCollapseButtonText()
    {
        collapseBtn.setText(name + " - " + buttons.values().stream().filter(JToggleButton::isSelected).count() + " / " + buttons.size());
    }

    public void redraw()
    {
        assert SwingUtilities.isEventDispatchThread();

        buttons.clear();
        removeAll();

        collapseBtn = makeCollapseButton();
        buttonPanel = makeButtonPanel();

        add(collapseBtn, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(allOrNoneButtons(), BorderLayout.SOUTH);
        updateFilterText();
        updateCollapseButtonText();

        collapseBtn.setVisible(plugin.getConfig().filterPanelCollapsible());

        validate();
        repaint();
    }
}
