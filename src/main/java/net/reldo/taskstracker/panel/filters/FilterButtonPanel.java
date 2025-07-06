package net.reldo.taskstracker.panel.filters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

@Slf4j
public abstract class FilterButtonPanel extends FilterPanel
{
    protected final TasksTrackerPlugin plugin;
    private final String label;

    protected final Map<String, JToggleButton> buttons = new HashMap<>();
    protected String configKey;
    protected JPanel buttonPanel;

    protected JToggleButton collapseBtn;
    private final String expandBtnPath = "panel/components/";
    private final BufferedImage collapseImg = ImageUtil.loadImageResource(TasksTrackerPlugin.class, expandBtnPath + "filter_buttons_collapsed.png");
    private final Icon MENU_COLLAPSED_ICON = new ImageIcon(ImageUtil.alphaOffset(collapseImg, -180));
    private final Icon MENU_ICON_HOVER = new ImageIcon(collapseImg);
    private final BufferedImage expandedImg = ImageUtil.loadImageResource(TasksTrackerPlugin.class, expandBtnPath + "filter_buttons_expanded.png");
    private final Icon MENU_EXPANDED_ICON = new ImageIcon(ImageUtil.alphaOffset(expandedImg, -180));
    private final Icon MENU_ICON_HOVER_SELECTED = new ImageIcon(expandedImg);


    public FilterButtonPanel(TasksTrackerPlugin plugin, String label)
    {
        this.plugin = plugin;

        this.label = label;
    }

    protected abstract LinkedHashMap<String, BufferedImage> getIconImages();

    protected abstract JPanel makeButtonPanel();

    protected JToggleButton makeButton(String tooltip, BufferedImage image)
    {
        JToggleButton button = new JToggleButton();
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setBorder(new BasicBorders.ToggleButtonBorder(ColorScheme.DARKER_GRAY_COLOR,
                                                             ColorScheme.DARKER_GRAY_COLOR.darker(),
                                                             ColorScheme.MEDIUM_GRAY_COLOR.darker(),
                                                             ColorScheme.MEDIUM_GRAY_COLOR));
        button.setFocusable(false);

        if (image != null) {
            ImageIcon selectedIcon = new ImageIcon(image);
            ImageIcon deselectedIcon = new ImageIcon(ImageUtil.alphaOffset(image, -180));

            button.setIcon(deselectedIcon);
            button.setSelectedIcon(selectedIcon);
            button.setPreferredSize(new Dimension(image.getWidth(), image.getHeight() + 10));
        } else {
	        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 50));
        }
        button.setToolTipText(tooltip.substring(0,1).toUpperCase() + tooltip.substring(1).toLowerCase());

        button.addActionListener(e -> {
            updateFilterText();
            updateCollapseButtonText();
            plugin.refreshAllTasks();
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
        all.setPreferredSize(new Dimension(50, 0));
        all.addActionListener(e -> {
            setAllSelected(true);
            updateFilterText();
            updateCollapseButtonText();
            plugin.refreshAllTasks();
        });

        JButton none = new JButton("none");
        SwingUtil.removeButtonDecorations(none);
        none.setFocusable(false);
        none.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        none.setFont(FontManager.getRunescapeSmallFont());
        none.setPreferredSize(new Dimension(50, 0));
        none.addActionListener(e -> {
            setAllSelected(false);
            updateFilterText();
            updateCollapseButtonText();
            plugin.refreshAllTasks();
        });

        JLabel separator = new JLabel("|");
        separator.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);

        buttonWrapper.add(Box.createHorizontalGlue());
        buttonWrapper.add(all);
        buttonWrapper.add(Box.createHorizontalGlue());
        buttonWrapper.add(separator);
        buttonWrapper.add(Box.createHorizontalGlue());
        buttonWrapper.add(none);
        buttonWrapper.add(Box.createHorizontalGlue());

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
        collapseBtn.setBorder(new EmptyBorder(2, 0, 2, 0));
        collapseBtn.setFocusable(false);
        collapseBtn.setSelected(true);

        return collapseBtn;
    }

    protected void updateFilterText()
    {
        String filterText = buttons.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(e -> "f-" + e.getKey() + "-f") // prefix included to cover cases where one key name is contained in another (e.g. "Master" -> "Grandmaster")
                .collect(Collectors.joining(","));

        plugin.getConfigManager().setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, configKey, filterText);
    }

    protected boolean getConfigButtonState(String buttonKey)
    {
        String configValue = plugin.getConfigManager().getConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, configKey);
        boolean isEmptyFilterSelection = configValue == null || configValue.isEmpty() || configValue.equals("-1");
        if (isEmptyFilterSelection)
        {
            return true;
        }

        return configValue.contains("f-" + buttonKey + "-f");
    }

    protected void setAllSelected(boolean state)
    {
        buttons.values().forEach(button -> button.setSelected(state));
    }

    protected void updateCollapseButtonText()
    {
        collapseBtn.setText(label + " - " + buttons.values().stream().filter(JToggleButton::isSelected).count() + " / " + buttons.size());
    }

    public void redraw()
    {
        if(SwingUtilities.isEventDispatchThread())
        {
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
        else
        {
            log.error("Filter button panel redraw failed - not event dispatch thread.");
        }
    }
}
