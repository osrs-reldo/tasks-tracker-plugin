package net.reldo.taskstracker.panel.filters;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicBorders;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class FilterButton extends JToggleButton
{
	private final String key;
	private final FilterButtonPanel parentPanel;

	public FilterButton(String key, String tooltip, BufferedImage image, FilterButtonPanel parentPanel)
	{
		this.key = key;
		this.parentPanel = parentPanel;

		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new BasicBorders.ToggleButtonBorder(ColorScheme.DARKER_GRAY_COLOR,
			ColorScheme.DARKER_GRAY_COLOR.darker(),
			ColorScheme.MEDIUM_GRAY_COLOR.darker(),
			ColorScheme.MEDIUM_GRAY_COLOR));
		setFocusable(false);

		if (image != null)
		{
			ImageIcon selectedIcon = new ImageIcon(image);
			ImageIcon deselectedIcon = new ImageIcon(ImageUtil.alphaOffset(image, -180));

			setIcon(deselectedIcon);
			setSelectedIcon(selectedIcon);
			setPreferredSize(new Dimension(image.getWidth(), image.getHeight() + 10));
		}
		else
		{
			setPreferredSize(new Dimension(getPreferredSize().width, 50));
		}

		setToolTipText(tooltip.substring(0, 1).toUpperCase() + tooltip.substring(1).toLowerCase());

		addActionListener(e -> {
			parentPanel.updateFilterText();
			parentPanel.updateCollapseButtonText();
			parentPanel.plugin.refreshAllTasks();
		});

		setupRightClickMenu();

		setSelected(true);
	}

	private void setupRightClickMenu()
	{
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showContextMenu(e);
				}
			}
		});
	}

	private void showContextMenu(MouseEvent e)
	{
		JPopupMenu popupMenu = new JPopupMenu();
		
		JMenuItem filterToOnlyItem = new JMenuItem("Select Only");
		filterToOnlyItem.addActionListener(event -> {
			parentPanel.setOnlySelected(key);
			parentPanel.updateFilterText();
			parentPanel.updateCollapseButtonText();
			parentPanel.plugin.refreshAllTasks();
		});
		popupMenu.add(filterToOnlyItem);
		
		JMenuItem exceptItem = new JMenuItem("Select All Except");
		exceptItem.addActionListener(event -> {
			parentPanel.setAllExceptSelected(key);
			parentPanel.updateFilterText();
			parentPanel.updateCollapseButtonText();
			parentPanel.plugin.refreshAllTasks();
		});
		popupMenu.add(exceptItem);
		
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	public String getKey()
	{
		return key;
	}
}
